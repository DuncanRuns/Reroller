package me.duncanruns.reroller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.duncanruns.reroller.rerollers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.LootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class RerollerMod implements ModInitializer {
    public static final String MOD_ID = "reroller";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static String VERSION = "";

    public static final Map<String, JsonObject> REROLLER_CONFIGS = new HashMap<>();
    public static final Map<String, Supplier<Reroller>> REROLLERS = new HashMap<>();
    public static final String[] SKULL_REROLLERS = new String[4];

    static {
        REROLLERS.put(LootTables.PIGLIN_BARTERING_GAMEPLAY.toString(), PiglinBarterReroller::new);
        REROLLERS.put("minecraft:entities/blaze", BlazeReroller::new);
        REROLLERS.put("minecraft:entities/iron_golem", IronGolemReroller::new);
        REROLLERS.put("minecraft:blocks/gravel", GravelReroller::new);
        REROLLERS.put("eye_drops", EyeBreaksReroller::new);
    }

    public static int getLooting(Entity entity) {
        if (!(entity instanceof LivingEntity)) return 0;
        return EnchantmentHelper.getLooting((LivingEntity) entity);
    }

    public static int getLootingForSkullRngId(String key) {
        return Arrays.asList(SKULL_REROLLERS).indexOf(key);
    }

    @Override
    public void onInitialize() {
        VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).map(m -> m.getMetadata().getVersion().getFriendlyString()).orElseThrow(() -> new RuntimeException("Failed to get own mod version!"));

        try {
            loadConfig();
        } catch (IOException e) {
            LOGGER.error("Failed to load config!", e);
        }
    }

    private static void loadConfig() throws IOException {
        clearConfig();
        String config = getConfigFileString();
        if (config == null) return;
        loadConfig(config);
    }

    private static @Nullable String getConfigFileString() throws IOException {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("reroller.json");
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath.getParent());
            Files.write(configPath, "{}".getBytes());
            return null;
        }
        return new String(Files.readAllBytes(configPath));
    }

    private static void loadConfig(String config) {
        JsonObject configJson = new Gson().fromJson(config, JsonObject.class);
        if (configJson.has("skull_rerollers"))
            loadSkullConfig(configJson.getAsJsonObject("skull_rerollers"));
        if (configJson.has("rerollers"))
            loadRerollersConfig(configJson.getAsJsonObject("rerollers"));
    }

    private static void clearConfig() {
        REROLLER_CONFIGS.clear();
        IntStream.range(0, 4).forEach(i -> SKULL_REROLLERS[i] = "skulls/" + i);
    }

    private static void loadRerollersConfig(JsonObject rerollers) {
        rerollers.entrySet().forEach(e -> {
            String key = e.getKey();
            Reroller reroller = getReroller(key);
            if (reroller == null) return;
            // Test config on a sample reroller object to make sure it's valid, no side effects should be caused by
            // Reroller#loadConfig in this context.
            boolean loadSuccess;
            try {
                loadSuccess = reroller.loadConfig(e.getValue().getAsJsonObject());
            } catch (Exception ex) {
                LOGGER.error("Failed to load config for {}", key, ex);
                return;
            }
            if (!loadSuccess) {
                LOGGER.warn("Failed to load config for {}", key);
                return;
            }
            REROLLER_CONFIGS.put(key, e.getValue().getAsJsonObject());
            LOGGER.info("Loaded config for {}: {}", key, e.getValue());
        });
    }

    private static @Nullable Reroller getReroller(String key) {
        Reroller reroller;
        if (Arrays.asList(SKULL_REROLLERS).contains(key)) {
            reroller = new SkullReroller(getLootingForSkullRngId(key));
        } else {
            if (!REROLLERS.containsKey(key)) {
                LOGGER.warn("No reroller found for {}", key);
                return null;
            }
            reroller = REROLLERS.get(key).get();
        }
        return reroller;
    }

    private static void loadSkullConfig(JsonObject skullRerollers) {
        skullRerollers.entrySet().forEach(e -> {
            try {
                int looting = Integer.parseInt(e.getKey());
                if (looting < 0 || looting > 3) throw new IllegalArgumentException("Looting must be between 0 and 3!");
                SKULL_REROLLERS[looting] = e.getValue().getAsString();
            } catch (Exception ex) {
                LOGGER.error("Keys in skull_rerollers must be an integer between 0 and 3!", ex);
            }
        });
    }

}