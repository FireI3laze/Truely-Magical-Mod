package com.fireblaze.magic_overhaul.runes;

import net.minecraft.world.level.block.Block;

public class RuneLoot {
    public final String[] chestLootTables;
    public final float chance;

    public RuneLoot(String[] chestLootTables, float chance) {
        this.chestLootTables = chestLootTables;
        this.chance = chance;
    }
}
