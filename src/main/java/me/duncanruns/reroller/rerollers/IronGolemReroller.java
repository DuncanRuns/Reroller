package me.duncanruns.reroller.rerollers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class IronGolemReroller extends ItemReroller {
    @Override
    protected Collection<ItemStack> getNext(MinecraftServer server, Random random) {
        int poppies = random.nextInt(3);
        int ingots = random.nextInt(3) + 3;
        return Arrays.asList(new ItemStack(Items.IRON_INGOT, ingots), new ItemStack(Items.POPPY, poppies));
    }

    @Override
    public String getDisplayName() {
        return "Iron Golems";
    }
}
