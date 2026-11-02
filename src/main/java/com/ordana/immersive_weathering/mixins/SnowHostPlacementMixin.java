package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.snow.SnowHosts;
import com.ordana.immersive_weathering.snow.SnowCoveredBlock;
import com.ordana.immersive_weathering.snow.SnowSettings;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class SnowHostPlacementMixin {
    @Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    private void snowHostPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        if (context.getItemInHand().is(Items.SNOW)) return;
        BlockState placement = cir.getReturnValue();
        BlockState old = context.getLevel().getBlockState(context.getClickedPos());
        boolean replaceableCover = old.getBlock() instanceof SnowCoveredBlock && old.canBeReplaced(context);
        if (placement != null && replaceableCover) {
            BlockState raw = SnowHosts.raw(old);
            if (raw.getBlock() instanceof PinkPetalsBlock && placement.is(raw.getBlock())) {
                placement = raw.setValue(PinkPetalsBlock.AMOUNT, Math.min(4, raw.getValue(PinkPetalsBlock.AMOUNT) + 1));
                cir.setReturnValue(placement);
            }
        }
        if (placement != null && SnowSettings.mixing() && (old.is(Blocks.SNOW) || replaceableCover)
                && SnowHosts.layers(old) < 8) {
            BlockState covered = SnowHosts.cover(placement, SnowHosts.layers(old));
            if (covered != null) cir.setReturnValue(covered);
        }
    }
}
