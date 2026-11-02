package com.ordana.immersive_weathering.configs;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;

import java.util.function.Supplier;

public class ClientConfigs {

    public static ConfigSpec CLIENT_SPEC;
    public static Supplier<Boolean> SANDSTORM_PARTICLES;
    public static Supplier<Boolean> SANDSTORM_FOG;
    public static Supplier<Boolean> SANDSTORM_CLOUDS;
    public static Supplier<Boolean> SANDSTORM_SOUNDS;
    public static Supplier<Integer> SANDSTORM_PARTICLE_RANGE;
    public static Supplier<Integer> SANDSTORM_PARTICLE_RARITY;
    public static Supplier<Double> SANDSTORM_FOG_START;
    public static Supplier<Double> SANDSTORM_FOG_END;


    //client configs

    public static Supplier<Boolean> LEAF_DECAY_PARTICLES;
    public static Supplier<Boolean> FALLING_LEAF_PARTICLES;
    public static Supplier<Boolean> LEAF_DECAY_SOUND;

    public static Supplier<Double> FALLING_LEAF_PARTICLE_RATE;
    public static Supplier<Double> RAINY_FALLING_LEAF_PARTICLE_RATE;
    public static Supplier<Double> STORMY_FALLING_LEAF_PARTICLE_RATE;

    public static void init() {
        ConfigBuilder builder = ConfigBuilder.create(ImmersiveWeathering.res("client"), ConfigType.CLIENT);

        builder.push("sandstorms");
        SANDSTORM_PARTICLES = builder.comment("Show windblown dust; also respects vanilla particle settings. All IW storm effects yield to installed Scorchful.")
                .define("particles", true);
        SANDSTORM_PARTICLE_RANGE = builder.comment("Particle scan radius; checks (2 * range)^2 columns per client tick, without loading chunks.")
                .define("particle_range", 20, 0, 32);
        SANDSTORM_PARTICLE_RARITY = builder.comment("One particle trial in this many columns. Minimal disables storm particles; Decreased halves the trial rate.")
                .define("particle_rarity", 60, 1, 1200);
        SANDSTORM_FOG = builder.comment("Blend sand-colored spherical terrain fog with vanilla weather and biome boundaries, including indoors. Preserves fluid/blindness/darkness/stronger vanilla fog.")
                .define("fog", true);
        SANDSTORM_FOG_START = builder.comment("Full-storm terrain fog start in blocks; clamped to end and vanilla fog.")
                .define("fog_start", 16d, 0d, 256d);
        SANDSTORM_FOG_END = builder.comment("Full-storm terrain fog end in blocks; never extends vanilla view distance.")
                .define("fog_end", 64d, 1d, 512d);
        SANDSTORM_CLOUDS = builder.comment("Hide clouds in eligible biomes above 0.75 rain strength. Independent of the fog toggle, including indoors.")
                .define("hide_clouds", true);
        SANDSTORM_SOUNDS = builder.comment("Intermittent wind near storm surfaces. Uses the Weather volume slider and Wind blows subtitle, not a looping sound.")
                .define("sounds", true);
        builder.pop();

        builder.push("general");
        LEAF_DECAY_PARTICLES = builder.define("leaves_decay_particles", true);
        FALLING_LEAF_PARTICLES = builder.define("falling_leaf_particles", true);
        LEAF_DECAY_SOUND = builder.define("decay_sound", true);

        FALLING_LEAF_PARTICLE_RATE = builder.define("falling_leaf_rate", 0.08f, 0f, 1f);
        RAINY_FALLING_LEAF_PARTICLE_RATE = builder.define("rainy_falling_leaf_rate", 0.2f, 0f, 1f);
        STORMY_FALLING_LEAF_PARTICLE_RATE = builder.define("stormy_falling_leaf_rate", 0.4f, 0f, 1f);
        builder.pop();

        CLIENT_SPEC = builder.buildAndRegister();

        //load early
        CLIENT_SPEC.loadFromFile();

    }

}
