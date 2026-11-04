package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

/** Native IW block classes, registered before vanilla registries freeze. */
public final class ModBlocks {
    public static final Supplier<net.minecraft.world.level.block.Block> WEEDS = () -> Plants.WEEDS;
    public static final Supplier<net.minecraft.world.level.block.Block> DUNE_GRASS = () -> Plants.DUNE;
    public static final Supplier<net.minecraft.world.level.block.Block> FROSTY_GRASS = () -> Plants.GRASS;
    public static final Supplier<net.minecraft.world.level.block.Block> FROSTY_FERN = () -> Plants.FERN;
    public static final Supplier<com.ordana.immersive_weathering.blocks.IvyBlock> IVY = () -> Plants.IVY;
    public static final Supplier<net.minecraft.world.level.block.Block> MOSS = () -> Plants.MOSS;
    public static final Supplier<net.minecraft.world.level.block.Block> HANGING_ROOTS_WALL = () -> Plants.ROOTS;
    private static final class Plants {
        static final net.minecraft.world.level.block.Block WEEDS = new com.ordana.immersive_weathering.blocks.WeedsBlock(BlockBehaviour.Properties.copy(Blocks.GRASS));
        static final net.minecraft.world.level.block.Block DUNE = new com.ordana.immersive_weathering.blocks.DuneGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS));
        static final net.minecraft.world.level.block.Block GRASS = new com.ordana.immersive_weathering.blocks.frosted.FrostyGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS));
        static final net.minecraft.world.level.block.Block FERN = new com.ordana.immersive_weathering.blocks.frosted.FrostyGrassBlock(BlockBehaviour.Properties.copy(Blocks.FERN));
        static final com.ordana.immersive_weathering.blocks.IvyBlock IVY = new com.ordana.immersive_weathering.blocks.IvyBlock(BlockBehaviour.Properties.copy(Blocks.VINE));
        static final net.minecraft.world.level.block.Block MOSS = new com.ordana.immersive_weathering.blocks.MossMultifaceBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN));
        static final net.minecraft.world.level.block.Block ROOTS = new com.ordana.immersive_weathering.blocks.WallRootsBlock(BlockBehaviour.Properties.copy(Blocks.HANGING_ROOTS));
    }
    public static <T extends net.minecraft.world.level.block.Block> Supplier<T> regBlock(String name, Supplier<T> supplier) {
        T block = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK,
                "immersive_weathering:" + name, supplier.get());
        return () -> block;
    }
    public static final SandLayerBlock SAND = new SandLayerBlock(0xDBD3A0, BlockBehaviour.Properties.copy(Blocks.SAND));
    public static final SandLayerBlock RED = new SandLayerBlock(0xA95821, BlockBehaviour.Properties.copy(Blocks.RED_SAND));
    public static final Supplier<net.minecraft.world.level.block.Block> SAND_LAYER_BLOCK = () -> SAND;
    public static final Supplier<net.minecraft.world.level.block.Block> RED_SAND_LAYER_BLOCK = () -> RED;
}
