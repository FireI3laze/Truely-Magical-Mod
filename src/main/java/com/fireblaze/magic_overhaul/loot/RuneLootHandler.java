package com.fireblaze.magic_overhaul.loot;

import com.fireblaze.magic_overhaul.MagicOverhaul;
import com.fireblaze.magic_overhaul.registry.ModRunes;
import com.fireblaze.magic_overhaul.runes.RuneLoot;
import com.fireblaze.magic_overhaul.runes.RuneType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = MagicOverhaul.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RuneLootHandler {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        if (table == null) return;

        ResourceLocation id = event.getName();
        if (id == null) return;

        for (RuneType rune : RuneType.values()) {

            if (rune.runeLoot == null) continue;

            RuneLoot loot = rune.runeLoot;

            if (loot.chestLootTables == null) continue;

            for (String lootName : loot.chestLootTables) {

                String[] parts = lootName.split(":", 2);
                if (parts.length != 2) continue;

                String namespace = parts[0];
                String path = parts[1];

                ResourceLocation target = ResourceLocation.fromNamespaceAndPath(namespace, path);

                if (!id.equals(target)) continue;

                LootPool pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(
                                LootItem.lootTableItem(Objects.requireNonNull(ModRunes.getItemFromType(rune)))
                                        .when(LootItemRandomChanceCondition.randomChance(loot.chance))
                        )
                        .build();

                table.addPool(pool);

                MagicOverhaul.LOGGER.info(
                        "[Runes] → Rune '{}' in LootTable '{}' injected with chance {}",
                        rune.id, lootName, loot.chance
                );
            }
        }
    }
}

