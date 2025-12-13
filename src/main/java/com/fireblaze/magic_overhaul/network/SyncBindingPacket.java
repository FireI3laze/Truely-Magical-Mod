package com.fireblaze.magic_overhaul.network;

import com.fireblaze.magic_overhaul.util.ClientBindingState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncBindingPacket {
    private final boolean hasBinding;
    private final BlockPos pos;

    // Konstruktor für Server-seitiges Senden
    public SyncBindingPacket(BlockPos pos) {
        this.hasBinding = pos != null;
        this.pos = pos;
    }

    // Decoder (Client empfängt)
    public static SyncBindingPacket decode(FriendlyByteBuf buf) {
        boolean has = buf.readBoolean();
        BlockPos p = null;
        if (has) p = buf.readBlockPos();
        return new SyncBindingPacket(p);
    }

    // Encoder (Server sendet)
    public static void encode(SyncBindingPacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.hasBinding);
        if (pkt.hasBinding && pkt.pos != null) buf.writeBlockPos(pkt.pos);
    }

    // Handler: läuft auf Network-Thread — schedule auf Client-Thread
    public static void handle(SyncBindingPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // Stelle sicher, dass wir auf dem Client-Render/Client-Main-Thread arbeiten
            Minecraft.getInstance().execute(() -> {
                if (msg.hasBinding && msg.pos != null) ClientBindingState.setBoundTable(msg.pos);
                else ClientBindingState.setBoundTable(null);


            });
        });
        ctx.setPacketHandled(true);
    }
}
