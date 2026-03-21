package me.duncanruns.reroller;

import me.duncanruns.reroller.mixinint.RerollerServer;
import me.duncanruns.reroller.mixinint.RerollerTagOwner;
import me.duncanruns.reroller.random.CountedRandom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SpawnerManager {
    public static final int SPAWN_TIME_T0 = 1000000;

    private final RNGManager rngManager;
    private final Map<String, Integer> nextSpawnTimes = new HashMap<>();
    private static final float COLLIDE_WITH_SPAWNER_CHANCE = 0.0864f;

    public SpawnerManager(RNGManager rngManager) {
        this.rngManager = rngManager;
    }

    public static SpawnerManager get(MinecraftServer server) {
        return ((RerollerServer) server).reroller$getSpawnerManager();
    }

    public boolean shouldSpawn(String entityName, int spawnDelay) {
        int nextSpawn = nextSpawnTimes.computeIfAbsent(entityName, s ->
                rerollNextSpawnTime(entityName)
        );
        if (spawnDelay > nextSpawn) return false;
        int overshot = nextSpawn - spawnDelay;
        nextSpawnTimes.put(entityName, rerollNextSpawnTime(entityName) + overshot);
        return true;
    }

    // 4 spawn attempts with 8.64% chance of failure on each attempt
    public static int getRandomSpawnCount(Random random, float obstruction) {
        int out = 0;
        for (int i = 0; i < 4; i++) {
            if (random.nextFloat() < (COLLIDE_WITH_SPAWNER_CHANCE + obstruction)) continue;
            out++;
        }
        return out;
    }

    private int rerollNextSpawnTime(String entityName) {
        return SPAWN_TIME_T0 - (getSpawnerCooldownRandom(entityName).nextInt(600));
    }

    public CountedRandom getSpawnerCooldownRandom(String entityName) {
        return rngManager.getRandom("spawner/cooldown/" + entityName);
    }

    public CountedRandom getSpawnerCountRandom(String entityName) {
        return rngManager.getRandom("spawner/count/" + entityName);
    }

    public void load(MinecraftServer server) {
        CompoundTag tag = ((RerollerTagOwner) server.getSaveProperties()).reroller$getTag();
        if (!tag.contains("SpawnerManager")) return;
        CompoundTag spawnerTag = tag.getCompound("SpawnerManager");
        spawnerTag.getKeys().forEach(s -> nextSpawnTimes.put(s, spawnerTag.getInt(s)));
    }

    public CompoundTag getTag() {
        CompoundTag tag = new CompoundTag();
        nextSpawnTimes.forEach(tag::putInt);
        return tag;
    }

    // Technically incorrect for entities that aren't 1x2x1 in size (e.g. magma cubes)
    public static float getObstruction(ServerWorld world, BlockPos spawnerPos) {
        int collided = 0;
        int total = 0;
        // 3 layers they can spawn on
        for (int y = -1; y <= 1; y++) {
            // in a 9x9 area
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    if (x == 0 && z == 0 && y < 1) continue;
                    for (int y2 = 0; y2 <= 1; y2++) {
                        int worth = 8 - (Math.abs(x) + Math.abs(z));
                        total += worth;
                        BlockPos pos = spawnerPos.add(x, y + y2, z);
                        if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()) collided += worth;
                    }
                }
            }
        }

        return ((float) collided / total) * (1 - COLLIDE_WITH_SPAWNER_CHANCE);
    }

    public int getCount(String entityId) {
        return getSpawnerCooldownRandom(entityId).getCount();
    }
}
