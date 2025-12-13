package com.fireblaze.magic_overhaul.util;

import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;

/**
 * Einfacher clientseitiger Speicher für das aktuell gebundene Table des lokalen Spielers.
 * Wird vom SyncBindingPacket aktualisiert.
 */
public class ClientBindingState {
    // null = nicht gebunden
    private static volatile BlockPos boundTable = null;

    public static @Nullable BlockPos getBoundTable() {
        return boundTable;
    }

    public static void setBoundTable(@Nullable BlockPos pos) {
        boundTable = pos;
    }
}
