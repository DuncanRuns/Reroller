package me.duncanruns.reroller;

import me.duncanruns.reroller.mixinint.RerollerServer;
import me.duncanruns.reroller.mixinint.RerollerTagOwner;
import me.duncanruns.reroller.random.CountedRandom;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class SpawnerManager {
    public static final short SPAWN_TIME_T0 = Short.MAX_VALUE / 2;

    private final RNGManager rngManager;
    private final Map<String, Integer> nextSpawnTimes = new HashMap<>();

    public SpawnerManager(RNGManager rngManager) {
        this.rngManager = rngManager;
    }

    public static SpawnerManager get(MinecraftServer server) {
        return ((RerollerServer) server).reroller$getSpawnerManager();
    }

    public boolean shouldSpawn(String entityName, int spawnDelay) {
        int nextSpawn = nextSpawnTimes.computeIfAbsent(entityName, s ->
                rerollNextSpawnTime(entityName)
        );
        if (spawnDelay > nextSpawn) return false;
        int overshot = nextSpawn - spawnDelay;
        nextSpawnTimes.put(entityName, rerollNextSpawnTime(entityName) + overshot);
        return true;
    }

    private int rerollNextSpawnTime(String entityName) {
        return SPAWN_TIME_T0 - (getSpawnerCooldownRandom(entityName).nextInt(600));
    }

    public CountedRandom getSpawnerCooldownRandom(String entityName) {
        return rngManager.getRandom("spawner/cooldown/" + entityName);
    }

    public void load(MinecraftServer server) {
        CompoundTag tag = ((RerollerTagOwner) server.getSaveProperties()).reroller$getTag();
        if (!tag.contains("SpawnerManager")) return;
        CompoundTag spawnerTag = tag.getCompound("SpawnerManager");
        spawnerTag.getKeys().forEach(s -> nextSpawnTimes.put(s, spawnerTag.getInt(s)));
    }

    public CompoundTag getTag() {
        CompoundTag tag = new CompoundTag();
        nextSpawnTimes.forEach(tag::putInt);
        return tag;
    }

    public int getCount(String entityId) {
        return getSpawnerCooldownRandom(entityId).getCount();
    }
}
