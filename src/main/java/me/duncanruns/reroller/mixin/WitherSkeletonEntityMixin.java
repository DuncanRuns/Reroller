package me.duncanruns.reroller.mixin;

import me.duncanruns.reroller.RNGManager;
import me.duncanruns.reroller.RerollerMod;
import me.duncanruns.reroller.random.CountedRandom;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WitherSkeletonEntity.class)
public abstract class WitherSkeletonEntityMixin extends AbstractSkeletonEntity {
    protected WitherSkeletonEntityMixin(EntityType<? extends AbstractSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);
        if (!causedByPlayer) return;
        int looting = RerollerMod.getLooting(source.getAttacker());
        String rngId = RerollerMod.SKULL_REROLLERS[MathHelper.clamp(looting, 0, 3)];
        CountedRandom random = RNGManager.getRandom(getServer(), rngId);
        if (random.nextFloat() < 0.025f + 0.01f * looting) {
            dropStack(new ItemStack(Items.WITHER_SKELETON_SKULL, 1));
        }
    }
}
