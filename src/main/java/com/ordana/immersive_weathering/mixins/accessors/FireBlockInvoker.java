package com.ordana.immersive_weathering.mixins.accessors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    @Invoker("getIgniteOdds")
    int immersiveWeathering$igniteOdds(LevelReader level, BlockPos pos);
}
