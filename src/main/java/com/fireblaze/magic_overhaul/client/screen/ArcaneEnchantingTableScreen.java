package com.fireblaze.magic_overhaul.client.screen;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.MagicAccumulator;
import com.fireblaze.magic_overhaul.client.ClientConfig;
import com.fireblaze.magic_overhaul.client.screen.utils.*;
import com.fireblaze.magic_overhaul.menu.ArcaneEnchantingTableMenu;
import com.fireblaze.magic_overhaul.network.Network;
import com.fireblaze.magic_overhaul.network.SetEnchantSelectionPacket;
import com.fireblaze.magic_overhaul.util.MagicCostCalculator;
import com.fireblaze.magic_overhaul.util.MagicSourceBlockTags;
import com.fireblaze.magic_overhaul.util.MagicSourceBlocks;
import com.fireblaze.magic_overhaul.util.PlayerSettings;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

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

    // Interface Control
    private final List<ScreenController> controllers = new ArrayList<>();
    private final Map<ScreenSide, ScreenController> visibleController = new HashMap<>();
    private final Map<ScreenSide, Boolean> sideVisibilityState = new EnumMap<>(ScreenSide.class);
    private final Map<ScreenController, ItemStack> controllerIcons = new HashMap<>();


    // controllers as fields so we can reference them
    private ScreenController blocklistController;
    private ScreenController enchantmentController;
    private ScreenController magicalBarController;

    private MagicAccumulator acc;
    private BlocklistScreen blocklistScreen;
    private MagicPowerBar magicPowerBar;
    private EnchantmentListScreen enchantmentListScreen;

    // dynamic buttons
    private final List<CustomButton> controllerSwitchButtons = new ArrayList<>();
    private final List<CustomButton> sideSwitchButtons = new ArrayList<>();
    private final List<CustomButton> sideToggleButtons = new ArrayList<>();

    @Override
    protected void init() {
        super.init();

        acc = menu.getBlockEntity().getMagicAccumulator();

        ClientConfig cfg = ClientConfig.get();

        // Build controllers (store as fields)
        blocklistController = new ScreenController(
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                rowHeight,
                blockListWidth,
                ScreenSide.LEFT
        );

        enchantmentController = new ScreenController(
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                rowHeight,
                blockListWidth,
                ScreenSide.LEFT
        );

        magicalBarController = new ScreenController(
                leftPos,
                topPos,
                imageWidth,
                imageHeight,
                rowHeight,
                400,
                ScreenSide.TOP
        );

        // register controllers
        controllers.clear();
        controllers.add(blocklistController);
        controllers.add(enchantmentController);
        controllers.add(magicalBarController);

        // Build UI components bound to controllers
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

        enchantmentListScreen = new EnchantmentListScreen(menu, font, enchantmentController, enchantmentController.getListWidth());

        magicPowerBar = new MagicPowerBar(magicalBarController, font, magicalBarController.getListWidth(), acc.getMagicPowerCapPerPlayerSoft(), 100000);

        // load player settings
        assert minecraft != null && minecraft.player != null;
        magicPowerBar.setMotion(PlayerSettings.loadBoolean(minecraft.player, "magicBarMotion", false));
        magicPowerBar.setSparkle(PlayerSettings.loadBoolean(minecraft.player, "magicBarSparkle", true));

        // initial visibility: one visible controller per side (first in that group)
        var groups = groupControllersBySide();
        for (var e : groups.entrySet()) {
            List<ScreenController> grp = e.getValue();
            if (!grp.isEmpty()) {
                ScreenController first = grp.get(0);
                visibleController.put(e.getKey(), first);
                for (ScreenController c : grp) c.setVisible(c == first);
            }
        }

        controllerIcons.put(blocklistController, new ItemStack(Items.BRICKS));        // Blockliste
        controllerIcons.put(enchantmentController, new ItemStack(Items.ENCHANTED_BOOK)); // Enchantments
        controllerIcons.put(magicalBarController, new ItemStack(Items.NETHER_STAR));  // Magic Bar

        // ensure components have correct parent coords
        controllers.forEach(c -> c.recalculatePosition(leftPos, topPos, imageWidth, imageHeight));
        // initial dynamic UI
        buildDynamicUI();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.renderTooltip(gui, mouseX, mouseY);

        // dynamische Buttons zeichnen
        controllerSwitchButtons.forEach(b -> { if (b.visible) b.render(gui, mouseX, mouseY); });
        sideSwitchButtons.forEach(b -> { if (b.visible) b.render(gui, mouseX, mouseY); });
        sideToggleButtons.forEach(b -> { if (b.visible) b.render(gui, mouseX, mouseY); });

        controllerSwitchButtons.forEach(b -> {
            if (b.visible) {
                b.render(gui, mouseX, mouseY);
                if (b.side != null) {
                    ScreenController visible = visibleController.get(b.side);
                    if (visible != null) {
                        ItemStack stack = controllerIcons.get(visible);
                        if (stack != null) {
                            gui.renderItem(stack, b.x1 + 11, b.y1);
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int x, int y) {
        // update controller parent positions in case leftPos/topPos changed on resize
        controllers.forEach(c -> c.recalculatePosition(leftPos, topPos, imageWidth, imageHeight));

        // render only visible controllers' content
        if (enchantmentController.isVisible()) {
            enchantmentListScreen.render(gui, mouseX, mouseY);
        }

        if (blocklistController.isVisible()) {
            // update blocklistScreen positions if parent moved
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
            blocklistScreen.render(gui, mouseX, mouseY);
        }

        // Magic bar always rendered if its controller visible
        if (magicalBarController.isVisible()) {
            magicPowerBar.setPlannedConsumption(calculateSelectedMagicCost());
            magicPowerBar.setAccumulatedMagicPower(acc.getAccumulatedMagicPower());
            magicPowerBar.setCurrentMagicPowerIncreaseRate(acc.getCurrentMagicPowerIncreaseRate());
            magicPowerBar.render(gui, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        // pass events only to visible controllers
        if (enchantmentController.isVisible() && enchantmentListScreen.mouseScrolled(mouseX, mouseY, delta)) return true;
        if (blocklistController.isVisible() && blocklistScreen.mouseScrolled(mouseX, mouseY, delta)) return true;

        // check controller-switch buttons
        for (var b : controllerSwitchButtons) {
            // scrolling doesn't affect buttons
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (var b : controllerSwitchButtons) if (b.mouseReleased(mouseX, mouseY, button)) return true;
        for (var b : sideSwitchButtons) if (b.mouseReleased(mouseX, mouseY, button)) return true;
        for (var b : sideToggleButtons) if (b.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (enchantmentController.isVisible() && enchantmentListScreen.mouseClicked(mouseX, mouseY, button)) return true;
        if (magicalBarController.isVisible() && magicPowerBar.handleMouseClick(mouseX, mouseY, button)) return true;

        for (var b : controllerSwitchButtons) if (b.visible && b.mouseClicked(mouseX, mouseY, button)) return true;
        for (var b : sideSwitchButtons) if (b.visible && b.mouseClicked(mouseX, mouseY, button)) return true;
        for (var b : sideToggleButtons) if (b.visible && b.mouseClicked(mouseX, mouseY, button)) return true;

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

    private Map<ScreenSide, List<ScreenController>> groupControllersBySide() {
        return controllers.stream().collect(Collectors.groupingBy(ScreenController::getSide));
    }

    // -------------------------
    // Dynamic UI builders
    // -------------------------

    private void buildDynamicUI() {
        controllerSwitchButtons.clear();
        sideSwitchButtons.clear();
        sideToggleButtons.clear(); // neue Liste für Toggle-Buttons

        buildSwitchButtons();
        buildSideSwitchButtons();
        buildSideToggleButtons(); // neuer Button
    }

    private void buildSwitchButtons() {
        controllerSwitchButtons.clear(); // alte Buttons löschen

        var groups = groupControllersBySide();

        for (var entry : groups.entrySet()) {
            ScreenSide side = entry.getKey();

            // Nur LEFT oder RIGHT Buttons erstellen
            if (side != ScreenSide.LEFT && side != ScreenSide.RIGHT) continue;

            List<ScreenController> group = entry.getValue();
            if (group.isEmpty()) continue;

            // Button nur, wenn mehr als 1 Interface auf der Seite
            if (group.size() <= 1) continue;

            ScreenController visible = visibleController.get(side);
            if (visible == null) continue;

            // Position des Buttons immer relativ zum sichtbaren Controller
            int x = getButtonXRelative(visible, 0);
            int y = getButtonYBelow(visible, 4); // über dem Interface

            // **Nur ein Button pro Seite**
            CustomButton cycle = new CustomButton(
                    x,
                    y,
                    x + 40,
                    y + 16,
                    "",
                    "",
                    0xFF333333,
                    0xFF333333,
                    0xFFAA0FFF,
                    font,
                    (newState) -> cycleToNextController(side),
                    side
            );

            controllerSwitchButtons.add(cycle);
        }
    }

    private void cycleToNextController(ScreenSide side) {
        List<ScreenController> group = groupControllersBySide().get(side);
        if (group == null || group.size() <= 1) return;

        ScreenController current = visibleController.get(side);
        int idx = group.indexOf(current);
        if (idx == -1) return;

        int next = (idx + 1) % group.size(); // zirkulär
        ScreenController nextController = group.get(next);

        // Sichtbarkeit aktualisieren
        visibleController.put(side, nextController);
        for (ScreenController c : group) {
            c.setVisible(c == nextController);
        }

        // **Button nicht erneut bauen**, sondern nur Position ggf. anpassen
        // controllerSwitchButtons.clear(); // NICHT mehr aufrufen
        CustomButton btn = controllerSwitchButtons.stream().findFirst().orElse(null);
        if (btn != null) {
            btn.x1 = getButtonXRelative(nextController, 0);
            btn.y1 = getButtonYBelow(nextController, 4);
            btn.x2 = btn.x1 + 40;
            btn.y2 = btn.y1 + 16;
        }

    }


    private void switchToController(ScreenSide side, ScreenController controller) {
        visibleController.put(side, controller);
        for (ScreenController c : groupControllersBySide().getOrDefault(side, Collections.emptyList())) {
            c.setVisible(c == controller);
        }
        // update buttons (toggled state)
        for (CustomButton b : controllerSwitchButtons) {
            // set toggled states by comparing label -> safe enough if displayNames unique per group
            b.setToggled(bLabelEquals(b, controller.getDisplayName()));
        }
    }

    private boolean bLabelEquals(CustomButton b, String label) {
        // reflection of label text is not exposed; as a pragmatic approach we assume unique labels and check toggled already set
        // (we update toggled in creation and on switchToController above), so nothing to do here unless you expose getter on CustomButton.
        return b != null && b.getWidth() >= 0; // noop placeholder to keep compile-time consistent
    }

    private void buildSideSwitchButtons() {
        var groups = groupControllersBySide();

        for (var entry : groups.entrySet()) {
            ScreenSide side = entry.getKey();

            // nur LEFT oder RIGHT Buttons erstellen
            if (side != ScreenSide.LEFT && side != ScreenSide.RIGHT) continue;

            List<ScreenController> group = entry.getValue();
            if (group.isEmpty()) continue;

            ScreenController visible = visibleController.get(side);
            if (visible == null) continue;

            int x = getButtonXRelative(visible, 50); // Offset rechts für Move-Button
            int y = getButtonYBelow(visible, 4);    // unterhalb des Interface


            CustomButton move = new CustomButton(
                    x,
                    y,
                    x + 40,
                    y + 16,
                    "Move",
                    "Move",
                    0xFF333333,
                    0xFF555555,
                    0xFFAA0FFF,
                    font,
                    (newState) -> {
                        ScreenController currentVisible = visibleController.get(side);
                        if (currentVisible != null) moveControllerToOppositeSide(currentVisible);
                    }

            );

            sideSwitchButtons.add(move);
        }
    }

    private void moveControllerToOppositeSide(ScreenController controller) {
        ScreenSide oldSide = controller.getSide();
        ScreenSide newSide = (oldSide == ScreenSide.LEFT) ? ScreenSide.RIGHT : ScreenSide.LEFT;

        controller.moveToSide(newSide);

        // re-evaluate groups and visible map: if the controller was visible on old side,
        // move visibility to this controller in the new side (and ensure only one visible per side)
        controllers.forEach(c -> c.recalculatePosition(leftPos, topPos, imageWidth, imageHeight));

        // re-create grouping defaults: ensure each side has a visible controller
        var groups = groupControllersBySide();
        // ensure visible controller mapping remains valid
        groups.forEach((side, list) -> {
            ScreenController currently = visibleController.get(side);
            if (currently == null || !list.contains(currently)) {
                // pick first available
                if (!list.isEmpty()) visibleController.put(side, list.get(0));
            }
            // ensure visible flags
            for (ScreenController sc : list) sc.setVisible(sc == visibleController.get(side));
        });

        // rebuild dynamic UI to reflect new positions / buttons
        buildDynamicUI();
    }

    private void buildSideToggleButtons() {
        var groups = groupControllersBySide();

        for (var entry : groups.entrySet()) {
            ScreenSide side = entry.getKey();

            // nur LEFT und RIGHT Buttons erstellen
            if (side != ScreenSide.LEFT && side != ScreenSide.RIGHT) continue;

            List<ScreenController> group = entry.getValue();
            if (group.isEmpty()) continue;

            ScreenController visible = visibleController.get(side);
            if (visible == null) continue;

            int x = getButtonXRelative(visible, 100); // rechts neben Move-Button
            int y = getButtonYBelow(visible, 4);      // unterhalb des Interface

            // initialer Zustand: alle sichtbar
            sideVisibilityState.putIfAbsent(side, true);

            CustomButton toggle = new CustomButton(
                    x,
                    y,
                    x + 40,
                    y + 16,
                    "Hide",
                    "Show",
                    0xFF333333,
                    0xFF555555,
                    0xFFAA0FFF,
                    font,
                    (newState) -> toggleSideVisibility(side)
            );

            sideToggleButtons.add(toggle);
        }
    }

    private void toggleSideVisibility(ScreenSide side) {
        boolean currentlyVisible = sideVisibilityState.getOrDefault(side, true);
        boolean newVisibility = !currentlyVisible;
        sideVisibilityState.put(side, newVisibility);

        List<ScreenController> group = groupControllersBySide().getOrDefault(side, Collections.emptyList());

        if (newVisibility) {
            // nur den aktuell sichtbaren Controller anzeigen
            ScreenController visible = visibleController.get(side);
            if (visible != null) {
                visible.setVisible(true);
            }
            // alle anderen unsichtbar
            for (ScreenController c : group) {
                if (c != visible) c.setVisible(false);
            }
        } else {
            // alle ausblenden
            for (ScreenController c : group) {
                c.setVisible(false);
            }
        }

        // Cycle- und Move-Buttons auf dieser Seite ein-/ausblenden
        controllerSwitchButtons.forEach(b -> {
            if (bMatchesSide(b, side)) {
                b.visible = newVisibility;
            }
        });

        sideSwitchButtons.forEach(b -> {
            if (bMatchesSide(b, side)) {
                b.visible = newVisibility;
            }
        });

        // Toggle-Buttons selbst immer sichtbar lassen
        sideToggleButtons.forEach(b -> b.visible = true);
    }

    // Hilfsmethode: Prüft, ob ein Button zu einer Seite gehört
    private boolean bMatchesSide(CustomButton b, ScreenSide side) {
        if (side != ScreenSide.LEFT && side != ScreenSide.RIGHT) return false;

        ScreenController visible = visibleController.get(side);
        if (visible == null) return false;

        int baseX = visible.getListX();
        int baseY = getButtonYBelow(visible, 4); // <- neue Position unterhalb des Interfaces

        // Buttons liegen in einem bekannten Offset zum Controller
        return (b.x1 >= baseX && b.x1 <= baseX + 120) && (b.y1 == baseY);
    }

    // Gibt die Y-Position unterhalb des Interfaces zurück
    private int getButtonYBelow(ScreenController controller, int offset) {
        return controller.getListY() + controller.getHeight() + offset;
    }

    // Gibt die X-Position relativ zum Interface zurück (für mehrere Buttons nebeneinander)
    private int getButtonXRelative(ScreenController controller, int offset) {
        return controller.getListX() + offset;
    }

    private ScreenController getControllerForToggleButton(CustomButton b) {
        if (bMatchesSide(b, ScreenSide.LEFT)) return visibleController.get(ScreenSide.LEFT);
        if (bMatchesSide(b, ScreenSide.RIGHT)) return visibleController.get(ScreenSide.RIGHT);
        return null;
    }

}
