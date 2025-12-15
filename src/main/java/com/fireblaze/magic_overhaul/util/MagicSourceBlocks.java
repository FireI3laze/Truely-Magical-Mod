package com.fireblaze.magic_overhaul.util;

import net.minecraft.world.level.block.Block;

public class MagicSourceBlocks {
    public final Block block;
    public int magicPower; // Magiekraft pro Block
    public int magicCap;   // Maximalwert erreichbarer Magiekraft über diesen Block

    public MagicSourceBlocks(Block block, int magicPower, int magicCap) {
        this.block = block;
        this.magicPower = magicPower;
        this.magicCap = magicCap;
    }
}
