package me.duncanruns.reroller.mixin;

import me.duncanruns.reroller.RNGManager;
import me.duncanruns.reroller.SpawnerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {
    @Shadow
    protected abstract boolean isPlayerInRange();

    @Shadow
    private double field_9159;

    @Shadow
    private double field_9161;

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract BlockPos getPos();

    @Shadow
    private int spawnDelay;

    @Shadow
    protected abstract void updateSpawns();

    @Shadow
    private int spawnCount;

    @Shadow
    private MobSpawnerEntry spawnEntry;

    @Shadow
    private int spawnRange;

    @Shadow
    private int maxNearbyEntities;

    @Shadow
    protected abstract void spawnEntity(Entity entity);

    @Shadow
    private int minSpawnDelay;

    @Shadow
    private int maxSpawnDelay;

    @Shadow
    private int requiredPlayerRange;

    @Unique
    private boolean shouldSpawn = false;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    public void overwriteUpdate(CallbackInfo ci) {
        if (isCustomSpawner()) return; // If spawner isn't vanilla generated, don't touch it, too complicated
        World world = this.getWorld();
        if (world.isClient) return;

        ci.cancel();

        if (!this.isPlayerInRange()) {
            this.field_9159 = this.field_9161;
            return;
        }
        BlockPos blockPos = this.getPos();
        if (this.spawnDelay == -1) {
            this.updateSpawns();
        }

        SpawnerManager spawnerManager = SpawnerManager.get(world.getServer());

        String entityId = spawnEntry.getEntityTag().getString("id");
        if (!shouldSpawn) {
            if (entityId.isEmpty()) return;

            spawnDelay--;
            if (spawnDelay > 0) {
                if ((spawnDelay < (SpawnerManager.SPAWN_TIME_T0 / 2))) return;
                if (!spawnerManager.shouldSpawn(entityId, spawnDelay)) return;
            }
        }

        shouldSpawn = true;

        CompoundTag entityTag = this.spawnEntry.getEntityTag();
        Optional<EntityType<?>> entityTypeOpt = EntityType.fromTag(entityTag);
        if (!entityTypeOpt.isPresent()) {
            this.updateSpawns();
            return;
        }

        long cycleSeeding = RNGManager.mixSeed("spawner/cycle/" + entityId + "/" + spawnerManager.getCount(entityId), ((ServerWorld) world).getSeed());
        Random cycleRandom = new Random(cycleSeeding);

        long miscRandomSeeding = RNGManager.mixSeed("spawner/misc/" + entityId + "/" + spawnerManager.getCount(entityId), ((ServerWorld) world).getSeed());
        Random miscRandom = new Random(miscRandomSeeding);

        Predicate<Vec3d> canSpawnPred = pos -> {
            if (!world.doesNotCollide(entityTypeOpt.get().createSimpleBoundingBox(pos.x, pos.y, pos.z))) return false;
            return SpawnRestriction.canSpawn(entityTypeOpt.get(), world.getWorld(), SpawnReason.SPAWNER, new BlockPos(pos.x, pos.y, pos.z), miscRandom);
        };
        Supplier<Vec3d> posSupplier = () -> {
            double x = blockPos.getX() + (cycleRandom.nextDouble() - cycleRandom.nextDouble()) * this.spawnRange + 0.5;
            double y = blockPos.getY() + cycleRandom.nextInt(3) - 1;
            double z = blockPos.getZ() + (cycleRandom.nextDouble() - cycleRandom.nextDouble()) * this.spawnRange + 0.5;
            return new Vec3d(x, y, z);
        };

        long successes = IntStream.range(0, 512)
                .mapToObj(i -> posSupplier.get())
                .map(canSpawnPred::test)
                .filter(b -> b)
                .count();

        float successChance = (float) successes / 512;

        long toSpawn = Math.max(IntStream.range(0, this.spawnCount)
                .mapToObj(i -> cycleRandom.nextFloat() < successChance)
                .filter(b -> b)
                .count(), 1);
        int toAttempt = 1000;

        while (toSpawn > 0 && toAttempt > 0) {
            toAttempt--;
            CompoundTag compoundTag = this.spawnEntry.getEntityTag();
            Optional<EntityType<?>> optional = EntityType.fromTag(compoundTag);
            if (!optional.isPresent()) {
                this.updateSpawns();
                return;
            }
            Vec3d pos = posSupplier.get();
            if (!canSpawnPred.test(pos)) continue;
            Entity entity = EntityType.loadEntityWithPassengers(compoundTag, world, entityx -> {
                entityx.refreshPositionAndAngles(pos.x, pos.y, pos.z, entityx.yaw, entityx.pitch);
                return entityx;
            });
            if (entity == null) {
                this.updateSpawns();
                return;
            }

            int l = world.getNonSpectatingEntities(
                            entity.getClass(),
                            new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).expand(this.spawnRange)
                    )
                    .size();
            if (l >= this.maxNearbyEntities) {
                this.updateSpawns();
                return;
            }

            entity.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), world.random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof MobEntity) {
                MobEntity mobEntity = (MobEntity) entity;
                if (!mobEntity.canSpawn(world, SpawnReason.SPAWNER) || !mobEntity.canSpawn(world)) {
                    continue;
                }

                if (this.spawnEntry.getEntityTag().getSize() == 1 && this.spawnEntry.getEntityTag().contains("id", 8)) {
                    ((MobEntity) entity).initialize(world, world.getLocalDifficulty(entity.getBlockPos()), SpawnReason.SPAWNER, null, null);
                }
            }

            this.spawnEntity(entity);
            world.syncWorldEvent(2004, blockPos, 0);
            if (entity instanceof MobEntity) {
                ((MobEntity) entity).playSpawnEffects();
            }

            toSpawn--;
        }

        this.updateSpawns();
        shouldSpawn = false;


    }

    @Unique
    private boolean isCustomSpawner() {
        return minSpawnDelay != 200 || maxSpawnDelay != 800 || spawnCount != 4 || maxNearbyEntities != 6 || requiredPlayerRange != 16 || spawnRange != 4;
    }

    @Redirect(method = "updateSpawns", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", remap = false))
    private int replaceRandom(Random instance, int i) {
        return SpawnerManager.SPAWN_TIME_T0;
    }

}
