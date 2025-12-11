package com.fireblaze.magic_overhaul.util;

import com.fireblaze.magic_overhaul.MagicOverhaul;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {

    public static class Items {
        public static final TagKey<Item> RUNES =
                TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MagicOverhaul.MODID, "runes"));
    }
}
