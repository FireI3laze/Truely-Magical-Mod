package com.fireblaze.magic_overhaul.registry;

import com.fireblaze.magic_overhaul.MagicOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MagicOverhaul.MODID);

    public static final RegistryObject<SoundEvent> BEAM_LOOP = SOUNDS.register("beam_loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MagicOverhaul.MODID, "beam_loop")));

    public static final RegistryObject<SoundEvent> MAGIC_EXPLOSION = SOUNDS.register("magic_explosion",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MagicOverhaul.MODID, "magic_explosion")));

    public static final RegistryObject<SoundEvent> BAD_OMEN = SOUNDS.register("bad_omen",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MagicOverhaul.MODID, "bad_omen")));


    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
