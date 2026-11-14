package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.piles.PilePlants;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BlockItem.class)
public class PileHostPlacementMixin {
    @ModifyArgs(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private void pileHostPlacement(Args args) {
        BlockPlaceContext context = args.get(0);
        BlockState placement = args.get(1);
        args.set(1, PilePlants.placement(context, placement));
    }
}
