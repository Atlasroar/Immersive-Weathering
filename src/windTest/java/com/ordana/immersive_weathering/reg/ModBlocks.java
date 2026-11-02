package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.blocks.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Supplier;

/** Registration substitute: actual production blocks and Mojang state containers. */
public final class ModBlocks {
    public static final Supplier<Block> UNLIT_TORCH = () -> Standing.NORMAL;
    public static final Supplier<Block> UNLIT_SOUL_TORCH = () -> Standing.SOUL;
    public static final Supplier<Block> UNLIT_WALL_TORCH = () -> Standing.WALL;
    public static final Supplier<Block> UNLIT_SOUL_WALL_TORCH = () -> Standing.SOUL_WALL;
    public static final Supplier<Block> THIN_ICE = () -> Standing.ICE;
    public static final Supplier<Block> ICICLE = () -> Standing.ICICLE;

    private static final class Standing {
        static final Block NORMAL = new UnlitTorchBlock(false, BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(s -> 0));
        static final Block SOUL = new UnlitTorchBlock(true, BlockBehaviour.Properties.copy(Blocks.SOUL_TORCH).lightLevel(s -> 0));
        static final Block WALL = new UnlitWallTorchBlock(false, BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(s -> 0));
        static final Block SOUL_WALL = new UnlitWallTorchBlock(true, BlockBehaviour.Properties.copy(Blocks.SOUL_WALL_TORCH).lightLevel(s -> 0));
        static final Block ICE = new ThinIceBlock(BlockBehaviour.Properties.copy(Blocks.ICE));
        static final Block ICICLE = new IcicleBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE));
    }
}
