package com.fireblaze.magic_overhaul.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class PlayerSettings {

    private static final String TAG_MOD_SETTINGS = "magic_overhaul_settings";

    public static void saveBoolean(Player player, String key, boolean value) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag modTag;
        if (persistent.contains(TAG_MOD_SETTINGS)) {
            modTag = persistent.getCompound(TAG_MOD_SETTINGS);
        } else {
            modTag = new CompoundTag();
        }

        modTag.putBoolean(key, value);
        persistent.put(TAG_MOD_SETTINGS, modTag);
    }

    public static boolean loadBoolean(Player player, String key, boolean defaultValue) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(TAG_MOD_SETTINGS)) return defaultValue;

        CompoundTag modTag = persistent.getCompound(TAG_MOD_SETTINGS);
        return modTag.contains(key) ? modTag.getBoolean(key) : defaultValue;
    }
}
