package com.fireblaze.truly_enchanting.client;

import com.fireblaze.truly_enchanting.TrulyEnchanting;
import com.fireblaze.truly_enchanting.runes.RuneLoader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.util.HashMap;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    private static boolean runesLoaded = false;

    @SubscribeEvent
    public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.isLocalServer()) return;  // Nur Singleplayer
        if (runesLoaded) return;          // Nur einmal ausführen
        if (mc.level == null) return;     // Welt noch nicht geladen

        // Desync Runes from Server (leeren)
        RuneLoader.replaceAll(new HashMap<>());

        // Reload aus config
        File runesDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "truly_enchanting/runes");
        RuneLoader.reloadRunes(runesDir, TrulyEnchanting.MODID);

        runesLoaded = true;
    }

    @SubscribeEvent
    public static void onClientLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // Reset, sobald der Spieler eine Welt verlässt
        runesLoaded = false;
    }

}