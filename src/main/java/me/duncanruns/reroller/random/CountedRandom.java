package me.duncanruns.reroller.random;

import net.minecraft.nbt.CompoundTag;

import java.util.Random;

public class CountedRandom extends Random {

    protected int nextCount;
    protected long initialSeed;

    public CountedRandom(long initialSeed, int count) {
        this.nextCount = 0;
        setSeed(initialSeed, count);
    }

    public void setSeed(long seed, int count) {
        setSeed(seed);
        this.nextCount = 0;
        this.initialSeed = seed;
        for (int i = 0; i < count; i++) {
            next(1);
        }
    }

    @Override
    protected int next(int bits) {
        this.nextCount++;
        return super.next(bits);
    }

    public int getCount() {
        return nextCount;
    }

    public long getInitialSeed() {
        return initialSeed;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("initialSeed", initialSeed);
        tag.putInt("count", nextCount);
        return tag;
    }

    public static CountedRandom fromTag(CompoundTag tag) {
        return new CountedRandom(tag.getLong("initialSeed"), tag.getInt("count"));
    }

    public CountedRandom copy() {
        return new CountedRandom(initialSeed, nextCount);
    }
}