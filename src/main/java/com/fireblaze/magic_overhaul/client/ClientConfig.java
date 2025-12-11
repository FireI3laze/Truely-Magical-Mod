package com.fireblaze.magic_overhaul.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ClientConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ClientConfig INSTANCE;

    // UI / Preferences (persisted)
    public boolean showBlocklist = true;
    public boolean sortAscending = true;

    // which controller (by displayName) is selected / visible per side
    public String selectedControllerLeft = null;
    public String selectedControllerRight = null;

    // whether the whole side is visible (toggle)
    public boolean sideLeftVisible = true;
    public boolean sideRightVisible = true;

    // magic bar visual options
    public boolean magicBarMotion = false;
    public boolean magicBarSparkle = true;

    // path within .minecraft/config/...
    private static final Path CONFIG_PATH = Minecraft.getInstance().gameDirectory
            .toPath()
            .resolve("config")
            .resolve("magic_overhaul_client_config.json");

    public static ClientConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (!file.exists()) {
            INSTANCE = new ClientConfig();
            save();
            System.out.println("[MagicOverhaul] Created default client config.");
            return;
        }

        try (FileReader r = new FileReader(file)) {
            INSTANCE = GSON.fromJson(r, ClientConfig.class);
            if (INSTANCE == null) INSTANCE = new ClientConfig();
            System.out.println("[MagicOverhaul] Loaded client config.");
        } catch (Exception e) {
            System.err.println("[MagicOverhaul] Failed to load client config — using defaults.");
            e.printStackTrace();
            INSTANCE = new ClientConfig();
        }
    }

    public static void save() {
        try {
            File file = CONFIG_PATH.toFile();
            file.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(file)) {
                GSON.toJson(get(), w);
            }
            System.out.println("[MagicOverhaul] Saved client config.");
        } catch (Exception e) {
            System.err.println("[MagicOverhaul] Failed to save client config.");
            e.printStackTrace();
        }
    }

    /** convenience: set & persist */
    public void saveNow() {
        INSTANCE = this;
        save();
    }
}
