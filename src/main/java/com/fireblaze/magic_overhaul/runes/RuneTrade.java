package com.fireblaze.magic_overhaul.runes;

import net.minecraft.world.entity.npc.VillagerProfession;

public class RuneTrade {
    public final VillagerProfession profession;
    public final int professionLevel;
    public final int price;

    public RuneTrade(VillagerProfession profession, int professionLevel, int price) {
        this.profession = profession;
        this.professionLevel = professionLevel;
        this.price = price;
    }
}
