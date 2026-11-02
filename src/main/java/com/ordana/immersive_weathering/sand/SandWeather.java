package com.ordana.immersive_weathering.sand;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.sandstorm.Sandstorms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SandWeather {
    public static boolean erode(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (!CommonConfigs.SAND_RAIN_EROSION.get() || SandHosts.layers(state) == 0
                || !level.isRainingAt(pos.above())) return false;
        var biome = level.getBiome(pos);
        if (biome.is(Sandstorms.REGULAR) || biome.is(Sandstorms.RED)) return false;
        double chance = CommonConfigs.SAND_RAIN_EROSION_CHANCE.get();
        if (chance == 0 || random.nextDouble() >= chance) return false;
        int layers = SandHosts.layers(state);
        BlockState next = layers > 1 ? state.setValue(LayerBlock.LAYERS_8, layers - 1)
                : state.getBlock() instanceof SandCoveredBlock ? SandHosts.raw(state) : Blocks.AIR.defaultBlockState();
        return level.setBlockAndUpdate(pos, next);
    }

    private SandWeather() {}
}
