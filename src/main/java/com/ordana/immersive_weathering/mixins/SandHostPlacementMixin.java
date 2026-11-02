package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.sand.SandHosts;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class SandHostPlacementMixin {
    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    private void sandHostPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(SandHosts.placement(context, cir.getReturnValue()));
    }
}
