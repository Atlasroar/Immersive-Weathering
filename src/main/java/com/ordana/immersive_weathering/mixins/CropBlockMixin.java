package com.ordana.immersive_weathering.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ordana.immersive_weathering.reg.ModTags;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin extends Block {
    protected CropBlockMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
    public void canPlantOnTop(BlockState floor, BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (floor.is(ModTags.FERTILE_BLOCKS)) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
        ordinal = 0))
    private static boolean fertileBlocks(BlockState instance, Block block, Operation<Boolean> original) {
        if (instance.is(ModTags.FERTILE_BLOCKS)) {
            return true;
        }
        return original.call(instance, block);
    }

    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean snowHostIdentity(BlockState state, Block block, Operation<Boolean> original) {
        return original.call(SnowHosts.raw(state), block);
    }

}