package com.ordana.immersive_weathering.data.block_growths;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.server.level.ServerLevel;

import java.util.Random;

/** Level-owned admission streams, separate from Minecraft's gameplay RNG. */
public final class WeatheringSampler {
    public enum Rate {
        PERCENT_25(1), PERCENT_50(2), PERCENT_75(3), PERCENT_100(4);

        private final int quarters;

        Rate(int quarters) {
            this.quarters = quarters;
        }
    }

    public interface Provider {
        WeatheringSampler immersiveWeathering$getSampler();
    }

    private final Random block;
    private final Random sky;

    public WeatheringSampler(long seed) {
        this(new Random(seed), new Random(seed ^ 0x9E3779B97F4A7C15L));
    }

    WeatheringSampler(Random block, Random sky) {
        this.block = block;
        this.sky = sky;
    }

    public boolean admitBlock(Rate rate) {
        return rate == Rate.PERCENT_100 || block.nextInt(4) < rate.quarters;
    }

    public boolean admitSky(Rate rate) {
        return rate == Rate.PERCENT_100 || sky.nextInt(4) < rate.quarters;
    }

    public static boolean sampleBlock(ServerLevel level) {
        Rate rate = CommonConfigs.WEATHERING_SAMPLING_RATE.get();
        return rate == Rate.PERCENT_100 || ((Provider) level).immersiveWeathering$getSampler().admitBlock(rate);
    }

    public static boolean sampleSky(ServerLevel level) {
        Rate rate = CommonConfigs.WEATHERING_SAMPLING_RATE.get();
        return rate == Rate.PERCENT_100 || ((Provider) level).immersiveWeathering$getSampler().admitSky(rate);
    }
}
