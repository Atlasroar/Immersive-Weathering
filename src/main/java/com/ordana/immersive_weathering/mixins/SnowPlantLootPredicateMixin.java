package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.piles.PileCoveredBlock;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(BlockPredicate.class)
public class SnowPlantLootPredicateMixin {
    @Shadow @Final private Set<Block> blocks;
    @Shadow @Final private TagKey<Block> tag;
    @Shadow @Final private StatePropertiesPredicate properties;
    @Shadow @Final private NbtPredicate nbt;

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void mixedPlantLocation(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || blocks == null || nbt != NbtPredicate.ANY || !level.isLoaded(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof PileCoveredBlock covered
                && blocks.contains(covered.host()) && (tag == null || state.is(tag))
                && properties.matches(SnowHosts.raw(state))) cir.setReturnValue(true);
    }
}
