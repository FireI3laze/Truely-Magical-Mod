package com.fireblaze.magic_overhaul.registry;

import com.fireblaze.magic_overhaul.MagicOverhaul;
import com.fireblaze.magic_overhaul.runes.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;

public class ModRunes {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MagicOverhaul.MODID);

    public static final EnumMap<RuneType, RegistryObject<Item>> RUNES =
            new EnumMap<>(RuneType.class);

    static {
        for (RuneType type : RuneType.values()) {
            RUNES.put(type, ITEMS.register(
                    type.id + "_rune",
                    () -> new RuneItem(type)
            ));
        }
    }

    public static Item getItemFromType(RuneType type) {
        RegistryObject<Item> ro = RUNES.get(type);
        return ro != null ? ro.get() : null;
    }
}
