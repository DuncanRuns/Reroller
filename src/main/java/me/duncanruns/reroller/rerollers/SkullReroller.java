package me.duncanruns.reroller.rerollers;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class SkullReroller extends ItemReroller {
    private final float chance;

    public SkullReroller(int looting) {
        this.chance = 0.025f + 0.01f * looting;
    }

    @Override
    protected Collection<ItemStack> getNext(MinecraftServer server, Random random) {
        float val = random.nextFloat();
        return val < chance ? Collections.singletonList(new ItemStack(Items.WITHER_SKELETON_SKULL, 1)) : Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return "Wither Skeleton Skulls";
    }
}
