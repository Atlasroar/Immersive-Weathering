package com.ordana.immersive_weathering.snow;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.snowstorm.Snowstorms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SnowWeather {
    private SnowWeather() {}

    public static boolean sample(Biome biome, ServerLevel level, BlockPos sampledAir) {
        if (!level.isRaining() || !level.isLoaded(sampledAir)
                || !sampledAir.equals(level.getHeightmapPos(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, sampledAir))) return false;
        int rule = level.getGameRules().getInt(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT);
        if (rule <= 0) return false;
        BlockPos target = sampledAir;
        BlockState below = level.getBlockState(target.below());
        if (SnowHosts.layers(below) > 0 || SnowHosts.supported(below)) target = target.below();
        BlockState old = level.getBlockState(target);
        int existing = SnowHosts.layers(old);
        // Keep vanilla's initial snow, including ordinary weather, without adding a second tick source.
        if (target.equals(sampledAir) && old.isAir()) return biome.shouldSnow(level, sampledAir);
        if (!CommonConfigs.SNOW_ACCUMULATION.get()
                || !weatherAllowed(level.isRaining(), level.isThundering(), CommonConfigs.SNOW_ACCUMULATION_WITHOUT_THUNDER.get())
                || level.getBrightness(LightLayer.BLOCK, target) > 11
                || biome.getPrecipitationAt(target) != Biome.Precipitation.SNOW
                || !SnowPhysics.supported(level, target)) return false;
        int cap = effectiveCap(CommonConfigs.SNOW_COLUMN_CAP.get(), rule);
        if (existing >= cap || existing == 0 && (!SnowSettings.mixing() || !SnowHosts.supported(old))) return false;
        int layers = Snowstorms.depositionLayers(existing, cap, Snowstorms.isSnowstorm(level, sampledAir),
                CommonConfigs.SNOWSTORM_DEPOSITION_BONUS.get());
        BlockState next = old.is(Blocks.SNOW) ? old.setValue(SnowLayerBlock.LAYERS, layers)
                : SnowHosts.cover(old, layers);
        if (next != null) {
            Block.pushEntitiesUp(old, next, level, target);
            level.setBlockAndUpdate(target, next);
        }
        return false;
    }

    public static int effectiveCap(int configured, int gamerule) {
        if (gamerule <= 0) return 0;
        int bounded = Math.max(1, Math.min(8, configured));
        return gamerule == 1 ? bounded : Math.min(bounded, gamerule);
    }

    public static boolean weatherAllowed(boolean raining, boolean thundering, boolean ordinary) {
        return raining && (thundering || ordinary);
    }
}
