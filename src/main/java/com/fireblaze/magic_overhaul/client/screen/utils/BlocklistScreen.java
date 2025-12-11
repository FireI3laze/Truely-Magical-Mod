package com.fireblaze.magic_overhaul.client.screen.utils;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.MagicAccumulator;
import com.fireblaze.magic_overhaul.util.MagicSourceBlockTags;
import com.fireblaze.magic_overhaul.util.MagicSourceBlocks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlocklistScreen {

    // --- Layout ---
    private int left;
    private int top;
    private int imageWidth;
    private int imageHeight;
    private int rowHeight;
    private int blockListWidth;

    // --- State ---
    private int scrollOffset = 0;

    // --- Data ---
    private Font font;
    private MagicAccumulator magicAccumulator;
    private Map<Block, MagicSourceBlocks> blockPalette;
    private Map<TagKey<Block>, MagicSourceBlockTags> tagPalette;

    public BlocklistScreen(
            int listX,
            int listY,
            int blockListWidth,
            int imageHeight,
            int rowHeight,
            Font font,
            MagicAccumulator magicAccumulator,
            Map<Block, MagicSourceBlocks> blockPalette,
            Map<TagKey<Block>, MagicSourceBlockTags> tagPalette
    ) {
        this.left = listX;
        this.top = listY;
        this.blockListWidth = blockListWidth;
        this.imageHeight = imageHeight;
        this.rowHeight = rowHeight;
        this.font = font;
        this.magicAccumulator = magicAccumulator;
        this.blockPalette = blockPalette;
        this.tagPalette = tagPalette;
    }


    // -----------------------------------------------------------------------
    //                              Layout Utils
    // -----------------------------------------------------------------------

    private int getListX() {
        return left;
    }

    private int getListY() {
        return top;
    }

    // -----------------------------------------------------------------------
    //                          Scroll-Unterstützung
    // -----------------------------------------------------------------------

    public void setScrollOffset(int offset) {
        scrollOffset = Math.max(0, offset);
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public int getTotalEntryCount() {
        int size = 0;
        if (blockPalette != null) size += blockPalette.size();
        if (tagPalette != null) size += tagPalette.size();
        return size;
    }

    // -----------------------------------------------------------------------
    //                              Rendering
    // -----------------------------------------------------------------------

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {

        List<Object> entries = new ArrayList<>();

        if (blockPalette != null && !blockPalette.isEmpty())
            entries.addAll(blockPalette.values());

        if (tagPalette != null && !tagPalette.isEmpty())
            entries.addAll(tagPalette.values());

        entries.sort((a, b) -> Integer.compare(
                getRegistrySortId(a),
                getRegistrySortId(b)
        ));

        int listX = getListX();
        int listY = getListY();

        int visibleRows = imageHeight / rowHeight;
        int totalEntries = entries.size();

        for (int i = 0; i < visibleRows; i++) {
            int entryIndex = i + scrollOffset;
            if (entryIndex >= totalEntries) break;

            Object entry = entries.get(entryIndex);
            int yPos = listY + i * rowHeight;

            Block renderBlock;
            String displayName;
            int displayCurrent;
            int displayMax;
            int perBlock;

            // ------------------------ Einzelblock ------------------------
            if (entry instanceof MagicSourceBlocks rb) {

                renderBlock = rb.block;
                displayMax = rb.magicCap;
                perBlock = rb.magicPower;

                int accumulated = magicAccumulator.getCurrentPowerForBlock(rb.block);
                int placed = perBlock > 0 ? (int) Math.ceil((double) accumulated / perBlock) : 0;
                displayCurrent = Math.min(placed * perBlock, displayMax);

                displayName = rb.block.getName().getString();
            }

            // ------------------------ Tag-Eintrag ------------------------
            else if (entry instanceof MagicSourceBlockTags rt) {

                Iterable<Holder<Block>> holders = BuiltInRegistries.BLOCK.getTagOrEmpty(rt.tag);
                List<Block> tagBlocks = new ArrayList<>();
                for (Holder<Block> holder : holders) {
                    tagBlocks.add(holder.value());
                }

                if (tagBlocks.isEmpty()) {
                    graphics.drawString(font, "[INVALID TAG]", listX + 18, yPos + 4, 0xAAAAAA, false);
                    continue;
                }

                int rotation = (int) ((System.currentTimeMillis() / 1000) % tagBlocks.size());
                renderBlock = tagBlocks.get(rotation);

                int sum = magicAccumulator.getCurrentPowerForTag(rt.tag);
                perBlock = rt.magicPower;
                displayMax = rt.magicCap;

                int placed = perBlock > 0 ? (int) Math.ceil((double) sum / perBlock) : 0;
                displayCurrent = Math.min(placed * perBlock, displayMax);

                String raw = rt.tag.location().getPath();
                displayName = raw.isEmpty() ? raw : raw.substring(0, 1).toUpperCase() + raw.substring(1);
            } else {
                continue;
            }

            // ICON
            graphics.renderItem(renderBlock.asItem().getDefaultInstance(), listX, yPos);

            // Name trimmen
            String trimmed = displayName;
            int maxWidth = 100 - 18 - 2;
            while (font.width(trimmed) > maxWidth && !trimmed.isEmpty()) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            if (font.width(trimmed) > maxWidth) {
                trimmed = trimmed.substring(0, trimmed.length() - 3) + "...";
            }

            graphics.drawString(font, trimmed, listX + 18, yPos + 4, 0xFFFFFF, false);

            // POWER rechtsbündig
            String text = displayCurrent + "/" + displayMax;
            int rightX = listX + blockListWidth - 4;
            int w = font.width(text);
            graphics.drawString(font, text, rightX - w, yPos + 4, 0xFFFFFF, false);

            // TOOLTIP
            if (mouseX >= listX && mouseX <= listX + 16 &&
                    mouseY >= yPos && mouseY <= yPos + 16) {

                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal(trimmed));
                tooltip.add(Component.literal("Current: " + displayCurrent + " / Max: " + displayMax));
                tooltip.add(Component.literal("Per Block: " + perBlock));

                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }
    }

    // -----------------------------------------------------------------------
    //                     Scroll Input Handling
    // -----------------------------------------------------------------------

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {

        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        int visibleRows = imageHeight / rowHeight;
        int maxScroll = Math.max(0, getTotalEntryCount() - visibleRows);

        scrollOffset -= delta;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        int listX = getListX();
        int listY = getListY();
        int listHeight = imageHeight;

        return mouseX >= listX && mouseX <= listX + blockListWidth
                && mouseY >= listY && mouseY <= listY + listHeight;
    }

    public static int getRegistrySortId(Object entry) {
        if (entry instanceof MagicSourceBlocks rb) {
            // direkte Registry-ID für jeden Block
            return BuiltInRegistries.BLOCK.getId(rb.block);
        }
        if (entry instanceof MagicSourceBlockTags rt) {
            // Tag → nimm die erste gültige Block-ID im Tag als Sortierschlüssel
            var holders = BuiltInRegistries.BLOCK.getTagOrEmpty(rt.tag);

            int minId = Integer.MAX_VALUE;
            for (var holder : holders) {
                int id = BuiltInRegistries.BLOCK.getId(holder.value());
                if (id < minId) minId = id;
            }
            return minId;
        }
        return Integer.MAX_VALUE;
    }

}