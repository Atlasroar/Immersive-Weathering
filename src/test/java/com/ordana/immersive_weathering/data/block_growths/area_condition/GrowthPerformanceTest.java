package com.ordana.immersive_weathering.data.block_growths.area_condition;

import com.google.common.base.Suppliers;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.data.block_growths.BlockGrowthHandler;
import com.ordana.immersive_weathering.data.block_growths.IConditionalGrowingBlock;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import com.ordana.immersive_weathering.data.block_growths.growths.ConfigurableBlockGrowth;
import com.ordana.immersive_weathering.data.block_growths.growths.IBlockGrowth;
import com.ordana.immersive_weathering.util.RandomizedBlockPositions;
import com.ordana.immersive_weathering.util.WeatheringHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * Executes production AreaCheck, BlockGrowthHandler, RandomizedBlockPositions and
 * WeatheringHelper bytecode with real Mojang geometry, state, predicates and RNG.
 * Only config/reload startup and the world are fixtures; no game loop is launched.
 */
public final class GrowthPerformanceTest {
    private static final int[][] RADII = {
            {2, 2, 2}, {3, 3, 3}, {2, 4, 2}, {1, 1, 1},
            {4, 3, 4}, {3, 1, 3}, {2, 3, 2}, {3, 2, 3},
            {0, 0, 0}, {0, 2, 1}, {2, 0, 3}
    };
    private static final Unsafe UNSAFE = unsafe();
    private static volatile long sink;
    private static int geometryCases;
    private static int areaCases;
    private static int dispatchCases;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        geometry();
        areas();
        dispatch();
        System.out.printf(Locale.ROOT, "PASS geometry=%d area=%d dispatch=%d%n",
                geometryCases, areaCases, dispatchCases);
        if (Arrays.asList(args).contains("--benchmark")) benchmark();
    }

    private static void geometry() throws Exception {
        Random seeds = new Random(73019);
        for (int[] radii : RADII) {
            for (int i = 0; i < 3000; i++) {
                comparePositions(new BlockPos(seeds.nextInt(), seeds.nextInt(), seeds.nextInt()), radii);
            }
        }
        comparePositions(new BlockPos(-31, -65, 70), new int[]{-1, 0, 0});
        comparePositions(BlockPos.ZERO, new int[]{Integer.MAX_VALUE, 1, 0});
        comparePositions(BlockPos.ZERO, new int[]{Integer.MIN_VALUE, 0, 0});
        comparePositions(new BlockPos(1, 2, 3), new int[]{16, 4, 16});

        Iterator<BlockPos> outer = RandomizedBlockPositions.iterator(new BlockPos(4, 5, 6), 2, 2, 2);
        List<BlockPos> retained = new ArrayList<>();
        while (outer.hasNext()) {
            retained.add(outer.next());
            comparePositions(new BlockPos(-8, 10, -20), new int[]{1, 1, 1});
        }
        check(retained.equals(WeatheringHelper.grabBlocksAroundRandomly(new BlockPos(4, 5, 6), 2, 2, 2)),
                "Reentrant traversal changed retained positions");
        check(retained.stream().noneMatch(p -> p instanceof BlockPos.MutableBlockPos), "Mutable yielded position");
        try {
            outer.next();
            throw new AssertionError("Exhausted iterator accepted next()");
        } catch (NoSuchElementException expected) {
            // Iterator contract.
        }
        List<BlockPos> mutable = WeatheringHelper.grabBlocksAroundRandomly(BlockPos.ZERO, 1, 1, 1);
        mutable.clear();
        check(mutable.isEmpty(), "Public helper list is no longer mutable");

        clearTemplates();
        for (int x = 0; x < 40; x++) comparePositions(BlockPos.ZERO, new int[]{x, 0, 0});
        check(templates().size() == 32, "Entry budget not enforced");
        check(retainedPositions() <= 4096, "Position budget exceeded");
        clearTemplates();
        comparePositions(BlockPos.ZERO, new int[]{7, 7, 7});
        comparePositions(BlockPos.ZERO, new int[]{6, 6, 6});
        check(templates().size() == 1 && retainedPositions() == 3375, "Total-position fallback not enforced");
        clearTemplates();
        for (int[] radii : RADII) comparePositions(BlockPos.ZERO, radii);
    }

    private static void comparePositions(BlockPos center, int[] radii) {
        List<BlockPos> expected = WeatheringHelper.grabBlocksAroundRandomly(center, radii[0], radii[1], radii[2]);
        Iterator<BlockPos> actual = RandomizedBlockPositions.iterator(center, radii[0], radii[1], radii[2]);
        for (BlockPos pos : expected) {
            check(actual.hasNext() && pos.equals(actual.next()), "Position/order mismatch: " + center);
        }
        check(!actual.hasNext(), "Extra position");
        geometryCases++;
    }

    private static void areas() throws Exception {
        Random inputs = new Random(921147);
        for (int i = 0; i < 6000; i++) {
            int[] radii = RADII[i % RADII.length];
            BlockPos center = new BlockPos(inputs.nextInt(4000) - 2000,
                    inputs.nextInt(600) - 300, inputs.nextInt(4000) - 2000);
            Optional<Integer> offset = i % 3 == 0 ? Optional.empty() : Optional.of(i % 13 - 6);
            int amount = new int[]{-1, 0, 1, 3, 20, 1000}[i % 6];
            compareArea(center, radii, amount, offset, i % 4, i % 5 == 0);
        }
        for (int[] radii : RADII) {
            for (int amount : new int[]{-1, 0, 1, 3, 20, 1000}) {
                for (int predicates = 0; predicates < 4; predicates++) {
                    for (Optional<Integer> offset : List.of(Optional.<Integer>empty(),
                            Optional.of(-7), Optional.of(0), Optional.of(7))) {
                        for (boolean includes : new boolean[]{false, true}) {
                            for (int seed = 0; seed < 2; seed++) {
                                compareArea(new BlockPos(seed * 61 - 31, seed * 13 - 7, seed * 47 - 19),
                                        radii, amount, offset, predicates, includes);
                            }
                        }
                    }
                }
            }
        }
        compareArea(BlockPos.ZERO, new int[]{16, 4, 16}, 100000, Optional.of(-4), 0, false);
        compareArea(BlockPos.ZERO, new int[]{-1, 0, 0}, -1, Optional.empty(), 3, false);
        compareArea(BlockPos.ZERO, new int[]{Integer.MAX_VALUE, 1, 0}, 0, Optional.empty(), 0, false);

        List<Object> baselineTrace = new ArrayList<>();
        List<Object> actualTrace = new ArrayList<>();
        boolean baseline = nestedArea(false, baselineTrace);
        boolean actual = nestedArea(true, actualTrace);
        check(baseline == actual && baselineTrace.equals(actualTrace), "Nested area mismatch");
        areaCases++;

        // Separate world instances stand in for dimensions/unloads: cached data is geometry only.
        compareArea(new BlockPos(12, -30, 7), RADII[0], 1000, Optional.of(2), 0, false);
        compareArea(new BlockPos(12, -30, 7), RADII[0], 1000, Optional.of(2), 3, true);
    }

    private static void compareArea(BlockPos pos, int[] radii, int amount, Optional<Integer> offset,
                                    int predicates, boolean includes) throws Exception {
        FixtureLevel oldLevel = world(true);
        FixtureLevel newLevel = world(true);
        AreaCheck oldCheck = area(radii, amount, offset, predicates, includes, oldLevel.trace);
        AreaCheck newCheck = area(radii, amount, offset, predicates, includes, newLevel.trace);
        ConfigurableBlockGrowth oldConfig = config(oldLevel.trace);
        ConfigurableBlockGrowth newConfig = config(newLevel.trace);
        boolean expected = baselineArea(oldCheck, pos, oldLevel, oldConfig);
        boolean actual = newCheck.test(pos, newLevel, newConfig);
        check(expected == actual, "Area result mismatch");
        check(oldLevel.trace.equals(newLevel.trace), "World-query/predicate/RNG order mismatch");
        areaCases++;
    }

    private static AreaCheck area(int[] radii, int amount, Optional<Integer> offset, int predicates,
                                  boolean includes, List<Object> trace) {
        Optional<RuleTest> have = (predicates & 1) == 0 ? Optional.empty() :
                Optional.of(new StochasticRule("have", trace));
        Optional<RuleTest> not = (predicates & 2) == 0 ? Optional.empty() :
                Optional.of(new StochasticRule("not", trace));
        return new AreaCheck(radii[0], radii[1], radii[2], amount, offset, have, not,
                includes ? Optional.of(new TracedIncludes(trace)) : Optional.empty());
    }

    private record TracedIncludes(List<Object> trace) implements HolderSet<Block> {
        private static final HolderSet<Block> CONTENTS = HolderSet.direct(Blocks.DIRT.builtInRegistryHolder());

        @Override
        public boolean contains(Holder<Block> holder) {
            trace.add("includes:" + holder.value());
            return CONTENTS.contains(holder);
        }

        @Override
        public java.util.stream.Stream<Holder<Block>> stream() {
            return CONTENTS.stream();
        }

        @Override
        public int size() {
            return CONTENTS.size();
        }

        @Override
        public com.mojang.datafixers.util.Either<net.minecraft.tags.TagKey<Block>, List<Holder<Block>>> unwrap() {
            return CONTENTS.unwrap();
        }

        @Override
        public Optional<Holder<Block>> getRandomElement(RandomSource random) {
            return CONTENTS.getRandomElement(random);
        }

        @Override
        public Holder<Block> get(int index) {
            return CONTENTS.get(index);
        }

        @Override
        public boolean canSerializeIn(net.minecraft.core.HolderOwner<Block> owner) {
            return CONTENTS.canSerializeIn(owner);
        }

        @Override
        public Optional<net.minecraft.tags.TagKey<Block>> unwrapKey() {
            return CONTENTS.unwrapKey();
        }

        @Override
        public Iterator<Holder<Block>> iterator() {
            return CONTENTS.iterator();
        }
    }

    private static ConfigurableBlockGrowth config(List<Object> trace) {
        return new ConfigurableBlockGrowth(new AbstractSet<>() {
            @Override
            public boolean contains(Object block) {
                trace.add("possible:" + block);
                return block == Blocks.STONE;
            }

            @Override
            public Iterator<Block> iterator() {
                return Set.of(Blocks.STONE).iterator();
            }

            @Override
            public int size() {
                return 1;
            }
        });
    }

    private static boolean nestedArea(boolean optimized, List<Object> trace) throws Exception {
        FixtureLevel level = world(true);
        level.trace = trace;
        AreaCheck inner = area(new int[]{1, 1, 1}, 1000, Optional.of(-1), 3, true, trace);
        ConfigurableBlockGrowth config = config(trace);
        RuleTest nested = new RuleTest() {
            @Override
            public boolean test(BlockState state, RandomSource random) {
                trace.add("nested:" + random.nextInt(87));
                return optimized ? inner.test(new BlockPos(-2, 17, 5), level, config) :
                        baselineArea(inner, new BlockPos(-2, 17, 5), level, config);
            }

            @Override
            protected RuleTestType<?> getType() {
                return null;
            }
        };
        AreaCheck outer = new AreaCheck(2, 2, 2, -1, Optional.of(3),
                Optional.of(nested), Optional.empty(), Optional.empty());
        return optimized ? outer.test(BlockPos.ZERO, level, config) : baselineArea(outer, BlockPos.ZERO, level, config);
    }

    // Baseline control flow from this repository at 56ed3501, not third-party code.
    private static boolean baselineArea(AreaCheck check, BlockPos pos, Level level, ConfigurableBlockGrowth config) {
        if (check.yOffset().isPresent()) pos = pos.above(check.yOffset().get());
        int count = 0;
        RandomSource random = RandomSource.create(Mth.getSeed(pos));
        boolean hasRequirement = check.mustHavePredicate().isEmpty();
        var list = WeatheringHelper.grabBlocksAroundRandomly(pos, check.rX(), check.rY(), check.rZ());
        for (BlockPos p : list) {
            BlockState state = level.getBlockState(p);
            if (config.getPossibleBlocks().contains(state.getBlock()) ||
                    (check.extraIncluded().isPresent() && state.is(check.extraIncluded().get()))) count += 1;
            if (!hasRequirement && check.mustHavePredicate().get().test(state, random)) {
                hasRequirement = true;
                if (check.requiredAmount() == -1) break;
            } else if (check.mustNotHavePredicate().isPresent() && check.mustNotHavePredicate().get().test(state, random)) {
                return false;
            }
            if (count >= check.requiredAmount()) return false;
        }
        return hasRequirement;
    }

    private static final class StochasticRule extends RuleTest {
        private final String name;
        private final List<Object> trace;

        private StochasticRule(String name, List<Object> trace) {
            this.name = name;
            this.trace = trace;
        }

        @Override
        public boolean test(BlockState state, RandomSource random) {
            int integer = random.nextInt(31);
            float value = random.nextFloat();
            trace.add(name + ":" + state + ":" + integer + ":" + Float.floatToIntBits(value));
            return integer % 7 == 0 && value < 0.65f;
        }

        @Override
        protected RuleTestType<?> getType() {
            return null;
        }
    }

    private static void dispatch() throws Exception {
        Map<TickSource, Set<IBlockGrowth>> universal = universal();
        Map<TickSource, Map<Block, Set<IBlockGrowth>>> specific = specific();
        ConditionalBlock conditional = (ConditionalBlock) UNSAFE.allocateInstance(ConditionalBlock.class);
        ConditionalState conditionalState = (ConditionalState) UNSAFE.allocateInstance(ConditionalState.class);
        conditionalState.block = conditional;
        for (int mode = 0; mode < 10; mode++) {
            List<Object> expected = runDispatch(false, mode, conditionalState, universal, specific);
            List<Object> actual = runDispatch(true, mode, conditionalState, universal, specific);
            check(expected.equals(actual), "Dispatch mismatch, mode " + mode);
            if (mode == 0 || mode == 4 || mode == 5 || mode == 6 || mode == 7) {
                check(actual.stream().noneMatch(e -> e instanceof String text && text.startsWith("biome:")),
                        "Unexpected biome query");
            }
            dispatchCases++;
        }
        universal.clear();
        specific.clear();
        CommonConfigs.enabled = true;
    }

    private static List<Object> runDispatch(boolean optimized, int mode, ConditionalState conditionalState,
                                           Map<TickSource, Set<IBlockGrowth>> universal,
                                           Map<TickSource, Map<Block, Set<IBlockGrowth>>> specific) throws Exception {
        universal.clear();
        specific.clear();
        FixtureLevel level = world(true);
        conditionalState.block.trace = level.trace;
        conditionalState.block.allowed = mode != 7;
        CommonConfigs.enabled = mode != 6;
        BlockState state = mode >= 6 ? conditionalState : Blocks.STONE.defaultBlockState();
        BlockPos pos = new BlockPos(4, -19, 67);
        IBlockGrowth blockCallback = callback("specific", level, state, pos, mode != 4);
        IBlockGrowth universalCallback = callback("universal", level, state, pos, mode != 4);
        if (mode == 1 || mode == 3 || mode >= 4) {
            universal.put(TickSource.BLOCK_TICK, new LinkedHashSet<>(List.of(universalCallback)));
        }
        if (mode == 2 || mode == 3 || mode >= 4) {
            specific.put(TickSource.BLOCK_TICK, new IdentityHashMap<>());
            specific.get(TickSource.BLOCK_TICK).put(state.getBlock(), new LinkedHashSet<>(List.of(blockCallback)));
        }
        if (mode == 5) {
            universal.get(TickSource.BLOCK_TICK).clear();
            specific.get(TickSource.BLOCK_TICK).get(state.getBlock()).clear();
        }
        if (mode == 8) {
            universal.get(TickSource.BLOCK_TICK).add(callback("universal-second", level, state, pos, true));
            specific.get(TickSource.BLOCK_TICK).get(state.getBlock())
                    .add(callback("specific-second", level, state, pos, true));
        }
        if (mode == 9) {
            universal.get(TickSource.BLOCK_TICK).add(new Callback(() -> {
                level.trace.add("reload");
                specific.clear();
                specific.put(TickSource.BLOCK_TICK, new IdentityHashMap<>());
                specific.get(TickSource.BLOCK_TICK).put(state.getBlock(),
                        new LinkedHashSet<>(List.of(callback("replacement", level, state, pos, true))));
            }));
        }
        check(BlockGrowthHandler.getBlockGrowths(TickSource.RAIN, state.getBlock()).isEmpty(), "Optional API");
        if (optimized) BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, state, level, pos);
        else baselineDispatch(TickSource.BLOCK_TICK, state, level, pos);
        return level.trace;
    }

    private static IBlockGrowth callback(String name, FixtureLevel level, BlockState original, BlockPos originalPos,
                                         boolean readsBiome) {
        return new IBlockGrowth() {
            @Override
            public Iterable<? extends Block> getOwners() {
                return null;
            }

            @Override
            public Collection<TickSource> getTickSources() {
                return List.of();
            }

            @Override
            public void tryGrowing(BlockPos pos, BlockState state, ServerLevel world, Supplier<Holder<Biome>> biome) {
                check(state == original && pos == originalPos && world == level, "Original arguments changed");
                level.trace.add(name);
                if (readsBiome) {
                    check(biome.get() == biome.get(), "Biome not memoized");
                }
            }
        };
    }

    private record Callback(Runnable action) implements IBlockGrowth {
        @Override
        public Iterable<? extends Block> getOwners() {
            return null;
        }

        @Override
        public Collection<TickSource> getTickSources() {
            return List.of();
        }

        @Override
        public void tryGrowing(BlockPos pos, BlockState state, ServerLevel level, Supplier<Holder<Biome>> biome) {
            action.run();
        }
    }

    private static void baselineDispatch(TickSource source, BlockState state, ServerLevel level, BlockPos pos) {
        if (!CommonConfigs.BLOCK_GROWTHS.get()) return;
        if (state.getBlock() instanceof IConditionalGrowingBlock cb && !cb.canGrow(state)) return;
        Supplier<Holder<Biome>> biome = Suppliers.memoize(() -> level.getBiome(pos));
        var universalGroup = universalUnchecked().get(source);
        if (universalGroup != null) {
            for (var config : universalGroup) config.tryGrowing(pos, state, level, biome);
        }
        var growth = BlockGrowthHandler.getBlockGrowths(source, state.getBlock());
        if (growth.isPresent()) {
            for (var config : growth.get()) config.tryGrowing(pos, state, level, biome);
        }
    }

    private static Map<TickSource, Set<IBlockGrowth>> universalUnchecked() {
        return DispatchMaps.UNIVERSAL;
    }

    private static final class DispatchMaps {
        private static final Map<TickSource, Set<IBlockGrowth>> UNIVERSAL = fieldMap("UNIVERSAL_GROWTHS");
        private static final Map<TickSource, Map<Block, Set<IBlockGrowth>>> SPECIFIC = fieldMap("GROWTH_FOR_BLOCK");
    }

    @SuppressWarnings("unchecked")
    private static <T> T fieldMap(String name) {
        try {
            Field field = BlockGrowthHandler.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Map<TickSource, Set<IBlockGrowth>> universal() {
        return DispatchMaps.UNIVERSAL;
    }

    private static Map<TickSource, Map<Block, Set<IBlockGrowth>>> specific() {
        return DispatchMaps.SPECIFIC;
    }

    private static Map<?, ?> templates() throws Exception {
        Field field = RandomizedBlockPositions.class.getDeclaredField("TEMPLATES");
        field.setAccessible(true);
        return (Map<?, ?>) field.get(null);
    }

    private static int retainedPositions() throws Exception {
        Field field = RandomizedBlockPositions.class.getDeclaredField("retainedPositions");
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static void clearTemplates() throws Exception {
        templates().clear();
        Field field = RandomizedBlockPositions.class.getDeclaredField("retainedPositions");
        field.setAccessible(true);
        field.setInt(null, 0);
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

    private static FixtureLevel world(boolean tracing) throws InstantiationException {
        FixtureLevel level = (FixtureLevel) UNSAFE.allocateInstance(FixtureLevel.class);
        level.trace = tracing ? new ArrayList<>() : null;
        return level;
    }

    private static final class FixtureLevel extends ServerLevel {
        private List<Object> trace;

        private FixtureLevel() {
            super(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            throw new AssertionError("World constructor must not run");
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            if (trace != null) trace.add(pos);
            int value = Math.floorMod(pos.getX() * 31 + pos.getY() * 17 + pos.getZ(), 3);
            return switch (value) {
                case 0 -> Blocks.STONE.defaultBlockState();
                case 1 -> Blocks.DIRT.defaultBlockState();
                default -> Blocks.AIR.defaultBlockState();
            };
        }

        @Override
        public Holder<Biome> getBiome(BlockPos pos) {
            if (trace != null) trace.add("biome:" + pos);
            return null;
        }
    }

    private static final class ConditionalBlock extends Block implements IConditionalGrowingBlock {
        private List<Object> trace;
        private boolean allowed;

        private ConditionalBlock() {
            super(BlockBehaviour.Properties.of());
        }

        @Override
        public boolean canGrow(BlockState state) {
            trace.add("canGrow");
            return allowed;
        }
    }

    private static final class ConditionalState extends BlockState {
        private ConditionalBlock block;

        private ConditionalState() {
            super(Blocks.STONE, com.google.common.collect.ImmutableMap.of(), null);
            throw new AssertionError("Conditional state constructor must not run");
        }

        @Override
        public Block getBlock() {
            return block;
        }
    }

    private static void benchmark() throws Exception {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        check(bean.isThreadAllocatedMemorySupported(), "Thread allocation measurement unavailable");
        bean.setThreadAllocatedMemoryEnabled(true);
        FixtureLevel level = world(false);
        BlockState state = Blocks.STONE.defaultBlockState();
        measure("empty-dispatch", () -> baselineDispatch(TickSource.BLOCK_TICK, state, level, BlockPos.ZERO),
                () -> BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, state, level, BlockPos.ZERO), bean, 1000000);
        for (int groups = 1; groups <= 3; groups++) {
            for (boolean readsBiome : new boolean[]{false, true}) {
                universal().clear();
                specific().clear();
                IBlockGrowth growth = new BenchmarkGrowth(readsBiome);
                if ((groups & 1) != 0) {
                    universal().put(TickSource.BLOCK_TICK, new LinkedHashSet<>(List.of(growth)));
                }
                if ((groups & 2) != 0) {
                    specific().put(TickSource.BLOCK_TICK, new IdentityHashMap<>());
                    specific().get(TickSource.BLOCK_TICK).put(state.getBlock(), new LinkedHashSet<>(List.of(growth)));
                }
                measure("active-dispatch-groups" + groups + "-" + (readsBiome ? "biome" : "no-biome"),
                        () -> baselineDispatch(TickSource.BLOCK_TICK, state, level, BlockPos.ZERO),
                        () -> BlockGrowthHandler.tickBlock(TickSource.BLOCK_TICK, state, level, BlockPos.ZERO),
                        bean, 1000000);
            }
        }
        universal().clear();
        specific().clear();
        for (int[] radii : Arrays.copyOf(RADII, 8)) {
            for (boolean early : new boolean[]{false, true}) {
                AreaCheck check = new AreaCheck(radii[0], radii[1], radii[2], early ? 0 : Integer.MAX_VALUE,
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
                ConfigurableBlockGrowth config = new ConfigurableBlockGrowth(Set.of(Blocks.STONE));
                int[] oldIndex = {0};
                int[] newIndex = {0};
                BlockPos[] centers = new BlockPos[1024];
                for (int i = 0; i < centers.length; i++) centers[i] = new BlockPos(i - 512, i % 128 - 64, 512 - i);
                measure(Arrays.toString(radii) + (early ? "-early" : "-full"),
                        () -> sink += baselineArea(check, centers[oldIndex[0]++ & 1023], level, config) ? 1 : 0,
                        () -> sink += check.test(centers[newIndex[0]++ & 1023], level, config) ? 1 : 0,
                        bean, 10000);
            }
        }
    }

    private static void measure(String name, Runnable baseline, Runnable optimized,
                                com.sun.management.ThreadMXBean bean, int iterations) {
        long warmupStart = System.nanoTime();
        int passes = 0;
        do {
            for (int i = 0; i < iterations; i++) {
                baseline.run();
                optimized.run();
            }
            passes++;
        } while (passes < 3 || System.nanoTime() - warmupStart < 1_000_000_000L);
        double[] oldTime = new double[7];
        double[] newTime = new double[7];
        double[] oldBytes = new double[7];
        double[] newBytes = new double[7];
        for (int round = 0; round < 7; round++) {
            if ((round & 1) == 0) {
                sample(baseline, bean, iterations, oldTime, oldBytes, round);
                sample(optimized, bean, iterations, newTime, newBytes, round);
            } else {
                sample(optimized, bean, iterations, newTime, newBytes, round);
                sample(baseline, bean, iterations, oldTime, oldBytes, round);
            }
        }
        Arrays.sort(oldTime);
        Arrays.sort(newTime);
        Arrays.sort(oldBytes);
        Arrays.sort(newBytes);
        System.out.printf(Locale.ROOT,
                "%s baseline=%.1f ns/op %.1f B/op optimized=%.1f ns/op %.1f B/op timeRatio=%.3f%n",
                name, oldTime[3], oldBytes[3], newTime[3], newBytes[3], newTime[3] / oldTime[3]);
        if (!name.startsWith("active-dispatch")) {
            check(newBytes[3] < oldBytes[3], "Missing intended allocation improvement: " + name);
        }
        if (name.equals("empty-dispatch")) check(newBytes[3] == 0, "Empty dispatch allocated");
    }

    private static void sample(Runnable action, com.sun.management.ThreadMXBean bean, int iterations,
                               double[] time, double[] bytes, int round) {
        long thread = Thread.currentThread().getId();
        long allocated = bean.getThreadAllocatedBytes(thread);
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) action.run();
        time[round] = (System.nanoTime() - start) / (double) iterations;
        bytes[round] = (bean.getThreadAllocatedBytes(thread) - allocated) / (double) iterations;
    }

    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }

    private record BenchmarkGrowth(boolean readsBiome) implements IBlockGrowth {
        @Override
        public Iterable<? extends Block> getOwners() {
            return null;
        }

        @Override
        public Collection<TickSource> getTickSources() {
            return List.of();
        }

        @Override
        public void tryGrowing(BlockPos pos, BlockState state, ServerLevel level, Supplier<Holder<Biome>> biome) {
            sink += readsBiome ? (biome.get() == biome.get() ? 1 : 0) : 1;
        }
    }
}
