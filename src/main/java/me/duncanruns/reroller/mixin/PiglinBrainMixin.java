package me.duncanruns.reroller.mixin;

import me.duncanruns.reroller.RNGManager;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
    /**
     * @author DuncanRuns
     * @reason Standardize Piglin Barters
     */
    @Overwrite
    private static List<ItemStack> getBarteredItem(PiglinEntity piglin) {
        MinecraftServer server = piglin.getServer();
        assert server != null;
        Identifier lootTableId = LootTables.PIGLIN_BARTERING_GAMEPLAY;
        LootTable lootTable = server.getLootManager().getTable(lootTableId);
        return lootTable.generateLoot(
                new LootContext.Builder((ServerWorld) piglin.world)
                        .parameter(LootContextParameters.THIS_ENTITY, piglin)
                        .random(RNGManager.getRandom(server, lootTableId.toString()))
                        .build(LootContextTypes.BARTER)
        );
    }
}
