package com.fireblaze.magic_overhaul.registry;

import com.fireblaze.magic_overhaul.block.ArcaneEnchantingTableBlock;
import com.fireblaze.magic_overhaul.block.MonolithBlock;
import com.fireblaze.magic_overhaul.util.Registration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final RegistryObject<Block> MONOLITH = Registration.BLOCKS.register("monolith",
            () -> new MonolithBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.LODESTONE)
            ));

    public static final RegistryObject<Block> ARCANE_ENCHANTING_TABLE = Registration.BLOCKS.register("arcane_enchanting_table",
                    () -> new ArcaneEnchantingTableBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE).sound(SoundType.AMETHYST)
                    ));

    public static void register() {}
}
