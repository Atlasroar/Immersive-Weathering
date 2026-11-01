package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class SnowHostReplaceMixin {
    @Inject(method = "canBeReplaced(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/item/context/BlockPlaceContext;)Z",
            at = @At("HEAD"), cancellable = true)
    private void snowIntoHost(BlockState state, BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        if (SnowHosts.canAddSnow(context, state)) cir.setReturnValue(true);
    }
}
