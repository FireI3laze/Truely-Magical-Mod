package com.fireblaze.truly_enchanting.client.color;

import com.fireblaze.truly_enchanting.TrulyEnchanting;
import com.fireblaze.truly_enchanting.registry.ModRunes;
import com.fireblaze.truly_enchanting.runes.RuneDefinition;
import com.fireblaze.truly_enchanting.runes.RuneLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(
        modid = TrulyEnchanting.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class RuneItemColorHandler {

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> {

                    if (tintIndex != 0) return 0xFFFFFFFF;
                    if (!stack.hasTag() || !stack.getTag().contains("rune_id")) {
                        return 0xFFFFFFFF;
                    }

                    ResourceLocation id =
                            ResourceLocation.tryParse(stack.getTag().getString("rune_id"));

                    if (id == null) return 0xFFFFFFFF;

                    RuneDefinition rune =
                            RuneLoader.getRuneDefinition(id.toString());

                    if (rune == null) return 0xFFFFFFFF;

                    return 0xFF000000 | rune.baseColor;
                },
                ModRunes.RUNE.get()
        );
    }
}
