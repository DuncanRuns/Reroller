package me.duncanruns.reroller.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import me.duncanruns.reroller.RNGManager;
import me.duncanruns.reroller.rerollers.Reroller;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelLoadingScreen.class)
// Naughty client side accessing server side! Storing things in local vars and checking nullability should prevent any issues.
public abstract class LevelLoadingScreenMixin extends Screen {
    protected LevelLoadingScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"))
    private void replaceText(CallbackInfo ci, @Local LocalRef<String> text) {
        assert client != null;
        IntegratedServer server = client.getServer();
        if (server == null) return; // Shouldn't happen
        RNGManager rngManager = RNGManager.get(server);
        if (rngManager == null) return; // Might happen
        Reroller reroller = rngManager.getCurrentlyRerolling();
        if (reroller == null) return;

        text.set(String.format("Rerolling %s (%s)", reroller.getDisplayName(), reroller.getProgressText()));
    }
}
