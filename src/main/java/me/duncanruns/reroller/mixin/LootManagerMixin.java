package me.duncanruns.reroller.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import me.duncanruns.reroller.RerollerMod;
import net.minecraft.loot.LootManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootManager.class)
public abstract class LootManagerMixin {
    @Unique
    private static final Identifier WITHER_SKELETON_LOOT_ID = new Identifier("minecraft:entities/wither_skeleton");

    @Inject(method = /*lambda method*/ "method_20711", at = @At("HEAD"))
    private static void removeVanillaSkulls(CallbackInfo ci, @Local(argsOnly = true) Identifier identifier, @Local(argsOnly = true) JsonElement jsonElement) {
        if (!identifier.equals(WITHER_SKELETON_LOOT_ID)) return;
        jsonElement.getAsJsonObject().getAsJsonArray("pools").remove(2);
        RerollerMod.LOGGER.info("Removed vanilla wither skeleton skulls");
    }
}
