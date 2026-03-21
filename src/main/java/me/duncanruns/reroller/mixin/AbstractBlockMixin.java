package me.duncanruns.reroller.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.duncanruns.reroller.RNGManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
    // Implementation warning: injecting at the invoke of builder.build and LocalRef replacing the builder works in dev
    // but not in prod, so things are a little funky around here.
    @WrapOperation(method = "getDroppedStacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext$Builder;build(Lnet/minecraft/loot/context/LootContextType;)Lnet/minecraft/loot/context/LootContext;"))
    private LootContext replaceRandom(LootContext.Builder instance, LootContextType type, Operation<LootContext> original, @Local Identifier lootTableId) {
        return instance.random(RNGManager.getRandom(instance.getWorld().getServer(), lootTableId.toString())).build(type);
    }

}
