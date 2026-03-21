package me.duncanruns.reroller.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.duncanruns.reroller.RNGManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(EyeOfEnderEntity.class)
public abstract class EyeOfEnderEntityMixin extends Entity {
    public EyeOfEnderEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "moveTowards", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", remap = false))
    private int replaceRandom(Random instance, int i, Operation<Integer> original) {
        return original.call(RNGManager.getRandom(getServer(), "eye_drops"), i);
    }
}
