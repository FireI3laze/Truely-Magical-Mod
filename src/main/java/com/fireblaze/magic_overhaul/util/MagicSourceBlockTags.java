package com.fireblaze.magic_overhaul.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MagicSourceBlockTags {
    public final TagKey<Block> tag;
    public int magicPower;
    public int magicCap;

    public MagicSourceBlockTags(TagKey<Block> tag, int magicPower, int magicCap) {
        this.tag = tag;
        this.magicPower = magicPower;
        this.magicCap = magicCap;
    }

    /**
     * Wandelt den Tag in ein RuneBlock-Objekt um, damit es im Scan
     * wie ein normaler Block behandelt werden kann.
     * Der Block selbst ist hier null, weil es für Tags keinen spezifischen Block gibt.
     */
    public MagicSourceBlocks toMagicSourceBlock() {
        return new MagicSourceBlocks(null, this.magicPower, this.magicCap);
    }
}
