package com.ordana.immersive_weathering.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class UnlitWallTorchBlock extends WallTorchBlock {
    private final boolean soul;

    public UnlitWallTorchBlock(boolean soul, Properties properties) {
        super(properties, ParticleTypes.SMOKE);
        this.soul = soul;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {}

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(soul ? Items.SOUL_TORCH : Items.TORCH);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return UnlitTorches.relight((soul ? Blocks.SOUL_WALL_TORCH : Blocks.WALL_TORCH).withPropertiesOf(state),
                level, pos, player, hand);
    }
}
