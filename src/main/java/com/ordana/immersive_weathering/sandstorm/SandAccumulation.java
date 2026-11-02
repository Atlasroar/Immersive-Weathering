/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt.
 */
package com.ordana.immersive_weathering.sandstorm;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SandAccumulation {
    public static void tickChunk(ServerLevel level, LevelChunk chunk) {
        if (!Sandstorms.ownsStorms() || !CommonConfigs.SANDSTORM_DEPOSITION.get() || !level.isRaining()) return;
        var chunkPos = chunk.getPos();
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                level.getBlockRandomPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), 15));
        Sandstorms.Type type = Sandstorms.exposedStorm(level, top);
        if (type == Sandstorms.Type.NONE || level.random.nextInt(16) != 0) return;
        LayerBlock pile = (LayerBlock) (type == Sandstorms.Type.REGULAR
                ? ModBlocks.SAND_LAYER_BLOCK.get() : ModBlocks.RED_SAND_LAYER_BLOCK.get());
        deposit(level, top, pile, CommonConfigs.SANDSTORM_LAYER_CAP.get());
    }

    public static boolean deposit(Level level, BlockPos top, LayerBlock pile, int cap) {
        if (cap < 1 || level.isOutsideBuildHeight(top) || level.isOutsideBuildHeight(top.below()) || !level.hasChunkAt(top)
                || !level.getWorldBorder().isWithinBounds(top)) return false;
        // MOTION_BLOCKING returns the cell above a partial pile, not the pile itself.
        BlockState ground = level.getBlockState(top.below());
        if (ground.getBlock() instanceof LayerBlock && !ground.is(pile)) return false;
        BlockPos pos = ground.is(pile) ? top.below() : top;
        if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos.below())
                || level.isOutsideBuildHeight(pos.below())) return false;
        BlockState current = level.getBlockState(pos);
        BlockState support = level.getBlockState(pos.below());
        if ((!current.isAir() && !current.is(pile)) || !current.getFluidState().isEmpty()
                || !support.getFluidState().isEmpty() || !support.isFaceSturdy(level, pos.below(), Direction.UP)
                || !pile.defaultBlockState().canSurvive(level, pos)) return false;
        int layers = current.is(pile) ? pile.getLayers(current) : 0;
        if (layers >= Math.min(cap, pile.getMaxLayers())) return false;
        BlockState next = pile.defaultBlockState().setValue(pile.layerProperty(), layers + 1);
        Block.pushEntitiesUp(current, next, level, pos);
        return level.setBlock(pos, next, Block.UPDATE_ALL);
    }

    private SandAccumulation() {}
}
