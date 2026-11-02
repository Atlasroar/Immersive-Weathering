package com.ordana.immersive_weathering.configs;

import java.util.function.Supplier;

/** Replaces platform config initialization; allows live config changes in fixtures. */
public final class ClientConfigs {
    public static boolean fog = true, clouds = true, particles = true, sounds = true;
    public static int range = 20, rarity = 60;
    public static double start = 16, end = 64;
    public static final Supplier<Boolean> SANDSTORM_FOG = () -> fog;
    public static final Supplier<Boolean> SANDSTORM_CLOUDS = () -> clouds;
    public static final Supplier<Boolean> SANDSTORM_PARTICLES = () -> particles;
    public static final Supplier<Boolean> SANDSTORM_SOUNDS = () -> sounds;
    public static final Supplier<Integer> SANDSTORM_PARTICLE_RANGE = () -> range;
    public static final Supplier<Integer> SANDSTORM_PARTICLE_RARITY = () -> rarity;
    public static final Supplier<Double> SANDSTORM_FOG_START = () -> start;
    public static final Supplier<Double> SANDSTORM_FOG_END = () -> end;
}
