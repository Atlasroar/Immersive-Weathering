package com.ordana.immersive_weathering.data.block_growths;

import com.google.gson.JsonObject;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.EnumConfigValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Real config and woven server hook, invoked at its post-native-tick boundary without a saved world. */
public final class WeatheringSamplingIntegrationTest {
    private static final Unsafe UNSAFE = unsafe();
    private static final BlockPos POS = new BlockPos(3, 68, 5);
    private static int checks;

    @SuppressWarnings("unchecked")
    public static void verify() throws Exception {
        EnumConfigValue<WeatheringSampler.Rate> config =
                (EnumConfigValue<WeatheringSampler.Rate>) CommonConfigs.WEATHERING_SAMPLING_RATE;
        WeatheringSampler.Rate original = config.get();
        check(original == WeatheringSampler.Rate.PERCENT_50, "Fresh actual config defaults to 50%");
        JsonObject saved = new JsonObject();
        config.saveToJson(saved);
        check(saved.get("weathering_sampling_rate").getAsString().equals("PERCENT_50"),
                "Moonlight must persist the supported enum name");
        Method hook = Arrays.stream(ServerLevel.class.getDeclaredMethods())
                .filter(m -> m.getName().contains("IW_weatheringTick")).findFirst().orElseThrow();
        hook.setAccessible(true);
        Field registrations = BlockGrowthHandler.class.getDeclaredField("UNIVERSAL_GROWTHS");
        registrations.setAccessible(true);
        Map<TickSource, Set<IBlockGrowth>> universal = (Map<TickSource, Set<IBlockGrowth>>) registrations.get(null);
        Set<IBlockGrowth> old = universal.put(TickSource.BLOCK_TICK, Set.of(new Callback()));
        SwitchingBlock nativeBlock = new SwitchingBlock();
        try {
            for (WeatheringSampler.Rate rate : WeatheringSampler.Rate.values()) {
                setRate(config, rate);
                check(config.get() == rate, "All supported rates must load through Moonlight");
                Fixture level = world(7123);
                Section section = (Section) UNSAFE.allocateInstance(Section.class);
                level.section = section;
                CountingRandom nativeRandom = level.nativeRandom;
                RandomSource control = RandomSource.create(99);
                int attempts = 100_000;
                for (int i = 0; i < attempts; i++) {
                    section.state = nativeBlock.defaultBlockState();
                    section.state.randomTick(level, POS, nativeRandom);
                    hook.invoke(level, null, 3, new CallbackInfo("tickChunk", false), null,
                            false, 0, 0, null, null, 0, section, 0, 64, i, POS, nativeBlock.defaultBlockState());
                    control.nextInt(100);
                }
                int expected = attempts * (rate.ordinal() + 1) / 4;
                check(Math.abs(level.dispatches - expected) < 600, "Woven hook admission proportion");
                check(section.reads == level.dispatches, "Rejected blocks skip fresh-state lookup and dispatch");
                check(nativeRandom.draws == attempts && nativeRandom.nextInt() == control.nextInt(),
                        "IW admission must not alter native tick RNG");
                check(level.nativeTicks == attempts, "Native ticks are never thinned");
                if (rate == WeatheringSampler.Rate.PERCENT_100) {
                    check(level.dispatches == attempts, "Full-rate legacy hook admits every sample");
                    Field sampler = Arrays.stream(ServerLevel.class.getDeclaredFields())
                            .filter(f -> f.getName().endsWith("immersiveWeathering$sampler")).findFirst().orElseThrow();
                    sampler.setAccessible(true);
                    check(sampler.get(level) == null, "Full rate does not even initialize admission state");
                }
            }
            setRate(config, WeatheringSampler.Rate.PERCENT_50);
            ServerLevel first = world(33);
            ServerLevel second = world(33);
            ServerLevel other = world(99);
            WeatheringSampler a = ((WeatheringSampler.Provider) first).immersiveWeathering$getSampler();
            WeatheringSampler b = ((WeatheringSampler.Provider) second).immersiveWeathering$getSampler();
            WeatheringSampler c = ((WeatheringSampler.Provider) other).immersiveWeathering$getSampler();
            check(a != b && b != c, "Sampler instances must be level-owned");
            for (int i = 0; i < 1000; i++) {
                c.admitBlock(WeatheringSampler.Rate.PERCENT_25);
                a.admitSky(WeatheringSampler.Rate.PERCENT_75);
                check(a.admitBlock(WeatheringSampler.Rate.PERCENT_50)
                                == b.admitBlock(WeatheringSampler.Rate.PERCENT_50),
                        "Interleaved other-world/sky work must not change block admissions");
            }
        } finally {
            setRate(config, original);
            if (old == null) universal.remove(TickSource.BLOCK_TICK);
            else universal.put(TickSource.BLOCK_TICK, old);
        }
        System.out.println("PASS Fabric weathering sampling checks=" + checks + " (woven hook, no saved world)");
    }

    private static void setRate(EnumConfigValue<WeatheringSampler.Rate> config, WeatheringSampler.Rate rate) {
        JsonObject json = new JsonObject();
        json.addProperty("weathering_sampling_rate", rate.name());
        config.loadFromJson(json);
    }

    private record Callback() implements IBlockGrowth {
        @Override public Iterable<? extends Block> getOwners() { return null; }
        @Override public Collection<TickSource> getTickSources() { return List.of(TickSource.BLOCK_TICK); }
        @Override public void tryGrowing(BlockPos pos, BlockState state, ServerLevel world, Supplier<Holder<Biome>> biome) {
            check(state == Blocks.STONE.defaultBlockState() && pos == POS,
                    "Admitted hook must dispatch fresh post-native state and original position");
            ((Fixture) world).dispatches++;
        }
    }

    private static final class SwitchingBlock extends Block {
        private SwitchingBlock() { super(BlockBehaviour.Properties.of().randomTicks()); }
        @Override public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            random.nextInt(100);
            Fixture fixture = (Fixture) level;
            fixture.nativeTicks++;
            fixture.section.state = Blocks.STONE.defaultBlockState();
        }
    }

    private static Fixture world(long seed) throws Exception {
        Fixture level = (Fixture) UNSAFE.allocateInstance(Fixture.class);
        level.seed = seed;
        level.nativeRandom = new CountingRandom(99);
        Field random = Level.class.getDeclaredField("random");
        UNSAFE.putObject(level, UNSAFE.objectFieldOffset(random), level.nativeRandom);
        Field dimension = Level.class.getDeclaredField("dimension");
        UNSAFE.putObject(level, UNSAFE.objectFieldOffset(dimension), Level.OVERWORLD);
        return level;
    }

    private static final class Fixture extends ServerLevel {
        private long seed;
        private int dispatches, nativeTicks;
        private Section section;
        private CountingRandom nativeRandom;
        private Fixture() {
            super(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            throw new AssertionError();
        }
        @Override public long getSeed() { return seed; }
    }

    private static final class Section extends LevelChunkSection {
        private BlockState state;
        private int reads;
        private Section() { super(null); throw new AssertionError(); }
        @Override public BlockState getBlockState(int x, int y, int z) {
            check(x == 3 && y == 4 && z == 5, "Preserve local section coordinates");
            reads++;
            return state;
        }
    }

    private static final class CountingRandom extends LegacyRandomSource {
        private int draws;
        private CountingRandom(long seed) { super(seed); }
        @Override public int nextInt(int bound) { draws++; return super.nextInt(bound); }
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
