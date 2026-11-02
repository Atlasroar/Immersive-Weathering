package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

/** Native IW block classes, registered before vanilla registries freeze. */
public final class ModBlocks {
    public static final SandLayerBlock SAND = new SandLayerBlock(0xDBD3A0, BlockBehaviour.Properties.copy(Blocks.SAND));
    public static final SandLayerBlock RED = new SandLayerBlock(0xA95821, BlockBehaviour.Properties.copy(Blocks.RED_SAND));
    public static final Supplier<net.minecraft.world.level.block.Block> SAND_LAYER_BLOCK = () -> SAND;
    public static final Supplier<net.minecraft.world.level.block.Block> RED_SAND_LAYER_BLOCK = () -> RED;
}
