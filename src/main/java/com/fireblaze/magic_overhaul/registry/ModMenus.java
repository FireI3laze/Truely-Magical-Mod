package com.fireblaze.magic_overhaul.registry;

import com.fireblaze.magic_overhaul.blockentity.EnchantingTable.ArcaneEnchantingTableBlockEntity;
import com.fireblaze.magic_overhaul.menu.ArcaneEnchantingTableMenu;
import com.fireblaze.magic_overhaul.menu.MonolithMenu;
import com.fireblaze.magic_overhaul.util.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {

    public static final RegistryObject<MenuType<MonolithMenu>> MONOLITH_MENU =
            Registration.MENUS.register("monolith_menu",
                    () -> IForgeMenuType.create(MonolithMenu::new)
            );

    // Neues Menu für Arcane Enchanting Table
    public static final RegistryObject<MenuType<ArcaneEnchantingTableMenu>> ARCANE_ENCHANTING_TABLE_MENU =
            Registration.MENUS.register("arcane_enchanting_table_menu",
                    () -> IForgeMenuType.create(ArcaneEnchantingTableMenu::new)
            );

    public static void register() {}
}
