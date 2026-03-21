package me.duncanruns.reroller.rerollers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class GravelReroller extends ItemReroller {
    @Override
    protected Collection<ItemStack> getNext(MinecraftServer server, Random random) {
        if (random.nextFloat() < 0.1f) {
            return Collections.singletonList(new ItemStack(Items.FLINT, 1));
        }
        return Collections.singletonList(new ItemStack(Items.GRAVEL, 1));
    }

    @Override
    public String getDisplayName() {
        return "Gravel";
    }
}
