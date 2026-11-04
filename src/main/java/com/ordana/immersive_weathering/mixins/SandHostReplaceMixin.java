package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.sand.SandHosts;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class SandHostReplaceMixin {
    @Inject(method = "canBeReplaced(Lnet/minecraft/world/item/context/BlockPlaceContext;)Z",
            at = @At("HEAD"), cancellable = true)
    private void sandIntoHost(BlockPlaceContext context, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = (BlockState) (Object) this;
        if (SandHosts.canAdd(context, state)) cir.setReturnValue(true);
    }
}
