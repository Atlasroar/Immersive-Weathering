package com.ordana.immersive_weathering.configs;

import com.ordana.immersive_weathering.data.block_growths.WeatheringSampler;
import java.util.List;
import java.util.function.Supplier;

/** Test-only substitute for loader-owned configuration initialization. */
public final class CommonConfigs {
    public static boolean enabled = true;
    public static final Supplier<Boolean> BLOCK_GROWTHS = () -> enabled;
    public static WeatheringSampler.Rate samplingRate = WeatheringSampler.Rate.PERCENT_50;
    public static final Supplier<WeatheringSampler.Rate> WEATHERING_SAMPLING_RATE = () -> samplingRate;
    public static final Supplier<List<String>> DISABLED_GROWTHS = List::of;
}
