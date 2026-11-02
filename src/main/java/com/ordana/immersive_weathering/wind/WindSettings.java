package com.ordana.immersive_weathering.wind;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.util.Mth;

public record WindSettings(double base, double coldBonus, int height, double heightBonus, double stormBonus) {
    public static WindSettings current() {
        return new WindSettings(CommonConfigs.WIND_BASE_CHANCE.get(), CommonConfigs.WIND_COLD_BONUS.get(),
                CommonConfigs.WIND_HEIGHT.get(), CommonConfigs.WIND_HEIGHT_BONUS.get(), CommonConfigs.WIND_STORM_BONUS.get());
    }

    public double chance(float baseTemperature, int y, boolean snowstorm) {
        return Mth.clamp(base + coldBonus * Mth.clamp((0.8 - baseTemperature) / 1.3, 0, 1)
                + (y >= height ? heightBonus : 0) + (snowstorm ? stormBonus : 0), 0, 1);
    }
}
