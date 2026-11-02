package com.ordana.immersive_weathering.configs;

import java.util.function.Supplier;

/** Replaces platform config initialization, not production storm rules. */
public final class CommonConfigs {
    public static boolean deposition = true, slowing = true;
    public static boolean mixing = true, replacement = true, erosion = true;
    public static double erosionChance = 0.125;
    public static final Supplier<Boolean> SAND_HOSTS = () -> mixing;
    public static final Supplier<Boolean> REPLACE_SAND_PILES = () -> replacement;
    public static final Supplier<Boolean> SAND_RAIN_EROSION = () -> erosion;
    public static final Supplier<Double> SAND_RAIN_EROSION_CHANCE = () -> erosionChance;
    public static int cap = 1;
    public static double slowAmount = 0.3;
    public static final Supplier<Boolean> SANDSTORM_DEPOSITION = () -> deposition;
    public static final Supplier<Integer> SANDSTORM_LAYER_CAP = () -> cap;
    public static final Supplier<Boolean> SANDSTORM_SLOWING = () -> slowing;
    public static final Supplier<Double> SANDSTORM_SLOW_AMOUNT = () -> slowAmount;
}
