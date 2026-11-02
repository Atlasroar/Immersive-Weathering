/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt.
 */
package com.ordana.immersive_weathering.sandstorm;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.sand.SandHosts;
import com.ordana.immersive_weathering.sand.SandPhysics;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
        SandLayerBlock pile = (SandLayerBlock) (type == Sandstorms.Type.REGULAR
                ? ModBlocks.SAND_LAYER_BLOCK.get() : ModBlocks.RED_SAND_LAYER_BLOCK.get());
        deposit(level, top, pile, CommonConfigs.SANDSTORM_LAYER_CAP.get());
    }

    public static boolean deposit(Level level, BlockPos top, SandLayerBlock pile, int cap) {
        if (cap < 1 || level.isOutsideBuildHeight(top) || level.isOutsideBuildHeight(top.below()) || !level.hasChunkAt(top)
                || !level.getWorldBorder().isWithinBounds(top)) return false;
        // MOTION_BLOCKING returns the cell above a partial pile, not the pile itself.
        BlockState ground = level.getBlockState(top.below());
        if (ground.getBlock() instanceof LayerBlock && !ground.is(pile)) return false;
        BlockPos pos = SandHosts.pile(ground) != null || SandHosts.supported(ground) ? top.below() : top;
        BlockState current = level.getBlockState(pos);
        if (SandHosts.layers(current) >= Math.min(cap, pile.getMaxLayers())) return false;
        return SandPhysics.deposit(level, pos, pile, 1) == 0;
    }

    private SandAccumulation() {}
}
