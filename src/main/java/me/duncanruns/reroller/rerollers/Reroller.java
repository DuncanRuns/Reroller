package me.duncanruns.reroller.rerollers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;

import java.util.OptionalLong;
import java.util.Random;

/**
 * A "reroller" is an object that defines how seeding for certain loot types are to be rerolled.
 * <p>
 * A standard rerolling logic is included in the abstract class such that implementations only need to define how items
 * are generated from the Random object.
 */
public abstract class Reroller {
    protected static final Gson GSON = new Gson();

    protected long maxTries;
    protected long currentTry;

    public OptionalLong getRerolledSeed(MinecraftServer server, long mixedSeed, JsonObject configEntry) {
        if (!loadConfig(configEntry)) return OptionalLong.empty();
        Random random = new Random(mixedSeed);
        init(server);
        for (currentTry = 0; currentTry < maxTries; currentTry++) {
            long seed = random.nextLong();
            if (test(server, seed)) {
                return OptionalLong.of(seed);
            }
        }
        return OptionalLong.empty();
    }

    /**
     * Called once before any rerolling is done. Useful for setting up entities needed for generating items or other
     * state.
     */
    protected void init(MinecraftServer server) {
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean loadConfig(JsonObject configEntry) {
        if (!configEntry.has("maxTries")) return false;
        maxTries = configEntry.get("maxTries").getAsLong();
        return true;
    }

    protected abstract boolean test(MinecraftServer server, long seed);

    /**
     * Should not come with side effects as it may be accessed from a different thread (client)
     */
    public String getProgressText() {
        return String.format("%.2f%%", (currentTry / (double) maxTries) * 100);
    }

    public abstract String getDisplayName();
}
