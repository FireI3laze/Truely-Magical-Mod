package com.fireblaze.magic_overhaul.client.screen;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.MagicAccumulator;
import com.fireblaze.magic_overhaul.client.screen.utils.*;
import com.fireblaze.magic_overhaul.menu.ArcaneEnchantingTableMenu;
import com.fireblaze.magic_overhaul.network.Network;
import com.fireblaze.magic_overhaul.network.SetEnchantSelectionPacket;
import com.fireblaze.magic_overhaul.util.MagicCostCalculator;
import com.fireblaze.magic_overhaul.util.MagicSourceBlocks;
import com.fireblaze.magic_overhaul.util.MagicSourceBlockTags;
import com.fireblaze.magic_overhaul.util.PlayerSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ArcaneEnchantingTableScreen extends AbstractContainerScreen<ArcaneEnchantingTableMenu> {

    public ArcaneEnchantingTableScreen(ArcaneEnchantingTableMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }


    private int scrollOffset = 0;
    private final int rowHeight = 20;
    private int mouseX;
    private int mouseY;

    // Blockliste
    private int blockScrollOffset = 0;
    private final int blockRowHeight = 20;
    private final int blockListWidth = 140;

    private MagicAccumulator acc;
    private BlocklistScreen blocklistScreen;
    private MagicPowerBar magicPowerBar;
    private EnchantmentListScreen enchantmentListScreen;
    private CustomButton toggleButton;
    private CustomButton irgendEinZweiterButton;
    private boolean showBlocklist = false;

    @Override
    protected void init() {
        super.init();

        acc = menu.getBlockEntity().getMagicAccumulator();

        ScreenController blocklistController = new ScreenController(
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                rowHeight,
                blockListWidth,
                ScreenSide.LEFT
        );

        blocklistScreen = new BlocklistScreen(
                blocklistController.getListX(),
                blocklistController.getListY(),
                blocklistController.getListWidth(),
                imageHeight,
                rowHeight,
                font,
                acc,
                acc.getBlockPalette(),
                acc.getTagPalette()
        );

        int barWidth = 400;

        ScreenController topController = new ScreenController(
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                rowHeight,
                barWidth,
                ScreenSide.TOP
        );

        magicPowerBar = new MagicPowerBar(topController, font, barWidth, acc.getMagicPowerCapPerPlayerSoft(), 100000); // todo getter für hard cap

        assert Objects.requireNonNull(minecraft).player != null;
        assert minecraft.player != null;
        magicPowerBar.setMotion(PlayerSettings.loadBoolean(minecraft.player, "magicBarMotion", false));
        magicPowerBar.setSparkle(PlayerSettings.loadBoolean(minecraft.player, "magicBarSparkle", true));

        assert minecraft != null;
        showBlocklist = PlayerSettings.loadBoolean(minecraft.player, "showBlocklist", false);

        toggleButton = new CustomButton(
                leftPos - 14, topPos + 4,
                leftPos + 4, topPos + 30,
                ">", "<",
                0x00000000, 0x00000000, 0xFFAA0FFF,
                font,
                state -> {
                    if (state) {
                        showBlocklist = true;

                    }
                    else showBlocklist = false;
                    PlayerSettings.saveBoolean(minecraft.player, "showBlocklist", state);
                }
        );
        toggleButton.setToggled(showBlocklist);

        ScreenController enchantmentController = new ScreenController(
                leftPos, topPos, imageWidth, imageHeight, rowHeight, 130, ScreenSide.LEFT
        );

        enchantmentListScreen = new EnchantmentListScreen(menu, font, enchantmentController);
    }



    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.renderTooltip(gui, mouseX, mouseY);

        toggleButton.render(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int x, int y) {
        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;

        enchantmentListScreen.render(gui, mouseX, mouseY);

        //renderEnchantmentList(gui, left, top);

        if (showBlocklist)
            blocklistScreen.render(gui, mouseX, mouseY);

        int plannedCost = calculateSelectedMagicCost();
        magicPowerBar.setPlannedConsumption(plannedCost);
        magicPowerBar.setAccumulatedMagicPower(acc.getAccumulatedMagicPower());
        magicPowerBar.setCurrentMagicPowerIncreaseRate(acc.getCurrentMagicPowerIncreaseRate());
        magicPowerBar.render(gui, mouseX, mouseY);
    }

    private void renderEnchantmentList(GuiGraphics gui, int left, int top) {
        Map<Enchantment, Integer> selected = menu.getSelectedEnchantments();
        ItemStack targetItem = menu.getItemInSlot0();
        Map<Enchantment, Integer> unlocked = menu.getUnlockedEnchantments();

        int listX = left + imageWidth + 6;
        int listY = top + 4;
        int visibleRows = imageHeight / rowHeight;

        // Dynamische Sichtliste nach Kompatibilität erstellen
        var visibleEntries = unlocked.entrySet().stream()
                .filter(entry -> targetItem.isEmpty() || entry.getKey().canEnchant(targetItem))
                .filter(entry -> targetItem.isEmpty() || targetItem.getEnchantmentLevel(entry.getKey()) < entry.getValue())
                .filter(entry -> {
                    boolean compatible = true;
                    for (Enchantment alreadySelected : selected.keySet()) {
                        if (alreadySelected == entry.getKey()) continue;
                        if (selected.get(alreadySelected) > 0 &&
                                !entry.getKey().isCompatibleWith(alreadySelected)) {
                            compatible = false;
                            break;
                        }
                    }
                    return compatible;
                })
                .toList();

        // ScrollOffset korrigieren, falls nötig
        int totalRows = visibleEntries.size();
        if (scrollOffset > totalRows - visibleRows)
            scrollOffset = Math.max(totalRows - visibleRows, 0);

        for (int i = scrollOffset; i < Math.min(scrollOffset + visibleRows, totalRows); i++) {
            var entry = visibleEntries.get(i);
            Enchantment ench = entry.getKey();
            int maxLevel = entry.getValue();
            int selectedLevel = selected.getOrDefault(ench, 0);
            int currentOnItem = targetItem.getEnchantmentLevel(ench);

            int y = listY + (i - scrollOffset) * rowHeight;

            int bgColor = selectedLevel > 0 ? 0xA028143C : 0x44000000;
            gui.fill(listX, y, listX + 130, y + rowHeight, bgColor);

            // Hover Effekt
            if (isMouseOver(mouseX, mouseY, listX, y, 130, rowHeight)) {
                int borderColor = 0x5FAA0FFF;
                int thickness = 1;

                gui.fill(listX, y, listX + 130, y + thickness, borderColor);
                gui.fill(listX, y + rowHeight - thickness, listX + 130, y + rowHeight, borderColor);
                gui.fill(listX, y, listX + thickness, y + rowHeight, borderColor);
                gui.fill(listX + 130 - thickness, y, listX + 130, y + rowHeight, borderColor);
            }

            gui.drawString(font, ench.getFullname(maxLevel).getString(), listX + 20, y + 2, 0xFFFFFF);

            ItemStack bookStack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(bookStack, new EnchantmentInstance(ench, currentOnItem + 1));
            gui.renderItem(bookStack, listX + 2, y + 2);

            if (selectedLevel > 0 && maxLevel > 1) {
                renderSlider(gui, ench, selectedLevel, maxLevel, currentOnItem, listX + 20, y + 12);
            }
        }
    }

    // Hilfsmethode
    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }


    private void renderSlider(GuiGraphics gui, Enchantment ench, int current, int max, int currentOnItem, int x, int y) {
        int sliderWidth = 80;

        // Positionen in Pixel
        int blockedWidth = (int)(sliderWidth * (currentOnItem / (float) max));
        int filledWidth = (int)(sliderWidth * (current / (float) max));

        // Hintergrund
        gui.fill(x, y, x + sliderWidth, y + 6, 0xFF555555);

        // Blockierte Levels dunkler
        if (blockedWidth > 0)
            gui.fill(x, y, x + blockedWidth, y + 6, 0x88462878); // 0xCC9678C8

        // Gewählte Levels hellgrün
        if (filledWidth > blockedWidth)
            gui.fill(x + blockedWidth, y, x + filledWidth, y + 6, 0x889678C8); // 0x88446688
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (enchantmentListScreen.mouseScrolled(mouseX, mouseY, delta)) return true;
        if (blocklistScreen.mouseScrolled(mouseX, mouseY, delta)) return true;

        /*
        Map<Enchantment, Integer> selected = menu.getSelectedEnchantments();
        ItemStack targetItem = menu.getItemInSlot0();
        Map<Enchantment, Integer> unlocked = menu.getUnlockedEnchantments();

        int listX = (width - imageWidth) / 2 + imageWidth + 6;
        int listY = (height - imageHeight) / 2 + 4;
        int visibleRows = imageHeight / rowHeight;

        // Sichtbare, kompatible Liste berechnen
        var visibleEntries = unlocked.entrySet().stream()
                .filter(entry -> targetItem.isEmpty() || entry.getKey().canEnchant(targetItem))
                // nur anzeigen, wenn noch nicht auf maxLevel
                .filter(entry -> targetItem.isEmpty() || targetItem.getEnchantmentLevel(entry.getKey()) < entry.getValue())
                .filter(entry -> {
                    boolean compatible = true;
                    for (Enchantment alreadySelected : selected.keySet()) {
                        if (alreadySelected == entry.getKey()) continue;
                        if (selected.get(alreadySelected) > 0 &&
                                !entry.getKey().isCompatibleWith(alreadySelected)) {
                            compatible = false;
                            break;
                        }
                    }
                    return compatible;
                })
                .toList();

        int totalRows = visibleEntries.size();

        // Prüfen, ob Maus über ausgewähltem Enchantment ist
        int rowIndex = 0;
        for (int i = scrollOffset; i < Math.min(scrollOffset + visibleRows, totalRows); i++) {
            var entry = visibleEntries.get(i);
            Enchantment ench = entry.getKey();
            int maxLevel = entry.getValue();
            int y = listY + rowIndex * rowHeight;

            if (mouseX >= listX && mouseX <= listX + 130 &&
                    mouseY >= y && mouseY <= y + rowHeight &&
                    selected.getOrDefault(ench, 0) > 0 &&
                    !targetItem.isEmpty()) {

                int currentLevel = selected.get(ench);
                int currentOnItem = targetItem.getEnchantmentLevel(ench);
                int newLevel = currentLevel + (delta > 0 ? 1 : -1);

                if (newLevel < 1) newLevel = 1;
                if (newLevel > maxLevel) newLevel = maxLevel;
                if (newLevel <= currentOnItem) newLevel = currentOnItem + 1;

                sendSelectionUpdate(ench, newLevel);
                return true; // Event behandelt, Liste scrollt nicht
            }

            rowIndex++;
        }

        boolean hoverEnchantList =
                mouseX >= listX && mouseX <= listX + 130 &&
                        mouseY >= listY && mouseY <= listY + imageHeight;

        // Scroll-Liste nur wenn mehr Zeilen als sichtbar
        if (hoverEnchantList && totalRows > visibleRows) {
            scrollOffset -= (delta > 0 ? 1 : -1);
            if (scrollOffset < 0) scrollOffset = 0;

            int maxScroll = totalRows - visibleRows;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;

            return true;
        }
        */
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (toggleButton.mouseReleased(mouseX, mouseY, button)) return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (enchantmentListScreen.mouseClicked(mouseX, mouseY, button)) return true;


        int left = (width - imageWidth) / 2;
        int listX = left + imageWidth + 6;
        int listY = (height - imageHeight) / 2 + 4;

        if (magicPowerBar.handleMouseClick(mouseX, mouseY, button))
            return true;

        if (toggleButton.mouseClicked(mouseX, mouseY, button))
            return true;

        /*
        ItemStack targetItem = menu.getItemInSlot0();
        if (targetItem.isEmpty())
            return super.mouseClicked(mouseX, mouseY, button);

        int renderedIndex = 0;
        int visibleRows = imageHeight / rowHeight;

        for (var entry : menu.getUnlockedEnchantments().entrySet()) {
            Enchantment ench = entry.getKey();
            int max = entry.getValue();
            int currentOnItem = targetItem.getEnchantmentLevel(ench);

            // Filter: nur passend UND noch nicht auf dem Item alle Stufen
            if (!ench.canEnchant(targetItem) || currentOnItem >= max)
                continue;

            // Kompatibilitätscheck
            boolean compatible = true;
            for (Enchantment alreadySelected : menu.getSelectedEnchantments().keySet()) {
                if (alreadySelected == ench)
                    continue; // sich selbst ignorieren
                if (menu.getSelectedEnchantments().get(alreadySelected) > 0 && !ench.isCompatibleWith(alreadySelected)) {
                    compatible = false;
                    break;
                }
            }
            if (!compatible)
                continue;

            if (renderedIndex < scrollOffset) {
                renderedIndex++;
                continue;
            }
            if (renderedIndex >= scrollOffset + visibleRows)
                break;

            int y = listY + (renderedIndex - scrollOffset) * rowHeight;

            if (mouseX >= listX && mouseX <= listX + 130 && mouseY >= y && mouseY <= y + rowHeight) {
                int currentSelected = menu.getSelectedEnchantments().getOrDefault(ench, 0);

                if (button == 0) {
                    int newLevel = (currentSelected == 0) ? max : 0;
                    sendSelectionUpdate(ench, newLevel);
                    return true;
                }

                if (button == 1 && max > 1) {
                    int newLevel = currentSelected;
                    newLevel++;
                    if (newLevel <= currentOnItem)
                        newLevel = currentOnItem + 1;
                    if (newLevel > max)
                        newLevel = currentOnItem + 1;
                    sendSelectionUpdate(ench, newLevel);
                    return true;
                }
            }

            renderedIndex++;
        }
        */

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sendSelectionUpdate(Enchantment ench, int level) {
        int currentLevel = menu.getSelectedEnchantments().getOrDefault(ench, 0);

        if (currentLevel == level) return;

        menu.getSelectedEnchantments().put(ench, level);  // Map vom Menu updaten
        Network.sendToServer(new SetEnchantSelectionPacket(ench, level));
    }

    public void onSelectionUpdated(Map<Enchantment, Integer> newSelection) {
        menu.setSelectedEnchantments(newSelection);

        // Optional: ScrollOffset anpassen, falls das selektierte Enchant außerhalb des sichtbaren Bereichs ist
        int row = 0;
        int visibleRows = imageHeight / rowHeight;
        var unlocked = menu.getUnlockedEnchantments();
        for (var entry : unlocked.entrySet()) {
            Enchantment ench = entry.getKey();
            if (newSelection.containsKey(ench) && newSelection.get(ench) > 0) {
                // scrollOffset anpassen, damit Enchantment sichtbar ist
                if (row < scrollOffset) scrollOffset = row;
                else if (row >= scrollOffset + visibleRows) scrollOffset = row - visibleRows + 1;
                break; // nur das erste sichtbare Highlight relevant
            }
            row++;
        }
    }

    /*
    private void renderBlockPalette(GuiGraphics graphics, int left, int top) {
        MagicAccumulator magicAccumulator = menu.getBlockEntity().getMagicAccumulator();
        Map <Block, MagicSourceBlocks> blockPalette = magicAccumulator.getBlockPalette();
        Map <TagKey<Block>, MagicSourceBlockTags> tagPalette = magicAccumulator.getTagPalette();

        List<Object> entries = new ArrayList<>();

        if (blockPalette != null && !blockPalette.isEmpty()) {
            entries.addAll(blockPalette.values());
        }

        if (tagPalette != null && !tagPalette.isEmpty()) {
            entries.addAll(tagPalette.values());
        }

        int listX = left - blockListWidth - 40;
        int listY = top + 4;
        int visibleRows = imageHeight / blockRowHeight;

        int totalEntries = entries.size();

        for (int i = 0; i < visibleRows; i++) {
            int entryIndex = i + blockScrollOffset;
            if (entryIndex >= totalEntries) break;

            Object entry = entries.get(entryIndex);
            int yPos = listY + i * rowHeight;

            Block renderBlock;
            String displayName;
            int displayCurrent; // was angezeigt wird (anzPlaced * magicPerBlock, ggf. geclamped)
            int displayMax;     // magic cap für den Eintrag
            int perBlock;       // magic per block (für Tooltip)

            if (entry instanceof MagicSourceBlocks rb) {
                // ---- Einzelblock ----
                renderBlock = rb.block;
                displayMax = rb.magicCap;
                perBlock = rb.magicPower;

                // gesammelte akkumulierte Power vom BE (kann kleiner als placed*perBlock sein, wenn Zählung partiell ist)
                int accumulated = magicAccumulator.getCurrentPowerForBlock(rb.block);

                // versuche die Anzahl platzierter Blöcke abzuschätzen (ceil), dann multiplizieren
                int placed = perBlock > 0 ? (int) Math.ceil((double) accumulated / perBlock) : 0;
                displayCurrent = Math.min(placed * perBlock, displayMax);

                // Name anzeigen (Blockname)
                displayName = rb.block.getName().getString();

            } else if (entry instanceof MagicSourceBlockTags rt) {
                // ---- Tag-Eintrag ----
                // Hole alle Blöcke des Tags
                Iterable<Holder<Block>> holders = BuiltInRegistries.BLOCK.getTagOrEmpty(rt.tag);
                List<Block> tagBlocks = new ArrayList<>();
                for (Holder<Block> holder : holders) {
                    tagBlocks.add(holder.value());
                }

                if (tagBlocks.isEmpty()) {
                    // Render Fallback-Text
                    graphics.drawString(font, "[INVALID TAG]", listX + 18, yPos + 4, 0xAAAAAA, false);
                    continue;
                }

                // Für das Icon: rotiere durch die Tag-Blöcke
                int rotation = (int) ((System.currentTimeMillis() / 1000) % tagBlocks.size());
                renderBlock = tagBlocks.get(rotation);

                // Summiere akkumulierte Power über alle Block-Typen des Tags (wie im BE gespeichert)
                int accumulatedSum = magicAccumulator.getCurrentPowerForTag(rt.tag);

                perBlock = rt.magicPower;
                displayMax = rt.magicCap;

                int placed = perBlock > 0 ? (int) Math.ceil((double) accumulatedSum / perBlock) : 0;
                displayCurrent = Math.min(placed * perBlock, displayMax);

                // Name: Tag-Path (kein '#'), erster Buchstabe groß
                String raw = rt.tag.location().getPath(); // z.B. "wool"
                displayName = raw.isEmpty() ? raw : raw.substring(0, 1).toUpperCase() + raw.substring(1);

            } else {
                // sollte nicht passieren
                continue;
            }

            // ---- ICON ----
            graphics.renderItem(renderBlock.asItem().getDefaultInstance(), listX, yPos);

            // ---- NAME (ggf. kürzen) ----
            String blockName = displayName;
            int maxWidth = 100 - 18 - 2;
            while (font.width(blockName) > maxWidth && !blockName.isEmpty()) {
                blockName = blockName.substring(0, blockName.length() - 1);
            }
            if (font.width(blockName) > maxWidth) {
                blockName = blockName.substring(0, blockName.length() - 3) + "...";
            }

            // Verschoben nach unten (yPos + 4)
            graphics.drawString(font, blockName, listX + 18, yPos + 4, 0xFFFFFF, false);

            // ---- POWER (aktuell / max) ----
            String powerText = displayCurrent + "/" + displayMax;
            int rightX = listX + 130;
            int textWidth = font.width(powerText);
            int drawX = rightX - textWidth;
            graphics.drawString(font, powerText, drawX, yPos + 4, 0xFFFFFF, false);

            // ---- TOOLTIP ----
            if (mouseX >= listX && mouseX <= listX + 16 &&
                    mouseY >= yPos && mouseY <= yPos + 16) {

                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal(blockName));
                tooltip.add(Component.literal("Current: " + displayCurrent + " / Max: " + displayMax));
                tooltip.add(Component.literal("Per Block: " + perBlock)); // Per-Block Info wie gewünscht

                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }
    }
    */

    private int calculateSelectedMagicCost() {
        int totalCost = 0;
        Map<Enchantment, Integer> selected = menu.getSelectedEnchantments();
        for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();
            totalCost += MagicCostCalculator.calculateMagicRequirement(ench, level);
        }
        return totalCost;
    }

}
