package com.ordana.immersive_weathering.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ordana.immersive_weathering.piles.PilePlants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {GrowingPlantBodyBlock.class, BigDripleafStemBlock.class,
        net.minecraft.world.level.block.FarmBlock.class,
        net.minecraft.world.entity.ai.behavior.HarvestFarmland.class},
        targets = {"net.minecraft.world.entity.animal.Rabbit$RaidGardenGoal",
                "net.minecraft.world.entity.animal.Bee$BeeGrowCropGoal"})
public class PileGrowingPlantMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private static Block pileHostType(BlockState state, Operation<Block> original) {
        return original.call(PilePlants.raw(state));
    }
}
