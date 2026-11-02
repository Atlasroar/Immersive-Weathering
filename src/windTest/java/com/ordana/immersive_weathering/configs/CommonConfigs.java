package com.ordana.immersive_weathering.configs;

import java.util.function.Supplier;

/** Configuration storage fixture only; all decisions use production classes. */
public final class CommonConfigs {
    public static boolean enabled = true, stormEnabled = true, extinguish = true, freeze = true, disableIcicles;
    public static boolean accumulation = true, withoutThunder, snowHosts = true;
    public static int stormBonus = 1, snowCap = 8, budget = 2, liveCap = 32, slowTicks = 60;
    public static double base = 0.001, cold = 0.019, altitude = 0.02, storm = 0.08, icicles = 0.05;
    public static final Supplier<Boolean> WIND_ENABLED = () -> enabled;
    public static final Supplier<Double> WIND_BASE_CHANCE = () -> base;
    public static final Supplier<Double> WIND_COLD_BONUS = () -> cold;
    public static final Supplier<Integer> WIND_HEIGHT = () -> 100;
    public static final Supplier<Double> WIND_HEIGHT_BONUS = () -> altitude;
    public static final Supplier<Double> WIND_STORM_BONUS = () -> storm;
    public static final Supplier<Integer> WIND_SPAWN_BUDGET = () -> budget;
    public static final Supplier<Integer> WIND_LIVE_CAP = () -> liveCap;
    public static final Supplier<Integer> WIND_SLOW_TICKS = () -> slowTicks;
    public static final Supplier<Integer> WIND_SLOW_AMPLIFIER = () -> 0;
    public static final Supplier<Boolean> WIND_EXTINGUISH = () -> extinguish;
    public static final Supplier<Boolean> WIND_FREEZE_WATER = () -> freeze;
    public static final Supplier<Double> WIND_ICICLE_CHANCE = () -> icicles;
    public static final Supplier<Boolean> DISABLE_ICICLES = () -> disableIcicles;
    public static final Supplier<Boolean> THIN_ICE_MELTING = () -> false;
    public static final Supplier<Boolean> SNOWSTORM_ENABLED = () -> stormEnabled;
    public static final Supplier<Integer> SNOWSTORM_DEPOSITION_BONUS = () -> stormBonus;
    public static final Supplier<Boolean> SNOW_ACCUMULATION = () -> accumulation;
    public static final Supplier<Boolean> SNOW_ACCUMULATION_WITHOUT_THUNDER = () -> withoutThunder;
    public static final Supplier<Boolean> SNOW_HOSTS = () -> snowHosts;
    public static final Supplier<Integer> SNOW_COLUMN_CAP = () -> snowCap;
    public static final Supplier<Boolean> FALLING_SNOW = () -> true;
    public static final Supplier<Boolean> REPLACE_PARTIAL_SNOW = () -> true;
}
