package com.ordana.immersive_weathering.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractMegaTreeGrower.class)
public class SnowMegaSaplingMixin {
    @WrapOperation(method = "isTwoByTwoSapling", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"), require = 4)
    private static boolean snowHostIdentity(BlockState state, Block block, Operation<Boolean> original) {
        return original.call(SnowHosts.raw(state), block);
    }
}
