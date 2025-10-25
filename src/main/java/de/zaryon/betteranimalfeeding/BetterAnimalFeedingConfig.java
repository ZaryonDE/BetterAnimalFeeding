package de.zaryon.betteranimalfeeding;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BetterAnimalFeedingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/betteranimalfeeding.json");

    public double forwardRadius = 2.0;

    public static BetterAnimalFeedingConfig INSTANCE = new BetterAnimalFeedingConfig();

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            BetterAnimalFeeding.LOGGER.info("Config created: {}", CONFIG_FILE.getAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            BetterAnimalFeedingConfig loaded = GSON.fromJson(reader, BetterAnimalFeedingConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
                BetterAnimalFeeding.LOGGER.info("Config loaded: {}", CONFIG_FILE.getAbsolutePath());
            }
        } catch (IOException e) {
            BetterAnimalFeeding.LOGGER.error("Failed to load config!", e);
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            BetterAnimalFeeding.LOGGER.error("Failed to save config!", e);
        }
    }
}