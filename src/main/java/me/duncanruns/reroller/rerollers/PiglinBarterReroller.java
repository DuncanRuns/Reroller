package me.duncanruns.reroller.rerollers;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public class PiglinBarterReroller extends ItemReroller {
    private PiglinEntity piglin;

    @Override
    protected void init(MinecraftServer server) {
        piglin = EntityType.PIGLIN.create(server.getWorlds().iterator().next());
    }

    @Override
    protected Collection<ItemStack> getNext(MinecraftServer server, Random random) {
        LootTable lootTable = Objects.requireNonNull(piglin.world.getServer()).getLootManager().getTable(LootTables.PIGLIN_BARTERING_GAMEPLAY);
        return lootTable.generateLoot((new LootContext.Builder((ServerWorld) piglin.world)).parameter(LootContextParameters.THIS_ENTITY, piglin).random(random).build(LootContextTypes.BARTER));
    }

    @Override
    public String getDisplayName() {
        return "Barters";
    }
}
