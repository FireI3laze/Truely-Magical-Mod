package com.fireblaze.magic_overhaul.villager;

import com.fireblaze.magic_overhaul.MagicOverhaul;
import com.fireblaze.magic_overhaul.registry.ModRunes;
import com.fireblaze.magic_overhaul.runes.RuneType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

@Mod.EventBusSubscriber(modid = MagicOverhaul.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerTradeHandler {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

        for (RuneType rune : RuneType.values()) {
            if (rune.runeTrade.profession == null || rune.runeTrade.professionLevel <= 0) continue;

            // Prüfen, ob der aktuelle Villager-Typ passt
            if (event.getType() == rune.runeTrade.profession) {
                int level = rune.runeTrade.professionLevel;

                // Sicherstellen, dass es eine Trade-Liste für diese Stufe gibt
                trades.computeIfAbsent(level, k -> new java.util.ArrayList<>());

                // Trade hinzufügen: 3 Emeralds -> 1 Rune (du kannst Preis/Xp/MaxUses anpassen)
                trades.get(level).add((trader, random) -> new MerchantOffer(
                        new net.minecraft.world.item.ItemStack(Items.EMERALD, rune.runeTrade.price),
                        new net.minecraft.world.item.ItemStack(ModRunes.RUNES.get(rune).get(), 1),
                        12,  // maxUses
                        5,   // villager XP
                        0.05f // priceMultiplier
                ));
            }
        }

        if (event.getType() != VillagerProfession.LIBRARIAN) return;

        // Alle bestehenden Trades löschen
        trades.forEach((level, tradeList) -> tradeList.clear());
        // LEVEL 1:
        trades.get(1).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.PAPER, 24),
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 1),
                16,
                2,
                0.05f
        ));

        trades.get(1).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 9),
                new net.minecraft.world.item.ItemStack(Items.BOOKSHELF, 1),
                12,
                2,
                0.05f
        ));

        // LEVEL 2:
        trades.get(2).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.BOOK, 4),
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 1),
                12,
                5,
                0.05f
        ));

        trades.get(2).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 1),
                new net.minecraft.world.item.ItemStack(Items.LANTERN, 1),
                12,
                5,
                0.05f
        ));

        // LEVEL 3: 10 Emeralds -> 8 Glass Pane
        trades.get(3).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 1),
                new net.minecraft.world.item.ItemStack(Items.GLASS, 4),
                12,
                10,
                0.05f
        ));

        trades.get(3).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.INK_SAC, 5),
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 1),
                12,
                10,
                0.05f
        ));

        // LEVEL 4:
        trades.get(4).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.WRITABLE_BOOK, 1),
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 1),
                12,
                15,
                0.05f
        ));

        trades.get(4).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 4),
                new net.minecraft.world.item.ItemStack(Items.COMPASS, 1),
                12,
                15,
                0.05f
        ));

        trades.get(4).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 5),
                new net.minecraft.world.item.ItemStack(Items.CLOCK, 1),
                12,
                15,
                0.05f
        ));

        // LEVEL 5:
        trades.get(5).add((trader, random) -> new MerchantOffer(
                new net.minecraft.world.item.ItemStack(Items.EMERALD, 20),
                new net.minecraft.world.item.ItemStack(Items.NAME_TAG, 1),
                12,
                20,
                0.05f
        ));
    }
}
