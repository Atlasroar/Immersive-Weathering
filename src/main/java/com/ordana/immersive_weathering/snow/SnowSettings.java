package com.ordana.immersive_weathering.snow;

import com.ordana.immersive_weathering.configs.CommonConfigs;

public final class SnowSettings {
    private SnowSettings() {}

    public static boolean mixing() { return CommonConfigs.SNOW_HOSTS.get(); }
    public static boolean falling() { return CommonConfigs.FALLING_SNOW.get(); }
    public static boolean replacement() { return CommonConfigs.REPLACE_PARTIAL_SNOW.get(); }
}
