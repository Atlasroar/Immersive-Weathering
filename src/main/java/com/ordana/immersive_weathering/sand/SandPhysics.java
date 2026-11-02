package com.ordana.immersive_weathering.sand;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import com.ordana.immersive_weathering.entities.FallingLayerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SandPhysics {
    public static boolean supported(LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.getFluidState().isEmpty() && below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    public static void checkSupport(ServerLevel level, BlockPos pos, BlockState state) {
        if (supported(level, pos)) return;
        SandLayerBlock pile = SandHosts.pile(state);
        if (pile == null) return;
        FallingLayerEntity falling = new FallingLayerEntity(level, pos,
                pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, SandHosts.layers(state)));
        if (level.addFreshEntity(falling)) {
            BlockState replacement = state.getBlock() instanceof SandCoveredBlock
                    ? SandHosts.raw(state) : Blocks.AIR.defaultBlockState();
            if (!level.setBlockAndUpdate(pos, replacement)) falling.discard();
        }
    }

    public static int deposit(Level level, BlockPos pos, SandLayerBlock pile, int incoming) {
        if (incoming < 1 || incoming > 8 || level.isOutsideBuildHeight(pos)
                || level.isOutsideBuildHeight(pos.below()) || !level.hasChunkAt(pos)
                || !level.hasChunkAt(pos.below()) || !level.getWorldBorder().isWithinBounds(pos)) return incoming;
        BlockState old = level.getBlockState(pos);
        if (!old.getFluidState().isEmpty() || old.hasBlockEntity() || !supported(level, pos)
                || SandHosts.layers(level.getBlockState(pos.below())) > 0) return incoming;
        SandLayerBlock existingPile = SandHosts.pile(old);
        if (existingPile != null && existingPile != pile) return incoming;
        int existing = SandHosts.layers(old);
        int added = Math.min(8 - existing, incoming);
        if (added <= 0) return incoming;
        BlockState next;
        if (old.is(pile)) next = old.setValue(LayerBlock.LAYERS_8, existing + added);
        else if (com.ordana.immersive_weathering.configs.CommonConfigs.SAND_HOSTS.get() && SandHosts.supported(old)) {
            if (!SandHosts.raw(old).canSurvive(level, pos)) return incoming;
            next = SandHosts.cover(old, pile, existing + added);
        } else if (old.isAir()) next = pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, added);
        else return incoming;
        if (next == null) return incoming;
        Block.pushEntitiesUp(old, next, level, pos);
        return level.setBlockAndUpdate(pos, next) ? incoming - added : incoming;
    }

    public static void tick(FallingLayerEntity entity, SandLayerBlock pile) {
        Level level = entity.level();
        entity.time++;
        if (!entity.isNoGravity()) entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.04, 0));
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        BlockPos pos = entity.blockPosition();
        int remaining = SandHosts.layers(entity.getBlockState());
        if (entity.onGround()) {
            remaining = deposit(level, pos, pile, remaining);
            if (remaining > 0 && SandHosts.supported(level.getBlockState(pos.below()))) {
                remaining = deposit(level, pos.below(), pile, remaining);
            }
        } else if (entity.time <= 600 && pos.getY() >= level.getMinBuildHeight()) {
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.98));
            return;
        }
        if (remaining > 0 && entity.dropItem && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            Block.popResource(level, pos, new ItemStack(pile, remaining));
        }
        entity.discard();
    }

    private SandPhysics() {}
}
