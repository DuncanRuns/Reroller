package me.duncanruns.reroller.mixinint;

import net.minecraft.nbt.CompoundTag;

public interface RerollerTagOwner {
    CompoundTag reroller$getTag();

    void reroller$setTag(CompoundTag tag);
}
