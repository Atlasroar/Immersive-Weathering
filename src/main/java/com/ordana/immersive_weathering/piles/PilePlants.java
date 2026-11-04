package com.ordana.immersive_weathering.piles;

import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The dry vegetation inventory is shared by every mineral pile, not selected by weather. */
public final class PilePlants {
    public static List<Block> vanilla() {
        return List.of(
                Blocks.GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY,
                Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
                Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                Blocks.WITHER_ROSE, Blocks.TORCHFLOWER, Blocks.PINK_PETALS,
                Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
                Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.MANGROVE_PROPAGULE,
                Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.DEAD_BUSH,
                Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                Blocks.TORCHFLOWER_CROP, Blocks.PITCHER_CROP, Blocks.PITCHER_PLANT,
                Blocks.MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM,
                Blocks.SWEET_BERRY_BUSH, Blocks.COCOA, Blocks.NETHER_WART,
                Blocks.BAMBOO, Blocks.BAMBOO_SAPLING, Blocks.SUGAR_CANE, Blocks.CACTUS,
                Blocks.VINE, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
                Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT,
                Blocks.CRIMSON_ROOTS, Blocks.WARPED_ROOTS, Blocks.NETHER_SPROUTS,
                Blocks.CRIMSON_FUNGUS, Blocks.WARPED_FUNGUS, Blocks.HANGING_ROOTS,
                Blocks.SPORE_BLOSSOM, Blocks.GLOW_LICHEN, Blocks.MOSS_CARPET,
                Blocks.AZALEA, Blocks.FLOWERING_AZALEA,
                Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF,
                Blocks.CHORUS_PLANT, Blocks.CHORUS_FLOWER);
    }

    public static Map<String, Supplier<? extends Block>> immersive() {
        return Map.of("weeds", ModBlocks.WEEDS, "dune_grass", ModBlocks.DUNE_GRASS,
                "frosty_grass", ModBlocks.FROSTY_GRASS, "frosty_fern", ModBlocks.FROSTY_FERN,
                "ivy", ModBlocks.IVY, "moss", ModBlocks.MOSS, "hanging_roots_wall", ModBlocks.HANGING_ROOTS_WALL);
    }

    public static BlockState raw(BlockState state) {
        return state.getBlock() instanceof PileCoveredBlock covered
                ? com.ordana.immersive_weathering.snow.SnowHosts.copy(state, covered.host().defaultBlockState()) : state;
    }

    public static boolean cultivatedFloor(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.FarmBlock || state.is(Blocks.SOUL_SAND);
    }

    private PilePlants() {}
}
