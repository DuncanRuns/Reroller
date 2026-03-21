package me.duncanruns.reroller.rerollers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class EyeBreaksReroller extends ItemReroller {
    @Override
    protected Collection<ItemStack> getNext(MinecraftServer server, Random random) {
        return random.nextInt(5) == 0 ? Collections.emptyList() : Collections.singletonList(new ItemStack(Items.ENDER_EYE, 1));
    }

    @Override
    public String getDisplayName() {
        return "Eye Drops";
    }
}
