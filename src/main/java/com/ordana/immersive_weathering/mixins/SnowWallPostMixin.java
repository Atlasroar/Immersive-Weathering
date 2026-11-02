package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WallBlock.class)
public class SnowWallPostMixin {
    @ModifyVariable(method = "shouldRaisePost", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private BlockState snowCoveredPost(BlockState above) {
        return com.ordana.immersive_weathering.sand.SandHosts.raw(SnowHosts.raw(above));
    }
}
