package com.fireblaze.magic_overhaul.client.screen;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.MagicAccumulator;
import com.fireblaze.magic_overhaul.blockentity.MonolithBlockEntity;
import com.fireblaze.magic_overhaul.client.color.RuneColorTheme;
import com.fireblaze.magic_overhaul.client.screen.utils.BlocklistScreen;
import com.fireblaze.magic_overhaul.client.screen.utils.ScreenController;
import com.fireblaze.magic_overhaul.client.screen.utils.ScreenSide;
import com.fireblaze.magic_overhaul.menu.MonolithMenu;
import com.fireblaze.magic_overhaul.util.MagicSourceBlocks;
import com.fireblaze.magic_overhaul.util.MagicSourceBlockTags;
import com.fireblaze.magic_overhaul.runes.RuneType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MonolithScreen extends AbstractContainerScreen<MonolithMenu> {

    public MonolithScreen(MonolithMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private BlocklistScreen blocklistScreen;
    private final int blockListWidth = 140;

    @Override
    protected void init() {
        super.init();

        MagicAccumulator acc = menu.monolith.getMagicAccumulator();
        Map<Block, MagicSourceBlocks> blockPalette = menu.currentRune.blockPalette;
        Map<TagKey<Block>, MagicSourceBlockTags> tagPalette = menu.currentRune.tagPalette;

        // Neuen Controller anlegen
        ScreenController controller = new ScreenController(
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                rowHeight,
                blockListWidth,
                ScreenSide.RIGHT
        );

        blocklistScreen = new BlocklistScreen(
                controller.getListX(),
                controller.getListY(),
                controller.getListWidth(),
                imageHeight,
                rowHeight,
                font,
                acc,
                blockPalette,
                tagPalette
        );
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private int scrollOffset = 0;
    private final int rowHeight = 18;
    private int blockScrollOffset = 0;

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        MonolithBlockEntity monolith = menu.monolith;
        if (monolith == null) return;
        MagicAccumulator magicAccumulator = menu.monolith.getMagicAccumulator();

        int magicPower = magicAccumulator.getCurrentMagicPowerIncreaseRate();

        RuneType rune = menu.currentRune;
        if (rune == null || rune.getEnchantments().length == 0) return;

        int startY = 84;
        int barMaxWidth = 60;
        int barHeight = 6;
        blocklistScreen.render(graphics, mouseX, mouseY);

        for (Enchantment ench : rune.getEnchantments()) {
            int enchX = x + 100;
            int enchY = y + startY - 3;

            // Hintergrund
            graphics.fill(enchX, enchY, enchX + barMaxWidth, enchY + barHeight, 0xFF555555);

            // Maximaler MagicCost
            int maxCost = com.fireblaze.magic_overhaul.util.MagicCostCalculator.calculateMagicRequirement(ench, ench.getMaxLevel());

            // Voller Gradient über die gesamte Bar
            RuneColorTheme theme = rune.getTheme();
            int startColor = theme.secondary;
            int endColor = theme.accent;

            for (int i = 0; i < barMaxWidth; i++) {
                float t = (float) i / (barMaxWidth - 1);
                int color = interpolateColor(startColor, endColor, t);
                graphics.fill(enchX + i, enchY, enchX + i + 1, enchY + barHeight, color);
            }

            // Overlay für ungenutzte Magie (grau)
            int filled = (int) (Math.min((float) magicPower / maxCost, 1.0f) * barMaxWidth);
            if (filled < barMaxWidth) {
                graphics.fill(enchX + filled, enchY, enchX + barMaxWidth, enchY + barHeight, 0xFF555555);
            }

            int currentMaxLevel = 0;
            boolean tooltipShown = false;

            // Level-Trennlinien
            for (int lvl = 1; lvl <= ench.getMaxLevel(); lvl++) {
                int required = com.fireblaze.magic_overhaul.util.MagicCostCalculator.calculateMagicRequirement(ench, lvl);
                float ratio = Math.min((float) required / maxCost, 1.0f);
                int posX = enchX + (int) (ratio * barMaxWidth);
                int color = required <= magicPower ? 0xFF000001 : 0xFF000000;
                graphics.fill(posX, enchY, posX + 1, enchY + barHeight, color);

                // Hover-Check für Level-Trennlinie
                if (mouseX >= posX && mouseX <= posX + 1 && mouseY >= enchY && mouseY <= enchY + barHeight) {
                    MutableComponent name = ench.getFullname(lvl).copy(); // Kopie erzeugen
                    name.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)));
                    graphics.renderComponentTooltip(
                            font,
                            List.of(
                                    name,
                                    Component.literal((int) Math.min(magicPower, required) + " / " + required)
                            ),
                            mouseX,
                            mouseY
                    );
                    tooltipShown = true; // Tooltip wurde angezeigt
                    break; // keine weiteren Level-Trennlinien prüfen
                }

                if (magicPower >= required) {
                    currentMaxLevel = lvl;
                }
            }

            // Hover-Check für die Bar selbst (nur wenn noch kein Tooltip angezeigt wurde)
            if (!tooltipShown && mouseX >= enchX && mouseX <= enchX + barMaxWidth && mouseY >= enchY && mouseY <= enchY + barHeight) {
                if (currentMaxLevel == 0) {
                    MutableComponent name = ench.getFullname(currentMaxLevel).copy();
                    name.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)));
                    graphics.renderComponentTooltip(
                            font,
                            List.of(
                                    Component.translatable(ench.getDescriptionId()),
                                    Component.literal((int) magicPower + " / " + maxCost)
                            ),
                            mouseX,
                            mouseY
                    );
                } else {
                    MutableComponent name = ench.getFullname(currentMaxLevel).copy();
                    name.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)));
                    graphics.renderComponentTooltip(
                            font,
                            List.of(
                                    name,
                                    Component.literal((int) magicPower + " / " + maxCost)
                            ),
                            mouseX,
                            mouseY
                    );
                }
            }

            startY += 15;
        }

        int left = (width - imageWidth) / 2;
        int top = (height - imageHeight) / 2;

        // ========== Block List ==========
        int listX = (width - imageWidth) / 2 + imageWidth + 6; // rechts neben GUI
        int listY = (height - imageHeight) / 2;               // exakt GUI-Top
        int visibleRows = imageHeight / rowHeight;


        Map <Block, MagicSourceBlocks> blockPalette = monolith.getCurrentRuneType().blockPalette;
        Map <TagKey<Block>, MagicSourceBlockTags> tagPalette = monolith.getCurrentRuneType().tagPalette;

        int blockListWidth = 90;
        int blockRowHeight = 20;
        //BlocklistScreen.renderBlockPalette(graphics, left, top, imageHeight, rowHeight, mouseX, mouseY, blockScrollOffset, blockRowHeight, blockListWidth, font, magicAccumulator, blockPalette, tagPalette, imageWidth, true);

        /*
        List<Object> entries = new ArrayList<>();

        if (blockPalette != null && !blockPalette.isEmpty()) {
            entries.addAll(blockPalette.values());
        }

        if (tagPalette != null && !tagPalette.isEmpty()) {
            entries.addAll(tagPalette.values());
        }

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
        }*/
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (blocklistScreen.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("magic_overhaul", "textures/gui/monolith_gui.png");


    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "Monolith", 4, 6, 0x404040, false);

        RuneType rune = menu.currentRune;
        if (rune != null && rune.getEnchantments().length > 0) {
            int y = 70; // Start Y Position

            // Überschrift mit Gradient
            drawGradientString(graphics, "Rune Enchantments", 4, 56, rune.getTheme().secondary, rune.getTheme().accent, 0);

            y += 10;
            for (Enchantment ench : rune.getEnchantments()) {
                drawGradientString(graphics, Component.translatable(ench.getDescriptionId()).getString(), 8, y,
                        rune.getTheme().secondary, rune.getTheme().accent, 15);
                y += 15;
            }
        }
    }

    private void drawGradientString(GuiGraphics graphics, String text, int x, int y, int startColor, int endColor, int maxLength) {
        // Optionales Abschneiden
        if (maxLength > 0 && text.length() > maxLength) {
            int cutLength = Math.max(0, maxLength - 3); // Platz für "..."
            text = text.substring(0, cutLength) + "...";
        }

        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            float t = 1.0f - ((float) i / Math.max(length - 1, 1)); // Gradient umgedreht
            int color = interpolateColor(startColor, endColor, t);
            graphics.drawString(font, String.valueOf(c), x, y, color, false);
            x += font.width(String.valueOf(c)); // Cursor nach rechts verschieben
        }
    }


    private static int interpolateColor(int startColor, int endColor, float t) {
        int a1 = (startColor >> 24) & 0xFF;
        int r1 = (startColor >> 16) & 0xFF;
        int g1 = (startColor >> 8) & 0xFF;
        int b1 = startColor & 0xFF;

        int a2 = (endColor >> 24) & 0xFF;
        int r2 = (endColor >> 16) & 0xFF;
        int g2 = (endColor >> 8) & 0xFF;
        int b2 = endColor & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

}
