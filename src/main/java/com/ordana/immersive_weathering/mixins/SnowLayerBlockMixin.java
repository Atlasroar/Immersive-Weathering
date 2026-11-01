package com.ordana.immersive_weathering.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.ordana.immersive_weathering.snow.SnowHosts;
import com.ordana.immersive_weathering.snow.SnowPhysics;
import com.ordana.immersive_weathering.snow.SnowSettings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowLayerBlock.class)
public abstract class SnowLayerBlockMixin extends Block {

    protected SnowLayerBlockMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    private void replacePartialSnow(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        if (SnowSettings.replacement() && state.getValue(SnowLayerBlock.LAYERS) < 8
                && context.getItemInHand().getItem() instanceof BlockItem && !context.getItemInHand().is(Items.SNOW)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void coveredSnowSupport(BlockState state, net.minecraft.world.level.LevelReader level,
                                      BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (SnowPhysics.coveredSupport(level.getBlockState(pos.below()))) cir.setReturnValue(true);
    }

    @Inject(method = "getStateForPlacement", at = @At("HEAD"), cancellable = true)
    private void coverHost(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (SnowHosts.canAddSnow(context, state)) {
            BlockState covered = SnowHosts.cover(state, SnowHosts.layers(state) + 1);
            if (covered != null) cir.setReturnValue(covered);
        }
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void scheduleFalling(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos otherPos, CallbackInfoReturnable<BlockState> cir) {
        if (SnowSettings.falling()) {
            level.scheduleTick(pos, (Block) (Object) this, 2);
            cir.setReturnValue(state);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        super.onPlace(state, level, pos, old, moving);
        if (SnowSettings.falling()) level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SnowPhysics.checkSupport(level, pos, state);
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"))
    protected void melt(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
        if (serverLevel.isRainingAt(blockPos.above())) {
            serverLevel.removeBlock(blockPos, false);
        }
    }
}
