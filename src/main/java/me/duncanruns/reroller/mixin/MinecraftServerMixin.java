package me.duncanruns.reroller.mixin;

import me.duncanruns.reroller.RNGManager;
import me.duncanruns.reroller.SpawnerManager;
import me.duncanruns.reroller.mixinint.RerollerServer;
import me.duncanruns.reroller.mixinint.RerollerTagOwner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements RerollerServer {
    @Unique
    private RNGManager rngManager;

    @Unique
    private SpawnerManager spawnerManager;

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void loadRerollerData(CallbackInfo ci) {
        @SuppressWarnings("DataFlowIssue")
        MinecraftServer thisServer = (MinecraftServer) (Object) this;

        rngManager = new RNGManager((thisServer).getSaveProperties().getGeneratorOptions().getSeed());
        rngManager.load(thisServer);

        spawnerManager = new SpawnerManager(rngManager);
        spawnerManager.load(thisServer);
    }

    @Inject(method = "save(ZZZ)Z", at = @At("HEAD"))
    private void saveRerollerData(CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer thisServer = (MinecraftServer) (Object) this;
        CompoundTag rerollerTag = new CompoundTag();
        rerollerTag.put("RNGManager", rngManager.getTag());
        rerollerTag.put("SpawnerManager", spawnerManager.getTag());
        ((RerollerTagOwner) thisServer.getSaveProperties()).reroller$setTag(rerollerTag);
    }

    @Inject(method = "tickWorlds", at = @At("HEAD"))
    private void reportFailedRerollers(CallbackInfo ci) {
        MinecraftServer thisServer = (MinecraftServer) (Object) this;
        rngManager.tick(thisServer);
    }


    @Override
    public RNGManager reroller$getRNGManager() {
        return rngManager;
    }

    @Override
    public SpawnerManager reroller$getSpawnerManager() {
        return spawnerManager;
    }
}