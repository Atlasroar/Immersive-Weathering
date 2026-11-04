package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.piles.PileCoveredBlock;
import com.ordana.immersive_weathering.snow.SnowHosts;
import com.ordana.immersive_weathering.snow.SnowSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoublePlantBlock.class)
public class SnowDoublePlantMixin {
    @Inject(method = "preventCreativeDropFromBottomPart", at = @At("HEAD"), cancellable = true)
    private static void destroyMixedCreativePair(Level level, BlockPos pos, BlockState state, Player player, CallbackInfo ci) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                && level.getBlockState(pos.below()).getBlock() instanceof PileCoveredBlock
                && SnowHosts.raw(level.getBlockState(pos.below())).is(state.getBlock())) {
            level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 35);
            ci.cancel();
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void surviveMixedPair(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                && level.getBlockState(pos.below()).getBlock() instanceof PileCoveredBlock) {
            cir.setReturnValue(SnowHosts.hostSurvives(state, level, pos));
        }
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void updateMixedPair(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos otherPos, CallbackInfoReturnable<BlockState> cir) {
        if (neighbor.getBlock() instanceof PileCoveredBlock) {
            cir.setReturnValue(SnowHosts.doubleShape(state, direction, neighbor, level, pos));
        }
    }

    @Inject(method = "setPlacedBy", at = @At("HEAD"), cancellable = true)
    private void preserveUpperSnow(Level level, BlockPos pos, BlockState state, LivingEntity entity,
                                    ItemStack stack, CallbackInfo ci) {
        BlockState above = level.getBlockState(pos.above());
        boolean replaceableCover = above.getBlock() instanceof PileCoveredBlock
                && above.canBeReplaced(new DirectionalPlaceContext(level, pos.above(), Direction.DOWN, stack, Direction.UP));
        var sand = com.ordana.immersive_weathering.sand.SandHosts.pile(above);
        if (sand != null && com.ordana.immersive_weathering.configs.CommonConfigs.SAND_HOSTS.get()
                && (!(above.getBlock() instanceof PileCoveredBlock) || replaceableCover)
                && com.ordana.immersive_weathering.sand.SandHosts.layers(above) < 8) {
            BlockState covered = com.ordana.immersive_weathering.sand.SandHosts.cover(
                    state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), sand,
                    com.ordana.immersive_weathering.sand.SandHosts.layers(above));
            if (covered != null) {
                level.setBlock(pos.above(), covered, 3);
                ci.cancel();
                return;
            }
        }
        if (SnowSettings.mixing() && (above.is(Blocks.SNOW) || replaceableCover) && SnowHosts.layers(above) < 8) {
            BlockState covered = SnowHosts.cover(state.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER),
                    SnowHosts.layers(above));
            if (covered != null) {
                level.setBlock(pos.above(), covered, 3);
                ci.cancel();
            }
        }
    }
}
