package com.ordana.immersive_weathering.snow;

import com.ordana.immersive_weathering.piles.PileCoveredBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SnowCoveredBlock extends PileCoveredBlock {
    private SnowCoveredBlock(Block host) { super(host); }

    public static SnowCoveredBlock create(Block host) {
        return create(host, () -> new SnowCoveredBlock(host));
    }

    @Override protected BlockState pileState(BlockState state) {
        return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, SnowHosts.layers(state));
    }
    @Override protected BlockState cover(BlockState state, int layers) { return SnowHosts.cover(state, layers); }
    @Override protected boolean canAdd(BlockPlaceContext context, BlockState state) { return SnowHosts.canAddSnow(context, state); }
    @Override protected boolean replacement(BlockState state) {
        return SnowSettings.replacement() && SnowHosts.layers(state) < 8;
    }
    @Override protected boolean isPileItem(ItemStack stack) { return stack.is(Items.SNOW); }
    @Override protected boolean hasPileSupport(net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return SnowPhysics.supported(level, pos);
    }
    @Override protected void checkSupport(ServerLevel level, BlockPos pos, BlockState state) {
        SnowPhysics.checkSupport(level, pos, state);
    }
    @Override protected boolean weatherTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBrightness(LightLayer.BLOCK, pos) <= 11 && !level.isRainingAt(pos.above())) return false;
        level.setBlockAndUpdate(pos, raw(state));
        return true;
    }
}
