package com.ordana.immersive_weathering.sand;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.piles.PileCoveredBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class SandCoveredBlock extends PileCoveredBlock {
    private final SandLayerBlock pile;

    private SandCoveredBlock(Block host, SandLayerBlock pile) {
        super(host);
        this.pile = pile;
    }

    public static SandCoveredBlock create(Block host, SandLayerBlock pile) {
        return create(host, () -> new SandCoveredBlock(host, pile));
    }

    public SandLayerBlock pile() { return pile; }
    @Override protected BlockState pileState(BlockState state) {
        return pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, SandHosts.layers(state));
    }
    @Override protected BlockState cover(BlockState state, int layers) { return SandHosts.cover(state, pile, layers); }
    @Override protected boolean canAdd(BlockPlaceContext context, BlockState state) { return SandHosts.canAdd(context, state); }
    @Override protected boolean replacement(BlockState state) { return CommonConfigs.REPLACE_SAND_PILES.get(); }
    @Override protected boolean isPileItem(ItemStack stack) {
        return stack.getItem() instanceof net.minecraft.world.item.BlockItem item && item.getBlock() instanceof SandLayerBlock;
    }
    @Override protected boolean hasPileSupport(net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return SandPhysics.supported(level, pos);
    }
    @Override protected void checkSupport(ServerLevel level, BlockPos pos, BlockState state) {
        SandPhysics.checkSupport(level, pos, state);
    }
    @Override protected boolean weatherTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        return SandWeather.erode(level, pos, state, random);
    }
    @Override public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        pile.stepOn(level, pos, pileState(state), entity);
    }
    @Override public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float height) {
        pile.fallOn(level, pileState(state), pos, entity, height);
    }
}
