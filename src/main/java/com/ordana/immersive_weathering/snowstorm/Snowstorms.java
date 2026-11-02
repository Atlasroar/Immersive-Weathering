package com.ordana.immersive_weathering.snowstorm;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/** Vanilla thunder supplies the weather clock; no separate storm scheduler or snow tick exists. */
public final class Snowstorms {
    public static boolean ownsPresentation() {
        // Pinned upstream fabric.mod.json uses the underscore ID and provides the hyphenated alias.
        return !FabricLoader.getInstance().isModLoaded("immersive_storms")
                && !FabricLoader.getInstance().isModLoaded("immersive-storms");
    }

    public static boolean visualStorm(Level level, BlockPos pos) {
        if (!CommonConfigs.SNOWSTORM_ENABLED.get() || !level.isRaining() || !level.isThundering()
                || level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) return false;
        return snowyBiome(level.getBiome(pos).value(), pos);
    }

    public static boolean snowyBiome(Biome biome, BlockPos pos) {
        return biome.hasPrecipitation() && biome.coldEnoughToSnow(pos)
                && biome.getPrecipitationAt(pos) == Biome.Precipitation.SNOW;
    }

    public static boolean isSnowstorm(Level level, BlockPos pos) {
        return visualStorm(level, pos) && level.canSeeSky(pos)
                && level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
    }

    public static int depositionLayers(int existing, int cap, boolean storm, int configuredBonus) {
        return Math.min(cap, existing + 1 + (storm ? Math.max(0, Math.min(3, configuredBonus)) : 0));
    }

    private Snowstorms() {}
}
