package com.fireblaze.magic_overhaul;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MagicOverhaul.MODID, value = Dist.CLIENT)
public class ClientEvents {


    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

    }
}
