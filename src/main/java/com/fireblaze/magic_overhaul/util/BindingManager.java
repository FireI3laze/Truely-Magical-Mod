package com.fireblaze.magic_overhaul.util;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.ArcaneEnchantingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class BindingManager {

    // Cache während des laufenden Spiels
    private static final Map<UUID, BlockPos> playerBindings = new HashMap<>();
    // Versucht, den Spieler an den Tisch zu binden
    public static boolean bind(Player player, ArcaneEnchantingTableBlockEntity table) {
        UUID id = player.getUUID();

        // Prüfen, ob schon gebunden
        if (getBoundTable(player) != null) return false;

        BlockPos pos = table.getBlockPos();
        playerBindings.put(id, pos);

        // Direkt in Spieler-NBT speichern
        saveBinding(player, pos);

        return true;
    }

    // Bindung lösen
    public static void unbind(Player player) {
        UUID id = player.getUUID();
        playerBindings.remove(id);

        // Spieler-NBT zurücksetzen
        player.getPersistentData().remove("boundTable");
    }

    // Liefert die aktuell gebundene Table (aus Cache oder NBT)
    public static BlockPos getBoundTable(Player player) {
        UUID id = player.getUUID();

        // Zuerst Cache prüfen
        if (playerBindings.containsKey(id)) {
            return playerBindings.get(id);
        }

        // Dann NBT prüfen
        BlockPos pos = loadBinding(player);
        if (pos != null) {
            playerBindings.put(id, pos); // Cache auffüllen
        }

        return pos;
    }

    public static List<UUID> getBoundPlayer(BlockPos pos) { // todo might need client sync? edit: probably not
        List<UUID> boundPlayers = new ArrayList<>();

        for (Map.Entry<UUID, BlockPos> entry : playerBindings.entrySet()) {
            if (entry.getValue().equals(pos)) {
                boundPlayers.add(entry.getKey());
            }
        }

        return boundPlayers;
    }

    private static void saveBinding(Player player, BlockPos pos) {
        player.getPersistentData().putLong("boundTable", pos.asLong());
    }

    // Lädt Bindung aus Spieler-NBT
    private static BlockPos loadBinding(Player player) {
        if (player.getPersistentData().contains("boundTable")) {
            return BlockPos.of(player.getPersistentData().getLong("boundTable"));
        }
        return null;
    }

    public static void removeBindingsForTable(Level level, BlockPos tablePos) {
        for (Player player : level.players()) {  // alle Spieler im Level durchgehen
            UUID id = player.getUUID();
            BlockPos bound = playerBindings.get(id);
            if (bound != null && bound.equals(tablePos)) {
                playerBindings.remove(id);                  // Server-Cache
                player.getPersistentData().remove("boundTable"); // Spieler-NBT
                player.displayClientMessage(
                        Component.literal("Arcane Enchanting Table at x" + tablePos.getX() + " y" + tablePos.getY() + " z" + tablePos.getZ() + " destroyed. Binding lost"),
                        true // true = nur im Action-Bar anzeigen
                );
            }
        }
    }
}
