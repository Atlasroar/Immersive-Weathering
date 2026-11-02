package com.ordana.immersive_weathering.configs;

import java.util.function.Supplier;

/** Test-only config initialization substitute; policy and transitions remain production code. */
public final class CommonConfigs {
    public static boolean mixing = true, falling = true, replacement = true, accumulation = true, ordinary;
    public static int cap = 8;
    public static final Supplier<Boolean> SNOWSTORM_ENABLED = () -> false;
    public static final Supplier<Integer> SNOWSTORM_DEPOSITION_BONUS = () -> 0;
    public static final Supplier<Boolean> SNOW_HOSTS = () -> mixing;
    public static final Supplier<Boolean> FALLING_SNOW = () -> falling;
    public static final Supplier<Boolean> REPLACE_PARTIAL_SNOW = () -> replacement;
    public static final Supplier<Boolean> SNOW_ACCUMULATION = () -> accumulation;
    public static final Supplier<Boolean> SNOW_ACCUMULATION_WITHOUT_THUNDER = () -> ordinary;
    public static final Supplier<Integer> SNOW_COLUMN_CAP = () -> cap;
}
