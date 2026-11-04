package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.piles.PileInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
public class PilePlantWriteMixin {
    @ModifyVariable(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), argsOnly = true)
    private BlockState retainPlantCover(BlockState next, BlockPos pos) {
        return PileInteractions.retain((Level) (Object) this, pos, next);
    }
}
