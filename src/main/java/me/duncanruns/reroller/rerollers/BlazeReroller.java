package me.duncanruns.reroller.rerollers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class BlazeReroller extends ItemReroller {
    @Override
    protected Collection<ItemStack> getNext(MinecraftServer server, Random random) {
        if (random.nextInt(2) == 1) {
            return Collections.singletonList(new ItemStack(Items.BLAZE_ROD, 1));
        }
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return "Blazes";
    }
}
