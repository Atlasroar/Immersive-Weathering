package com.ordana.immersive_weathering.configs;

import java.util.function.Supplier;

public final class CommonConfigs {
    public static boolean spread = true, torches = true, grass = true;
    public static int interval = 20;
    public static double chance = 0.02;
    public static final Supplier<Boolean> FIRE_ENTITY_SPREAD = () -> spread;
    public static final Supplier<Boolean> FIRE_TORCH_ATTACKS = () -> torches;
    public static final Supplier<Boolean> FIRE_FLAMMABLE_GRASS = () -> grass;
    public static final Supplier<Integer> FIRE_SPREAD_INTERVAL = () -> interval;
    public static final Supplier<Integer> FIRE_SPREAD_RADIUS = () -> 1;
    public static final Supplier<Integer> FIRE_HEAT_RADIUS = () -> 2;
    public static final Supplier<Integer> FIRE_HEAT_SOURCE_CAP = () -> 8;
    public static final Supplier<Double> FIRE_SPREAD_CHANCE = () -> chance;
    public static final Supplier<Double> FIRE_PROJECTILE_MULTIPLIER = () -> 0.0625;
    public static final Supplier<Double> FIRE_HEAT_BONUS = () -> 0.125;
    public static final Supplier<Double> FIRE_TEMPERATURE_BONUS = () -> 0.5;
    public static final Supplier<Double> FIRE_SPREAD_CHANCE_CAP = () -> 0.25;
}
