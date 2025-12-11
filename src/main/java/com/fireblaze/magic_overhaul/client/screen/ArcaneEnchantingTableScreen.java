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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (enchantmentListScreen.mouseScrolled(mouseX, mouseY, delta)) return true;
        if (blocklistScreen.mouseScrolled(mouseX, mouseY, delta)) return true;

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

        if (magicPowerBar.handleMouseClick(mouseX, mouseY, button))
            return true;

        if (toggleButton.mouseClicked(mouseX, mouseY, button))
            return true;

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
