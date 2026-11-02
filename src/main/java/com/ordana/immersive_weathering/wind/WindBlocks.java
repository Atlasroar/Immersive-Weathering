package com.ordana.immersive_weathering.wind;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class WindBlocks {
    private WindBlocks() {}

    public static BlockState unlitTorch(BlockState state) {
        if (state.is(Blocks.TORCH)) return ModBlocks.UNLIT_TORCH.get().defaultBlockState();
        if (state.is(Blocks.SOUL_TORCH)) return ModBlocks.UNLIT_SOUL_TORCH.get().defaultBlockState();
        if (state.is(Blocks.WALL_TORCH)) return ModBlocks.UNLIT_WALL_TORCH.get().withPropertiesOf(state);
        if (state.is(Blocks.SOUL_WALL_TORCH)) return ModBlocks.UNLIT_SOUL_WALL_TORCH.get().withPropertiesOf(state);
        return null;
    }

    public static boolean applicableFlame(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(BlockTags.CANDLES) || state.is(BlockTags.CANDLE_CAKES)
                || state.is(BlockTags.CAMPFIRES);
    }

    public static boolean sourceWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    public static void interact(ServerLevel level, FreezingWindEntity gust) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;
        // 3 x 4 x 3 cells, including the surface immediately beneath the gust.
        BlockPos origin = gust.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 2, 1))) {
            if (level.isOutsideBuildHeight(pos) || level.isOutsideBuildHeight(pos.above())
                    || !level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
                    || !level.getWorldBorder().isWithinBounds(pos)) continue;
            BlockHitResult hit = level.clip(new ClipContext(gust.position().add(0, 0.5, 0), Vec3.atCenterOf(pos),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, gust));
            if (hit.getType() != HitResult.Type.MISS && !hit.getBlockPos().equals(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (CommonConfigs.WIND_EXTINGUISH.get() && extinguish(level, pos, state)) continue;
            if (CommonConfigs.WIND_FREEZE_WATER.get() && sourceWater(state) && level.canSeeSky(pos.above())) {
                BlockState ice = ModBlocks.THIN_ICE.get().defaultBlockState();
                if (ice.canSurvive(level, pos)) level.setBlockAndUpdate(pos, ice);
            } else if (!CommonConfigs.DISABLE_ICICLES.get() && state.isAir()
                    && level.random.nextDouble() < CommonConfigs.WIND_ICICLE_CHANCE.get()) {
                BlockState icicle = ModBlocks.ICICLE.get().defaultBlockState()
                        .setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN);
                if (icicle.canSurvive(level, pos)) level.setBlockAndUpdate(pos, icicle);
            }
        }
    }

    public static boolean extinguish(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState torch = unlitTorch(state);
        if (torch != null) {
            level.setBlockAndUpdate(pos, torch);
        } else if (state.is(BlockTags.FIRE)) {
            level.removeBlock(pos, false);
        } else if (applicableFlame(state) && state.hasProperty(BlockStateProperties.LIT)
                && state.getValue(BlockStateProperties.LIT)) {
            if (state.getBlock() instanceof AbstractCandleBlock) {
                AbstractCandleBlock.extinguish(null, state, level, pos);
                return true;
            }
            if (state.getBlock() instanceof CampfireBlock) CampfireBlock.dowse(null, level, pos, state);
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false));
        } else {
            return false;
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1);
        level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }
}
