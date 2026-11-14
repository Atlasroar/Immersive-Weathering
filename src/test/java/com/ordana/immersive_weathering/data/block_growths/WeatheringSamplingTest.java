package com.ordana.immersive_weathering.data.block_growths;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

/** Production admission and sky loop with native states/RNG and bounded world fixtures. */
public final class WeatheringSamplingTest {
    private static final Unsafe UNSAFE = unsafe();
    private static int checks;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        admission();
        sky();
        System.out.println("PASS weathering sampling checks=" + checks);
    }

    private static void admission() {
        for (WeatheringSampler.Rate rate : WeatheringSampler.Rate.values()) {
            QuarterRandom block = new QuarterRandom();
            QuarterRandom sky = new QuarterRandom();
            WeatheringSampler sampler = new WeatheringSampler(block, sky);
            int blocks = 0, skies = 0;
            for (int i = 0; i < 400; i++) {
                if (sampler.admitBlock(rate)) blocks++;
                if (sampler.admitSky(rate)) skies++;
            }
            check(blocks == (rate.ordinal() + 1) * 100 && skies == blocks,
                    "All four equally likely draws must admit exactly the configured quarters");
            check(block.draws == (rate == WeatheringSampler.Rate.PERCENT_100 ? 0 : 400)
                            && sky.draws == block.draws, "100% must not draw admission RNG");

            for (long seed : new long[]{0, 1, -1, 1234567, Long.MIN_VALUE, Long.MAX_VALUE}) {
                sampler = new WeatheringSampler(seed);
                blocks = 0;
                skies = 0;
                int attempts = 1_000_000;
                for (int i = 0; i < attempts; i++) {
                    if (sampler.admitBlock(rate)) blocks++;
                    if (sampler.admitSky(rate)) skies++;
                }
                int expected = attempts * (rate.ordinal() + 1) / 4;
                check(Math.abs(blocks - expected) < 2500 && Math.abs(skies - expected) < 2500,
                        "Seeded admission proportions deviate by >= 0.25 percentage points");
            }
        }

        WeatheringSampler first = new WeatheringSampler(123);
        WeatheringSampler second = new WeatheringSampler(123);
        WeatheringSampler otherWorld = new WeatheringSampler(456);
        boolean distinct = false;
        for (int i = 0; i < 1000; i++) {
            boolean block = first.admitBlock(WeatheringSampler.Rate.PERCENT_50);
            first.admitSky(WeatheringSampler.Rate.PERCENT_25);
            first.admitSky(WeatheringSampler.Rate.PERCENT_75);
            otherWorld.admitSky(WeatheringSampler.Rate.PERCENT_50);
            check(block == second.admitBlock(WeatheringSampler.Rate.PERCENT_50),
                    "Other worlds/sky activity must not advance this block stream");
            distinct |= block != otherWorld.admitBlock(WeatheringSampler.Rate.PERCENT_50);
        }
        check(distinct, "Different world seeds must not lock admission streams together");
        first = new WeatheringSampler(12);
        second = new WeatheringSampler(12);
        for (int i = 0; i < 1000; i++) {
            first.admitBlock(WeatheringSampler.Rate.PERCENT_100);
            first.admitSky(WeatheringSampler.Rate.PERCENT_100);
            first.admitBlock(WeatheringSampler.Rate.PERCENT_25);
            check(first.admitSky(WeatheringSampler.Rate.PERCENT_75)
                            == second.admitSky(WeatheringSampler.Rate.PERCENT_75),
                    "Block activity/full-rate bypass must not advance sky stream");
        }
    }

    private static void sky() throws Exception {
        LevelChunk chunk = (LevelChunk) UNSAFE.allocateInstance(LevelChunk.class);
        Field chunkPos = ChunkAccess.class.getDeclaredField("chunkPos");
        UNSAFE.putObject(chunk, UNSAFE.objectFieldOffset(chunkPos), new ChunkPos(-3, 5));
        Map<TickSource, Set<IBlockGrowth>> universal = universal();
        for (TickSource source : List.of(TickSource.CLEAR_SKY, TickSource.RAIN, TickSource.SNOW)) {
            universal.put(source, Set.of(new Callback(source)));
        }
        try {
            for (boolean rain : new boolean[]{false, true}) {
                for (boolean snow : new boolean[]{false, true}) {
                    for (int speed : new int[]{0, 1, 3, 47, 48, 49, 95, 96, 97, 480}) {
                        Fixture expected = world(rain, snow, 91827);
                        Fixture actual = world(rain, snow, 91827);
                        CommonConfigs.samplingRate = WeatheringSampler.Rate.PERCENT_100;
                        for (int i = 0; i < 100; i++) {
                            legacySky(expected, chunk, speed);
                            BlockGrowthHandler.performSkyAccessTick(actual, chunk, speed);
                        }
                        check(expected.trace.equals(actual.trace), "100% legacy RNG/query/source/order mismatch");
                        check(actual.samplerAccesses == 0, "100% must not initialize/access sampler");
                        check(expected.getRandom().nextInt() == actual.getRandom().nextInt(),
                                "100% changed subsequent gameplay RNG");
                    }
                }
            }
            for (WeatheringSampler.Rate rate : WeatheringSampler.Rate.values()) {
                for (int speed : new int[]{0, 1, 3, 47, 48, 49, 96, 97, 480}) {
                    Fixture level = world(false, false, 1122);
                    level.growthDraws = false;
                    QuarterRandom quarters = new QuarterRandom();
                    level.sampler = new WeatheringSampler(new QuarterRandom(), quarters);
                    CommonConfigs.samplingRate = rate;
                    int opportunities = 0;
                    CountingRandom control = new CountingRandom(1122);
                    for (int i = 0; i < 400; i++) {
                        float chance = speed / (3f * 16f);
                        do {
                            if (chance > control.nextFloat()) opportunities++;
                            chance--;
                        } while (chance > 0);
                        BlockGrowthHandler.performSkyAccessTick(level, chunk, speed);
                    }
                    int expected = opportunities / 4 * (rate.ordinal() + 1)
                            + Math.min(opportunities % 4, rate.ordinal() + 1);
                    check(level.heightmaps == expected && level.positions == expected
                                    && level.states == expected && level.dispatches == expected,
                            "Rejected sky samples must skip positions, heightmaps, state reads and dispatch");
                    check(level.random.draws == control.draws,
                            "Original sky source probability and zero/high-speed draw counts must remain");
                    check(quarters.draws == (rate == WeatheringSampler.Rate.PERCENT_100 ? 0 : opportunities),
                            "Admission only follows successful original sky opportunities");
                }
            }
            CommonConfigs.samplingRate = WeatheringSampler.Rate.PERCENT_25;
            Fixture direct = world(false, false, 2);
            universal.put(TickSource.LIGHTNING, Set.of(new Callback(TickSource.LIGHTNING)));
            universal.put(TickSource.BLOCK_TICK, Set.of(new Callback(TickSource.BLOCK_TICK)));
            for (int i = 0; i < 100; i++) {
                BlockGrowthHandler.tickBlock(TickSource.LIGHTNING, Blocks.STONE.defaultBlockState(), direct, BlockPos.ZERO);
                BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, Blocks.STONE.defaultBlockState(), direct, BlockPos.ZERO);
            }
            check(direct.dispatches == 200 && direct.samplerAccesses == 0,
                    "Direct lightning/API dispatch must not be sampled");
        } finally {
            universal.clear();
            CommonConfigs.samplingRate = WeatheringSampler.Rate.PERCENT_50;
        }
    }

    private static void legacySky(Fixture level, LevelChunk chunk, int speed) {
        ChunkPos pos = chunk.getPos();
        float chance = speed / (3f * 16f);
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        boolean rain = level.isRaining();
        do {
            if (chance > level.getRandom().nextFloat()) {
                BlockPos air = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                        level.getBlockRandomPos(minX, 0, minZ, 15));
                BlockPos target = air.below();
                BlockState state = level.getBlockState(target);
                TickSource source = TickSource.CLEAR_SKY;
                if (rain) {
                    Biome biome = level.getBiome(target).value();
                    source = biome.getPrecipitationAt(target) == Biome.Precipitation.SNOW
                            ? TickSource.SNOW : TickSource.RAIN;
                }
                BlockGrowthHandler.tickBlock(source, state, level, new BlockPos(target));
            }
            chance--;
        } while (chance > 0);
    }

    private static Fixture world(boolean rain, boolean snow, long seed) throws Exception {
        Fixture level = (Fixture) UNSAFE.allocateInstance(Fixture.class);
        level.trace = new ArrayList<>();
        level.random = new CountingRandom(seed);
        level.sampler = new WeatheringSampler(seed);
        level.rain = rain;
        level.growthDraws = true;
        level.biome = Holder.direct(new Biome.BiomeBuilder().hasPrecipitation(true)
                .temperature(snow ? -0.5f : 0.8f).downfall(0.5f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build())
                .generationSettings(BiomeGenerationSettings.EMPTY).mobSpawnSettings(MobSpawnSettings.EMPTY).build());
        return level;
    }

    private static final class Fixture extends ServerLevel implements WeatheringSampler.Provider {
        private List<String> trace;
        private CountingRandom random;
        private WeatheringSampler sampler;
        private Holder<Biome> biome;
        private boolean rain, growthDraws;
        private int samplerAccesses, positions, heightmaps, states, dispatches;
        private Fixture() {
            super(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            throw new AssertionError();
        }
        @Override public WeatheringSampler immersiveWeathering$getSampler() { samplerAccesses++; return sampler; }
        @Override public RandomSource getRandom() { return random; }
        @Override public boolean isRaining() { trace.add("weather"); return rain; }
        @Override public BlockPos getBlockRandomPos(int x, int y, int z, int mask) {
            positions++;
            trace.add("position");
            return new BlockPos(x + (positions & mask), y, z + ((positions * 7) & mask));
        }
        @Override public BlockPos getHeightmapPos(Heightmap.Types type, BlockPos pos) {
            heightmaps++;
            trace.add("height:" + type + ":" + pos);
            return new BlockPos(pos.getX(), 65, pos.getZ());
        }
        @Override public BlockState getBlockState(BlockPos pos) {
            states++;
            trace.add("state:" + pos);
            return Blocks.STONE.defaultBlockState();
        }
        @Override public Holder<Biome> getBiome(BlockPos pos) { trace.add("biome:" + pos); return biome; }
    }

    private record Callback(TickSource source) implements IBlockGrowth {
        @Override public Iterable<? extends Block> getOwners() { return null; }
        @Override public Collection<TickSource> getTickSources() { return List.of(source); }
        @Override public void tryGrowing(BlockPos pos, BlockState state, ServerLevel world, Supplier<Holder<Biome>> biome) {
            Fixture level = (Fixture) world;
            level.dispatches++;
            level.trace.add("dispatch:" + source + ":" + pos + ":" + state
                    + ":" + (level.growthDraws ? level.getRandom().nextInt() : 0));
        }
    }

    private static final class QuarterRandom extends Random {
        private int draws;
        @Override public int nextInt(int bound) {
            check(bound == 4, "Admission bound");
            return draws++ & 3;
        }
    }

    private static final class CountingRandom extends LegacyRandomSource {
        private int draws;
        private CountingRandom(long seed) { super(seed); }
        @Override public float nextFloat() { draws++; return super.nextFloat(); }
    }

    @SuppressWarnings("unchecked")
    private static Map<TickSource, Set<IBlockGrowth>> universal() throws Exception {
        Field field = BlockGrowthHandler.class.getDeclaredField("UNIVERSAL_GROWTHS");
        field.setAccessible(true);
        return (Map<TickSource, Set<IBlockGrowth>>) field.get(null);
    }

    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
