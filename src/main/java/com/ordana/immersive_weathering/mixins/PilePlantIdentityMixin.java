package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.piles.PileCoveredBlock;
import com.ordana.immersive_weathering.piles.PilePlants;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class PilePlantIdentityMixin {
    @Inject(method = "is(Lnet/minecraft/world/level/block/Block;)Z", at = @At("RETURN"), cancellable = true)
    private void pileHostIdentity(Block block, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = (BlockState) (Object) this;
        if (!cir.getReturnValue() && state.getBlock() instanceof PileCoveredBlock covered
                && covered.host() == block) cir.setReturnValue(true);
    }

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"), cancellable = true)
    private void pileHostTag(TagKey<Block> tag, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = (BlockState) (Object) this;
        if (!cir.getReturnValue() && state.getBlock() instanceof PileCoveredBlock
                && PilePlants.raw(state).is(tag)) cir.setReturnValue(true);
    }
}
