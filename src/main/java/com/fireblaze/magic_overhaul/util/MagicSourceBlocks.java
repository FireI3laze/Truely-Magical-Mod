package com.fireblaze.magic_overhaul.util;

import net.minecraft.world.level.block.Block;

public class MagicSourceBlocks {
    public final Block block;
    public final int magicPower; // Magiekraft pro Block
    public final int magicCap;   // Maximalwert erreichbarer Magiekraft über diesen Block

    public MagicSourceBlocks(Block block, int magicPower, int magicCap) {
        this.block = block;
        this.magicPower = magicPower;
        this.magicCap = magicCap;
    }
}
