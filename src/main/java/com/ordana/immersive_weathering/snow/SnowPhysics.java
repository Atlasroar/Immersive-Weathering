package com.ordana.immersive_weathering.snow;

import com.ordana.immersive_weathering.entities.FallingLayerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SnowPhysics {
    private SnowPhysics() {}

    public static boolean supported(net.minecraft.world.level.LevelReader level, BlockPos pos) {
        if (com.ordana.immersive_weathering.piles.PilePlants.cultivatedFloor(level.getBlockState(pos.below()))) return true;
        if (coveredSupport(level.getBlockState(pos.below()))) return true;
        return Blocks.SNOW.defaultBlockState().canSurvive(level, pos);
    }

    public static boolean coveredSupport(BlockState below) {
        return below.getBlock() instanceof SnowCoveredBlock && SnowHosts.layers(below) == 8
                && !below.is(net.minecraft.tags.BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON);
    }

    public static void checkSupport(ServerLevel level, BlockPos pos, BlockState state) {
        if (supported(level, pos)) return;
        if (!SnowSettings.falling()) {
            if (state.getBlock() instanceof SnowCoveredBlock) level.setBlockAndUpdate(pos, SnowHosts.raw(state));
            return;
        }
        BlockState snow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, SnowHosts.layers(state));
        FallingLayerEntity falling = new FallingLayerEntity(level, pos, snow);
        if (level.addFreshEntity(falling)) {
            BlockState replacement = state.getBlock() instanceof SnowCoveredBlock
                    ? SnowHosts.raw(state) : Blocks.AIR.defaultBlockState();
            if (!level.setBlockAndUpdate(pos, replacement)) falling.discard();
        }
    }

    public static boolean sourceWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    public static void tick(FallingLayerEntity entity) {
        Level level = entity.level();
        entity.time++;
        if (!entity.isNoGravity()) entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.04, 0));
        Vec3 before = entity.position();
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        BlockPos pos = entity.blockPosition();
        int layers = SnowHosts.layers(entity.getBlockState());
        BlockHitResult hit = level.clip(new ClipContext(before, entity.position(), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.SOURCE_ONLY, entity));
        BlockPos contact = hit.getType() == HitResult.Type.MISS ? pos : hit.getBlockPos();
        if (sourceWater(level.getBlockState(contact))) {
            if (level.setBlockAndUpdate(contact, Blocks.ICE.defaultBlockState())) entity.discard();
            else finish(entity, layers, pos);
            return;
        }
        if (!level.getFluidState(pos).isEmpty()) {
            finish(entity, layers, pos);
            return;
        }
        if (entity.onGround()) {
            int remaining = deposit(level, pos, layers);
            // A fence's collision surface is above its cell; snow belongs around, not on, that fence.
            if (remaining == layers && SnowHosts.supported(level.getBlockState(pos.below()))) {
                remaining = deposit(level, pos.below(), layers);
            }
            finish(entity, remaining, pos);
            return;
        }
        if (entity.time > 600 || pos.getY() < level.getMinBuildHeight()) {
            finish(entity, layers, pos);
            return;
        }
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.98));
    }

    /** Returns undeposited layers; overflow becomes items, never an unbounded column. */
    public static int deposit(Level level, BlockPos pos, int incoming) {
        if (incoming < 1 || incoming > 8 || level.isOutsideBuildHeight(pos)) return incoming;
        BlockState old = level.getBlockState(pos);
        if (SnowHosts.layers(level.getBlockState(pos.below())) > 0) return incoming;
        if (!old.getFluidState().isEmpty() || old.hasBlockEntity() || !supported(level, pos)) return incoming;
        int existing = SnowHosts.layers(old);
        int placed = Math.min(8 - existing, incoming);
        if (placed <= 0) return incoming;
        BlockState next;
        if (old.is(Blocks.SNOW)) next = old.setValue(SnowLayerBlock.LAYERS, existing + placed);
        else if (SnowSettings.mixing() && SnowHosts.supported(old)) next = SnowHosts.cover(old, existing + placed);
        else if (old.isAir()) next = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, placed);
        else return incoming;
        return next != null && level.setBlockAndUpdate(pos, next) ? incoming - placed : incoming;
    }

    private static void finish(FallingLayerEntity entity, int remaining, BlockPos pos) {
        if (remaining > 0 && entity.dropItem && entity.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            Block.popResource(entity.level(), pos, new ItemStack(Items.SNOW, remaining));
        }
        entity.discard();
    }
}
