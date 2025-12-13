package com.fireblaze.magic_overhaul.runes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RuneItem extends Item {

    private final RuneType runeType;

    public RuneItem(RuneType type) {
        super(new Item.Properties());
        this.runeType = type;
    }

    @Override
    public Component getName(ItemStack stack) {
        // Dynamischer Name, z. B. basierend auf ENUM-Namen
        String displayName = runeType.name()
                .toLowerCase(Locale.ROOT)
                .replace("_", " ");

        // Erste Buchstaben groß
        displayName = Arrays.stream(displayName.split(" "))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));

        return Component.literal(displayName + " Rune");
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("Place in Monolith").withStyle(ChatFormatting.BLUE));
    }


    public RuneType getRuneType() {
        return runeType;
    }
}
