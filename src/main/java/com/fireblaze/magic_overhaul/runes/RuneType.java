package com.fireblaze.magic_overhaul.runes;

import com.fireblaze.magic_overhaul.client.color.RuneColorTheme;
import com.fireblaze.magic_overhaul.registry.ModEnchantments;
import com.fireblaze.magic_overhaul.util.MagicSourceBlockTags;
import com.fireblaze.magic_overhaul.util.MagicSourceBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public enum RuneType {

    VILLAGE("village",
            new Supplier[]{
                    () -> Enchantments.BLOCK_EFFICIENCY,
                    () -> Enchantments.POWER_ARROWS,
                    () -> Enchantments.PUNCH_ARROWS},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.COMPOSTER, 10, 20),
                    new MagicSourceBlocks(Blocks.SMITHING_TABLE, 10, 20),
                    new MagicSourceBlocks(Blocks.CARTOGRAPHY_TABLE, 10, 20),
                    new MagicSourceBlocks(Blocks.FLETCHING_TABLE, 10, 20),
                    new MagicSourceBlocks(Blocks.LECTERN, 10, 20),
                    new MagicSourceBlocks(Blocks.CAULDRON, 10, 20),
                    new MagicSourceBlocks(Blocks.GLASS, 2, 50),
                    new MagicSourceBlocks(Blocks.GLASS_PANE, 2, 50),
                    new MagicSourceBlocks(Blocks.GRASS_BLOCK, 1, 150),
                    new MagicSourceBlocks(Blocks.GRASS, 1, 50),
                    new MagicSourceBlocks(Blocks.BELL, 40, 40),
                    new MagicSourceBlocks(Blocks.FARMLAND, 2, 80),
                    new MagicSourceBlocks(Blocks.WHEAT, 2, 40),
                    new MagicSourceBlocks(Blocks.CARROTS, 2, 40),
                    new MagicSourceBlocks(Blocks.POTATOES, 2, 40),
                    new MagicSourceBlocks(Blocks.BEETROOTS, 2, 40),
                    new MagicSourceBlocks(Blocks.CAMPFIRE, 5, 20)
            }, new MagicSourceBlockTags[]{
            new MagicSourceBlockTags(BlockTags.LOGS_THAT_BURN, 1, 50), // todo wird nicht korrekt gezählt
            new MagicSourceBlockTags(BlockTags.PLANKS, 1, 150),
            new MagicSourceBlockTags(BlockTags.WOODEN_DOORS, 2, 20),
            },
            new String[]{"village", "village_plains", "village_snowy", "village_taiga", "village_desert", "village_savanna"},
            new RuneLoot(new String[]{"minecraft:chests/village/village_cartographer"}, 1.0f),
            new RuneTrade(VillagerProfession.CARTOGRAPHER, 1, 16),
        0x8040FF
    ), //Lila 0x228B22

    MINESHAFT("mineshaft",
            new Supplier[]{
                    () -> Enchantments.BLOCK_FORTUNE,
                    () -> Enchantments.UNBREAKING,
                    () -> Enchantments.SILK_TOUCH},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.RAIL, 3, 5),
                    new MagicSourceBlocks(Blocks.CHEST, 2, 3),
                    new MagicSourceBlocks(Blocks.COBWEB, 2, 3),
                    new MagicSourceBlocks(Blocks.OAK_FENCE, 1, 2),
                    new MagicSourceBlocks(Blocks.STONE, 1, 2),
                    new MagicSourceBlocks(Blocks.ANDESITE, 90, 900),
                    new MagicSourceBlocks(Blocks.DIORITE, 1, 2),
                    new MagicSourceBlocks(Blocks.GRANITE, 1, 2),
                    new MagicSourceBlocks(Blocks.GLOW_LICHEN, 1, 2),
                    new MagicSourceBlocks(Blocks.GRAVEL, 1, 2),
                    new MagicSourceBlocks(Blocks.DRIPSTONE_BLOCK, 1, 2),
                    new MagicSourceBlocks(Blocks.POINTED_DRIPSTONE, 1, 2)
            }, new MagicSourceBlockTags[]{
            new MagicSourceBlockTags(BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ores")), 4, 5)
    },
        new String[]{"mineshaft"},
            new RuneLoot(new String[]{"minecraft:chests/abandoned_mineshaft"}, 0.75f),
            new RuneTrade(null, 0, 0),
        0x8B4513
    ),    // Braun

    ANCIENT_CITY("ancient_city",
            new Supplier[]{
                    () -> Enchantments.ALL_DAMAGE_PROTECTION,
                    () -> Enchantments.SWIFT_SNEAK},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.SCULK, 4, 5),
                    new MagicSourceBlocks(Blocks.SCULK_VEIN, 4, 5),
                    new MagicSourceBlocks(Blocks.SCULK_CATALYST, 4, 5),
                    new MagicSourceBlocks(Blocks.SCULK_SENSOR, 3, 4),
                    new MagicSourceBlocks(Blocks.SCULK_SHRIEKER, 4, 5),
                    new MagicSourceBlocks(Blocks.DEEPSLATE, 2, 3),
                    new MagicSourceBlocks(Blocks.CALCITE, 1, 2),
                    new MagicSourceBlocks(Blocks.BLACKSTONE, 2, 3),
                    new MagicSourceBlocks(Blocks.SOUL_CAMPFIRE, 1, 2),
                    new MagicSourceBlocks(Blocks.SOUL_FIRE, 1, 2),
                    new MagicSourceBlocks(Blocks.SOUL_LANTERN, 1, 2),
                    new MagicSourceBlocks(Blocks.SOUL_TORCH, 1, 2), //todo should also be considered on walls
                    new MagicSourceBlocks(Blocks.REINFORCED_DEEPSLATE, 200, 1000)
            },
            new MagicSourceBlockTags[]{
                    new MagicSourceBlockTags(BlockTags.WOOL, 4, 5),
                    new MagicSourceBlockTags(BlockTags.CANDLES, 4, 5)
            },
            new String[]{"ancient_city"},
            new RuneLoot(new String[]{"minecraft:chests/ancient_city"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0x4B0082
    ), // Indigo

    SPAWNER_ROOM("spawner_room",
            new Supplier[]{
                    () -> Enchantments.SHARPNESS,
                    () -> Enchantments.SMITE,
                    () -> Enchantments.BANE_OF_ARTHROPODS,
                    () -> Enchantments.SWEEPING_EDGE},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.SPAWNER, 900, 900),
                    new MagicSourceBlocks(Blocks.MOSSY_COBBLESTONE, 2, 3),
                    new MagicSourceBlocks(Blocks.COBBLESTONE_WALL, 2, 3),
                    new MagicSourceBlocks(Blocks.COBBLESTONE, 1, 2),
                    new MagicSourceBlocks(Blocks.MOSSY_COBBLESTONE_WALL, 1, 2),
                    new MagicSourceBlocks(Blocks.IRON_BARS, 2, 3),
                    new MagicSourceBlocks(Blocks.GLOW_LICHEN, 1, 2)
            }, new MagicSourceBlockTags[]{},
            new String[]{"spawner_room"},
            new RuneLoot(new String[]{"minecraft:chests/simple_dungeon"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0xFF0000
    ), // Rot

    OUTPOST("outpost",
            new Supplier[]{
                    () -> Enchantments.MOB_LOOTING,
                    () -> Enchantments.PIERCING,
                    () -> Enchantments.MULTISHOT,
                    () -> Enchantments.QUICK_CHARGE},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.DARK_OAK_LOG, 2, 3),
                    new MagicSourceBlocks(Blocks.DARK_OAK_PLANKS, 2, 3),
                    new MagicSourceBlocks(Blocks.CAMPFIRE, 3, 4),
                    new MagicSourceBlocks(Blocks.SANDSTONE, 1, 2),
                    new MagicSourceBlocks(Blocks.JACK_O_LANTERN, 100, 600),
                    new MagicSourceBlocks(Blocks.PUMPKIN, 2, 3)
            }, new MagicSourceBlockTags[]{},
            new String[]{"pillager_outpost"},
            new RuneLoot(new String[]{"minecraft:chests/pillager_outpost"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0xFFD700
    ),       // Gold

    RUINED_PORTAL("ruined_portal",
            new Supplier[]{
                    () -> Enchantments.FIRE_ASPECT,
                    () -> Enchantments.FLAMING_ARROWS,
                    () -> Enchantments.FIRE_PROTECTION,
                    () -> Enchantments.SOUL_SPEED},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.OBSIDIAN, 3, 5),
                    new MagicSourceBlocks(Blocks.NETHERRACK, 1, 2),
                    new MagicSourceBlocks(Blocks.NETHER_WART, 1, 2),
                    new MagicSourceBlocks(Blocks.NETHER_WART_BLOCK, 1, 2),
                    new MagicSourceBlocks(Blocks.NETHER_BRICKS, 2, 3),
                    new MagicSourceBlocks(Blocks.CRYING_OBSIDIAN, 3, 4),
                    new MagicSourceBlocks(Blocks.MAGMA_BLOCK, 2, 3),
                    new MagicSourceBlocks(Blocks.NETHER_QUARTZ_ORE, 2, 3),
                    new MagicSourceBlocks(Blocks.NETHER_GOLD_ORE, 900, 900),
                    new MagicSourceBlocks(Blocks.LAVA, 1, 2),
                    new MagicSourceBlocks(Blocks.CRIMSON_STEM, 1, 2),
                    new MagicSourceBlocks(Blocks.WARPED_STEM, 1, 2),
                    new MagicSourceBlocks(Blocks.FIRE, 1, 2),
                    new MagicSourceBlocks(Blocks.GLOWSTONE, 1, 2)
            }, new MagicSourceBlockTags[]{},
            new String[]{"ruined_portal"},
            new RuneLoot(new String[]{"minecraft:chests/ruined_portal"}, 0.33f),
            new RuneTrade(null, 0, 0),
            0x800080
    ), // Dunkelviolett

    SHIPWRECK("shipwreck",
            new Supplier[]{
                    () -> Enchantments.AQUA_AFFINITY,
                    () -> Enchantments.RESPIRATION,
                    () -> Enchantments.DEPTH_STRIDER},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.OAK_PLANKS, 2, 3),
                    new MagicSourceBlocks(Blocks.OAK_LOG, 2, 3),
                    new MagicSourceBlocks(Blocks.LANTERN, 3, 4),
                    new MagicSourceBlocks(Blocks.CHEST, 2, 3),
                    new MagicSourceBlocks(Blocks.WATER, 1, 2)
            }, new MagicSourceBlockTags[]{
            new MagicSourceBlockTags(BlockTags.CORAL_BLOCKS, 1, 50),
            new MagicSourceBlockTags(BlockTags.CORALS, 1, 150),
            new MagicSourceBlockTags(BlockTags.CORAL_PLANTS, 2, 20),
            new MagicSourceBlockTags(BlockTags.WALL_CORALS, 2, 20)
            },
            new String[]{"shipwreck"},
            new RuneLoot(new String[]{"minecraft:chests/shipwreck_treasure"}, 0.33f),
            new RuneTrade(null, 0, 0),
            0x1E90FF
    ),    // Blau

    UNDERWATER_RUIN("underwater_ruin",
            new Supplier[]{
                    () -> Enchantments.IMPALING,
                    () -> Enchantments.RIPTIDE,
                    () -> Enchantments.CHANNELING,
                    () -> Enchantments.LOYALTY},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.PRISMARINE, 3, 4),
                    new MagicSourceBlocks(Blocks.PRISMARINE_BRICKS, 3, 4),
                    new MagicSourceBlocks(Blocks.DARK_PRISMARINE, 4, 5),
                    new MagicSourceBlocks(Blocks.SEA_LANTERN, 3, 4),
                    new MagicSourceBlocks(Blocks.KELP, 1, 2),
                    new MagicSourceBlocks(Blocks.WATER, 1, 2)
            }, new MagicSourceBlockTags[]{},
            new String[]{"underwater_ruin_big", "underwater_ruin_small"},
            new RuneLoot(new String[]{"minecraft:chests/underwater_ruin_big", "minecraft:chests/underwater_ruin_small"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0x00CED1
    ), // Türkis

    DESERT_PYRAMID("desert_pyramid",
            new Supplier[]{
                    () -> Enchantments.FALL_PROTECTION,
                    () -> Enchantments.BLAST_PROTECTION,
                    () -> Enchantments.THORNS},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.SANDSTONE, 2, 3),
                    new MagicSourceBlocks(Blocks.ORANGE_TERRACOTTA, 2, 3),
                    new MagicSourceBlocks(Blocks.SAND, 1, 2),
                    new MagicSourceBlocks(Blocks.RED_SAND, 1, 2),
                    new MagicSourceBlocks(Blocks.RED_SANDSTONE, 2, 3),
                    new MagicSourceBlocks(Blocks.TNT, 5, 5),
                    new MagicSourceBlocks(Blocks.CACTUS, 5, 5)
            }, new MagicSourceBlockTags[]{},
            new String[]{"desert_pyramid"},
            new RuneLoot(new String[]{"minecraft:chests/desert_pyramid"}, 0.25f),
            new RuneTrade(null, 0, 0),
            0xFFA500), // Orange

    JUNGLE_PYRAMID("jungle_pyramid",
            new Supplier[]{
                    () -> Enchantments.PROJECTILE_PROTECTION,
                    () -> Enchantments.KNOCKBACK},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.JUNGLE_LOG, 2, 3),
                    new MagicSourceBlocks(Blocks.JUNGLE_PLANKS, 2, 3),
                    new MagicSourceBlocks(Blocks.VINE, 1, 2),
                    new MagicSourceBlocks(Blocks.COBWEB, 2000, 2000),
                    new MagicSourceBlocks(Blocks.MOSSY_COBBLESTONE, 2, 3),
                    new MagicSourceBlocks(Blocks.MELON, 2, 3),
                    new MagicSourceBlocks(Blocks.BAMBOO, 2, 3)
            }, new MagicSourceBlockTags[]{},
            new String[]{"jungle_pyramid"},
            new RuneLoot(new String[]{"minecraft:chests/jungle_temple"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0x228B22), // Grün

    BURIED_TREASURE("buried_treasure",
            new Supplier[]{
                    () -> Enchantments.FISHING_LUCK,
                    () -> Enchantments.FISHING_SPEED},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.SAND, 1, 2),
                    new MagicSourceBlocks(Blocks.GRAVEL, 1, 2),
                    new MagicSourceBlocks(Blocks.CHEST, 3, 4),
                    new MagicSourceBlocks(Blocks.WATER, 2, 3),
                    new MagicSourceBlocks(Blocks.KELP, 1, 2)
            }, new MagicSourceBlockTags[]{
            new MagicSourceBlockTags(BlockTags.CORAL_BLOCKS, 1, 50),
            new MagicSourceBlockTags(BlockTags.CORALS, 1, 150),
            new MagicSourceBlockTags(BlockTags.CORAL_PLANTS, 2, 20),
            new MagicSourceBlockTags(BlockTags.WALL_CORALS, 2, 20),
            },
            new String[]{"buried_treasure"},
            new RuneLoot(new String[]{"minecraft:chests/buried_treasure"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0xDAA520
    ), // Goldenrod

    IGLOO("igloo",
            new Supplier[]{
                    () -> Enchantments.FROST_WALKER},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.SNOW_BLOCK, 2, 3),
                    new MagicSourceBlocks(Blocks.ICE, 2, 3),
                    new MagicSourceBlocks(Blocks.BLUE_ICE, 2, 3),
                    new MagicSourceBlocks(Blocks.PACKED_ICE, 50, 600),
                    new MagicSourceBlocks(Blocks.SNOW, 1, 2),
                    new MagicSourceBlocks(Blocks.SPRUCE_LOG, 2, 3),
                    new MagicSourceBlocks(Blocks.SPRUCE_PLANKS, 1, 2)
            }, new MagicSourceBlockTags[]{},
            new String[]{"igloo"},
            new RuneLoot(new String[]{"minecraft:chests/igloo_chest"}, 1.0f),
            new RuneTrade(null, 0, 0),
            0x00BFFF),           // Hellblau

    STRONGHOLD("stronghold",
            new Supplier[]{
                    () -> Enchantments.INFINITY_ARROWS},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.STONE_BRICKS, 2, 3),
                    new MagicSourceBlocks(Blocks.CRACKED_STONE_BRICKS, 2, 3),
                    new MagicSourceBlocks(Blocks.END_PORTAL_FRAME, 5, 5),
                    new MagicSourceBlocks(Blocks.IRON_BARS, 2, 3),
                    new MagicSourceBlocks(Blocks.SMOOTH_STONE, 1, 2),
                    new MagicSourceBlocks(Blocks.BOOKSHELF, 1, 2),
                    new MagicSourceBlocks(Blocks.COBWEB, 1, 2)
            }, new MagicSourceBlockTags[]{},
            new String[]{"stronghold"},
            new RuneLoot(new String[]{"minecraft:chests/stronghold_library"}, 1.0f),
            new RuneTrade(null, 0, 0),
            0x696969),  // Grau

    END_CITY("end_city",
            new Supplier[]{
                    ModEnchantments.VEIL_OF_ETERNITY},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.END_STONE, 1, 2),
                    new MagicSourceBlocks(Blocks.END_STONE_BRICKS, 2, 3),
                    new MagicSourceBlocks(Blocks.PURPUR_BLOCK, 2, 3),
                    new MagicSourceBlocks(Blocks.PURPUR_PILLAR, 2, 3),
                    new MagicSourceBlocks(Blocks.END_ROD, 1, 2),
                    new MagicSourceBlocks(Blocks.ENDER_CHEST, 900, 900),
                    new MagicSourceBlocks(Blocks.SHULKER_BOX, 900, 900)
            }, new MagicSourceBlockTags[]{},
            new String[]{"end_city"},
            new RuneLoot(new String[]{"minecraft:chests/end_city_treasure"}, 0.5f),
            new RuneTrade(null, 0, 0),
            0x9370DB),      // Medium Purple

    COURSES("courses",
            new Supplier[]{
                    () -> Enchantments.BINDING_CURSE,
                    () -> Enchantments.VANISHING_CURSE},
            new MagicSourceBlocks[]{
                    new MagicSourceBlocks(Blocks.BOOKSHELF, 2, 3),
                    new MagicSourceBlocks(Blocks.ENCHANTING_TABLE, 4, 5),
                    new MagicSourceBlocks(Blocks.OBSIDIAN, 3, 4),
                    new MagicSourceBlocks(Blocks.DARK_OAK_LOG, 2, 3)
            }, new MagicSourceBlockTags[]{},
            new String[]{"courses"},
            new RuneLoot(new String[]{""}, 0.0f),
            new RuneTrade(null, 0, 0),
            0xFF69B4);        // Pink

    public final String id;
    public Enchantment[] enchantments;
    private final Supplier<Enchantment>[] enchantmentSuppliers;
    public final MagicSourceBlocks[] blocks;

    // Palette für direkte Blocks
    public final Map<Block, MagicSourceBlocks> blockPalette;

    // Palette für TagBlocks
    public final Map<TagKey<Block>, MagicSourceBlockTags> tagPalette;
    public final String[] structureIds;
    public final RuneLoot runeLoot;
    public final RuneTrade runeTrade;
    public final int baseColor;

    RuneType(String id,
             Supplier<Enchantment>[] enchantmentsSuppliers,
             MagicSourceBlocks[] blocks,
             MagicSourceBlockTags[] tags,
             String[] structureIds,
             RuneLoot runeLoot,
             RuneTrade runeTrade,
             int baseColor) {

        this.id = id;
        this.enchantmentSuppliers = enchantmentsSuppliers;
        this.blocks = blocks;
        this.structureIds = structureIds;
        this.runeLoot = runeLoot;
        this.runeTrade = runeTrade;
        this.baseColor = baseColor;

        // BLOCK PALETTE
        this.blockPalette = new HashMap<>();
        for (MagicSourceBlocks rb : blocks) {
            blockPalette.put(rb.block, rb);
        }

        // TAG PALETTE
        this.tagPalette = new HashMap<>();
        for (MagicSourceBlockTags rt : tags) {
            tagPalette.put(rt.tag, rt);
        }
    }

    public RuneColorTheme getTheme() {
        return RuneColorTheme.fromBaseColor(baseColor);
    }

    public Enchantment[] getEnchantments() {
        return Arrays.stream(enchantmentSuppliers)
                .map(Supplier::get)
                .toArray(Enchantment[]::new);
    }
}

