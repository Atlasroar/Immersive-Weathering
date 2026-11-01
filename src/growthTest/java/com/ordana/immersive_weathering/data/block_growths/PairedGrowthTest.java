package com.ordana.immersive_weathering.data.block_growths;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.ordana.immersive_weathering.data.block_growths.area_condition.AreaCondition;
import com.ordana.immersive_weathering.data.block_growths.growths.ConfigurableBlockGrowth;
import com.ordana.immersive_weathering.mixins.accessors.RandomBlockMatchTestAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Separate classpath: never substitutes the production ConfigurableBlockGrowth. */
public final class PairedGrowthTest {
    private static final Unsafe UNSAFE = unsafe();
    private static final BlockPos ORIGIN = eligibleOrigin();
    private static int cases;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BuiltInRegistries.BLOCK.bindTags(Map.of(BlockTags.DIRT, List.of(Blocks.DIRT.builtInRegistryHolder())));
        BuiltInRegistries.FLUID.bindTags(Map.of(FluidTags.WATER,
                List.of(Fluids.WATER.builtInRegistryHolder(), Fluids.FLOWING_WATER.builtInRegistryHolder())));
        int pairs = 0;
        try (var files = Files.list(Path.of(args[0]))) {
            for (Path path : files.sorted().toList()) {
                JsonObject json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (!json.has("growth_for_face")) continue;
                for (var face : json.getAsJsonArray("growth_for_face")) {
                    for (var entry : face.getAsJsonObject().getAsJsonArray("growth")) {
                        JsonObject data = entry.getAsJsonObject().getAsJsonObject("data");
                        if (!data.has("above_block")) continue;
                        BlockPair pair = BlockPair.CODEC.parse(JsonOps.INSTANCE, data)
                                .getOrThrow(false, message -> { throw new AssertionError(message); });
                        boolean self = json.has("target_self") && json.get("target_self").getAsBoolean();
                        boolean water = path.getFileName().toString().equals("tall_seagrass.json");
                        AreaCondition area = self ? AreaCondition.CODEC.parse(JsonOps.INSTANCE, json.get("area_condition"))
                                .getOrThrow(false, message -> { throw new AssertionError(message); }) : AreaCondition.EMPTY;
                        paired(path.getFileName().toString(), pair, self, water,
                                json.get("growth_chance").getAsFloat(),
                                json.getAsJsonObject("replacing_target").get("probability").getAsFloat(), area);
                        pairs++;
                    }
                }
            }
        }
        check(pairs == 18, "Expected every shipped paired entry, got " + pairs);
        singles();
        controls();
        System.out.println("Paired growth: " + pairs + " shipped paired entries, " + cases + " placement/control cases passed");
    }

    private static void paired(String name, BlockPair pair, boolean self, boolean water, float chance,
                               float probability, AreaCondition area) throws Exception {
        BlockState shortPlant = name.equals("tall_fern.json") ? Blocks.FERN.defaultBlockState()
                : water ? Blocks.SEAGRASS.defaultBlockState() : Blocks.GRASS.defaultBlockState();
        for (Direction direction : new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH,
                Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            BlockPos target = self ? ORIGIN : ORIGIN.relative(direction);
            FixtureLevel level = world(target, self ? shortPlant : medium(water), water);
            RuleTest predicate = new ProbabilityRule(water ? Blocks.WATER : Blocks.AIR, probability);
            ConfigurableBlockGrowth growth = growth(pair, self, direction, predicate, area, false, chance);
            List<String> randomTrace = new ArrayList<>();
            CountingRandom random = new CountingRandom(randomTrace);
            setRandom(level, random);
            growth.tryGrowing(ORIGIN, level.getBlockState(ORIGIN), level, () -> {
                throw new AssertionError("Unexpected biome read");
            });
            check(level.getBlockState(target).equals(pair.getFirst()), name + "/" + direction + " lower missing: " + level.states
                    + " writes=" + level.writes + " rng=" + randomTrace);
            check(level.getBlockState(target.above()).equals(pair.getSecond()), name + "/" + direction + " upper missing: " + level.states);
            level.updateAll();
            check(level.getBlockState(target).equals(pair.getFirst()), name + " lower did not survive updates");
            check(level.getBlockState(target.above()).equals(pair.getSecond()), name + " upper did not survive updates");
            check(level.transientRemovals == 0, name + " transiently removed a double plant");
            check(level.placements.equals(List.of(target, target.above())), name + " placement order changed");
            check(randomTrace.equals(List.of("float", "int:1", "int:1")), "Changed world RNG draws: " + randomTrace);
            cases++;
        }
        for (int y : new int[]{319, -65}) {
            BlockPos boundary = new BlockPos(ORIGIN.getX(), y, ORIGIN.getZ());
            FixtureLevel level = world(boundary, shortPlant, water);
            Map<BlockPos, BlockState> before = Map.copyOf(level.states);
            growth(pair, true, Direction.UP, new BlockMatchTest(water ? Blocks.WATER : Blocks.AIR),
                    AreaCondition.EMPTY, false).tryGrowing(boundary, shortPlant, level, () -> null);
            check(level.writes == 0 && level.states.equals(before), name + " partially placed across build boundary");
            cases++;
        }
        for (BlockState upper : new BlockState[]{Blocks.STONE.defaultBlockState(),
                water ? Blocks.AIR.defaultBlockState() : Blocks.WATER.defaultBlockState(),
                water ? Blocks.LAVA.defaultBlockState() : Blocks.DIRT.defaultBlockState()}) {
            BlockPos target = self ? ORIGIN : ORIGIN.above();
            FixtureLevel level = world(target, self ? shortPlant : medium(water), water);
            level.states.put(target.above(), upper);
            Map<BlockPos, BlockState> before = Map.copyOf(level.states);
            growth(pair, self, Direction.UP, new ProbabilityRule(water ? Blocks.WATER : Blocks.AIR, 0.3F),
                    AreaCondition.EMPTY, false).tryGrowing(ORIGIN, level.getBlockState(ORIGIN), level, () -> null);
            check(level.states.equals(before) && level.writes == 0, name + " overwrote blocked/wrong-fluid upper");
            cases++;
        }
        BlockPos target = self ? ORIGIN : ORIGIN.above();
        FixtureLevel unsupported = world(target, self ? shortPlant : medium(water), water);
        unsupported.states.put(target.below(), Blocks.AIR.defaultBlockState());
        Map<BlockPos, BlockState> before = Map.copyOf(unsupported.states);
        growth(pair, self, Direction.UP, new BlockMatchTest(water ? Blocks.WATER : Blocks.AIR),
                AreaCondition.EMPTY, false).tryGrowing(ORIGIN, unsupported.getBlockState(ORIGIN), unsupported, () -> null);
        // Fence/pumpkin pairs do not require soil, unlike the double plants.
        if (!pair.getFirst().canSurvive(unsupported, target)) {
            check(unsupported.states.equals(before) && unsupported.writes == 0, name + " ignored survival");
        }
        cases++;
    }

    private static void singles() throws Exception {
        BlockPos target = ORIGIN.above();
        FixtureLevel level = world(target, Blocks.AIR.defaultBlockState(), false);
        growth(BlockPair.of(Blocks.POPPY), false, Direction.UP, new BlockMatchTest(Blocks.AIR),
                AreaCondition.EMPTY, false).tryGrowing(ORIGIN, Blocks.DIRT.defaultBlockState(), level, () -> null);
        check(level.getBlockState(target).is(Blocks.POPPY) && level.writes == 1, "Single block changed");
        cases++;
        level = world(target, Blocks.WATER.defaultBlockState(), true);
        growth(BlockPair.of(Blocks.OAK_FENCE), false, Direction.UP, new BlockMatchTest(Blocks.WATER),
                AreaCondition.EMPTY, false).tryGrowing(ORIGIN, Blocks.DIRT.defaultBlockState(), level, () -> null);
        check(level.getBlockState(target).getValue(BlockStateProperties.WATERLOGGED),
                "Single waterlogging changed");
        cases++;
        for (Direction dir : Direction.values()) {
            for (boolean self : new boolean[]{false, true}) {
                for (boolean water : new boolean[]{false, true}) {
                    for (boolean blocked : new boolean[]{false, true}) {
                        for (boolean destroy : new boolean[]{false, true}) {
                            differentialSingle(dir, self, water, blocked, destroy);
                        }
                    }
                }
            }
        }
    }

    private static void differentialSingle(Direction dir, boolean self, boolean water, boolean blocked,
                                           boolean destroy) throws Exception {
        BlockPos target = self ? ORIGIN : ORIGIN.relative(dir);
        BlockState old = blocked ? Blocks.STONE.defaultBlockState() : medium(water);
        FixtureLevel actual = world(target, old, water);
        FixtureLevel baseline = world(target, old, water);
        BlockState output = (water ? Blocks.OAK_FENCE : Blocks.POPPY).defaultBlockState();
        ProbabilityRule rule = new ProbabilityRule(water ? Blocks.WATER : Blocks.AIR, 0.3F);
        List<String> actualTrace = new ArrayList<>();
        List<String> baselineTrace = new ArrayList<>();
        setRandom(actual, new CountingRandom(actualTrace));
        setRandom(baseline, new CountingRandom(baselineTrace));
        growth(BlockPair.of(output), self, dir, rule, AreaCondition.EMPTY, destroy)
                .tryGrowing(ORIGIN, actual.getBlockState(ORIGIN), actual, () -> null);
        baselineSingle(baseline, output, self, dir, rule, destroy);
        check(actual.states.equals(baseline.states) && actual.placements.equals(baseline.placements)
                        && actualTrace.equals(baselineTrace) && actual.destroys.equals(baseline.destroys),
                "Single-block differential mismatch: " + dir + "/" + self + "/" + water + "/" + blocked + "/" + destroy);
        cases++;
    }

    // Original single-output control flow; the paired correction must not affect it.
    private static void baselineSingle(FixtureLevel level, BlockState output, boolean self, Direction dir,
                                       ProbabilityRule rule, boolean destroy) {
        if (level.random.nextFloat() >= 1) return;
        SimpleWeightedRandomList.<Direction>builder().add(dir, 1).build().getRandomValue(level.random);
        RandomSource seed = RandomSource.create(Mth.getSeed(ORIGIN));
        BlockPos targetPos = self ? ORIGIN : ORIGIN.relative(dir);
        BlockState target = level.getBlockState(targetPos);
        if (!self && !rule.test(target, seed)) return;
        if (self && seed.nextFloat() >= rule.getProbability()) return;
        SimpleWeightedRandomList.<BlockPair>builder().add(BlockPair.of(output), 1).build().getRandomValue(level.random);
        if (!output.canSurvive(level, targetPos)) return;
        if (destroy) level.destroyBlock(targetPos, true);
        BlockState placed = output.hasProperty(BlockStateProperties.WATERLOGGED) && target.getFluidState().is(FluidTags.WATER)
                ? output.setValue(BlockStateProperties.WATERLOGGED, true)
                : Block.updateFromNeighbourShapes(output, level, targetPos);
        level.setBlockAndUpdate(targetPos, placed);
    }

    private static void controls() throws Exception {
        BlockState lower = Blocks.TALL_GRASS.defaultBlockState();
        BlockState upper = lower.setValue(DoublePlantBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);
        BlockPair pair = BlockPair.of(lower, upper);
        AreaCondition veto = new AreaCondition() {
            @Override
            public boolean test(BlockPos pos, net.minecraft.world.level.Level level, ConfigurableBlockGrowth config) {
                return false;
            }

            @Override
            public int getMaxRange() {
                return 0;
            }

            @Override
            public AreaConditionType<?> getType() {
                return null;
            }
        };
        FixtureLevel level = world(ORIGIN, Blocks.GRASS.defaultBlockState(), false);
        Map<BlockPos, BlockState> before = Map.copyOf(level.states);
        growth(pair, true, Direction.UP, new BlockMatchTest(Blocks.AIR), veto, false)
                .tryGrowing(ORIGIN, Blocks.GRASS.defaultBlockState(), level, () -> null);
        check(level.writes == 0 && level.states.equals(before), "Area veto changed");
        cases++;
        level = world(ORIGIN, Blocks.GRASS.defaultBlockState(), false);
        growth(pair, true, Direction.UP, new ProbabilityRule(Blocks.AIR, 0), AreaCondition.EMPTY, false)
                .tryGrowing(ORIGIN, Blocks.GRASS.defaultBlockState(), level, () -> null);
        check(level.writes == 0, "Self probability gate changed");
        cases++;
        // A waterlogged block is not a water block: retain the shipped seagrass replacement rule.
        BlockState seagrassLower = Blocks.TALL_SEAGRASS.defaultBlockState();
        BlockPair seagrass = BlockPair.of(seagrassLower, seagrassLower.setValue(DoublePlantBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        level = world(ORIGIN, Blocks.SEAGRASS.defaultBlockState(), true);
        level.states.put(ORIGIN.above(), Blocks.OAK_FENCE.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        growth(seagrass, true, Direction.UP, new BlockMatchTest(Blocks.WATER), AreaCondition.EMPTY, false)
                .tryGrowing(ORIGIN, Blocks.SEAGRASS.defaultBlockState(), level, () -> null);
        check(level.writes == 0, "Waterlogged obstruction overwritten");
        cases++;
        for (float chance : new float[]{0, 0.001F, 0.005F}) {
            level = world(ORIGIN, Blocks.GRASS.defaultBlockState(), false);
            List<String> trace = new ArrayList<>();
            setRandom(level, new CountingRandom(trace, 0.5F));
            growth(pair, true, Direction.UP, new BlockMatchTest(Blocks.AIR), AreaCondition.EMPTY, false, chance)
                    .tryGrowing(ORIGIN, Blocks.GRASS.defaultBlockState(), level, () -> null);
            check(level.writes == 0 && trace.equals(chance == 0 ? List.of() : List.of("float")),
                    "Chance rejection/RNG changed");
            cases++;
        }
        level = world(ORIGIN, Blocks.WATER.defaultBlockState(), true);
        level.states.put(ORIGIN.above().east(), Blocks.STONE.defaultBlockState());
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        growth(BlockPair.of(fence, fence), true, Direction.NORTH, new BlockMatchTest(Blocks.WATER),
                AreaCondition.EMPTY, false).tryGrowing(ORIGIN, Blocks.WATER.defaultBlockState(), level, () -> null);
        check(level.getBlockState(ORIGIN).getValue(BlockStateProperties.WATERLOGGED)
                        && level.getBlockState(ORIGIN.above()).getValue(BlockStateProperties.WATERLOGGED),
                "Paired waterlogging lost");
        cases++;
        level = world(ORIGIN, Blocks.AIR.defaultBlockState(), false);
        level.states.put(ORIGIN.above().east(), Blocks.STONE.defaultBlockState());
        growth(BlockPair.of(fence, fence), true, Direction.NORTH, new BlockMatchTest(Blocks.AIR),
                AreaCondition.EMPTY, false).tryGrowing(ORIGIN, Blocks.AIR.defaultBlockState(), level, () -> null);
        check(!level.getBlockState(ORIGIN).getValue(BlockStateProperties.EAST)
                        && level.getBlockState(ORIGIN.above()).getValue(BlockStateProperties.EAST),
                "Paired non-plant shape used wrong position");
        cases++;
    }

    private static ConfigurableBlockGrowth growth(BlockPair pair, boolean self, Direction dir,
                                                   RuleTest rule, AreaCondition area, boolean destroy) {
        return growth(pair, self, dir, rule, area, destroy, 1);
    }

    private static ConfigurableBlockGrowth growth(BlockPair pair, boolean self, Direction dir,
                                                   RuleTest rule, AreaCondition area, boolean destroy, float chance) {
        return new ConfigurableBlockGrowth(List.of(TickSource.BLOCK_TICK), chance, rule, area,
                List.of(new ConfigurableBlockGrowth.DirectionalList(Optional.of(dir), Optional.of(1),
                        SimpleWeightedRandomList.<BlockPair>builder().add(pair, 1).build())),
                Optional.empty(), List.of(), self, destroy);
    }

    private static BlockState medium(boolean water) {
        return (water ? Blocks.WATER : Blocks.AIR).defaultBlockState();
    }

    private static FixtureLevel world(BlockPos target, BlockState first, boolean water) throws Exception {
        FixtureLevel level = (FixtureLevel) UNSAFE.allocateInstance(FixtureLevel.class);
        level.states = new HashMap<>();
        level.placements = new ArrayList<>();
        level.destroys = new ArrayList<>();
        level.states.put(target.below(), Blocks.DIRT.defaultBlockState());
        level.states.put(target, first);
        level.states.put(target.above(), medium(water));
        setRandom(level, new CountingRandom(new ArrayList<>()));
        return level;
    }

    private static void setRandom(FixtureLevel level, RandomSource random) throws Exception {
        Field field = net.minecraft.world.level.Level.class.getDeclaredField("random");
        UNSAFE.putObject(level, UNSAFE.objectFieldOffset(field), random);
    }

    private static BlockPos eligibleOrigin() {
        for (int x = 0; ; x++) {
            BlockPos pos = new BlockPos(x, 64, 0);
            if (RandomSource.create(Mth.getSeed(pos)).nextFloat() < 0.3F) return pos;
        }
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
        if (!condition) throw new AssertionError(message);
    }

    private static final class ProbabilityRule extends RandomBlockMatchTest implements RandomBlockMatchTestAccessor {
        private final float probability;

        private ProbabilityRule(Block block, float probability) {
            super(block, probability);
            this.probability = probability;
        }

        @Override
        public float getProbability() {
            return probability;
        }
    }

    private static final class CountingRandom extends net.minecraft.world.level.levelgen.LegacyRandomSource {
        private final List<String> trace;
        private final float floatValue;

        private CountingRandom(List<String> trace) {
            this(trace, 0);
        }

        private CountingRandom(List<String> trace, float floatValue) {
            super(123);
            this.trace = trace;
            this.floatValue = floatValue;
        }

        @Override
        public float nextFloat() {
            trace.add("float");
            return floatValue;
        }

        @Override
        public int nextInt(int bound) {
            trace.add("int:" + bound);
            return 0;
        }
    }

    private static final class FixtureLevel extends ServerLevel {
        private Map<BlockPos, BlockState> states;
        private int writes;
        private int transientRemovals;
        private List<BlockPos> placements;
        private List<BlockPos> destroys;

        private FixtureLevel() {
            super(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            throw new AssertionError("World constructor must not run");
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return getBlockState(pos).getFluidState();
        }

        @Override
        public boolean hasChunksAt(BlockPos from, BlockPos to) {
            return true;
        }

        @Override
        public int getMinBuildHeight() {
            return -64;
        }

        @Override
        public int getHeight() {
            return 384;
        }

        @Override
        public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            check(!isOutsideBuildHeight(pos), "Attempted out-of-height write");
            states.put(pos.immutable(), state);
            placements.add(pos.immutable());
            writes++;
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                BlockState old = getBlockState(neighbor);
                BlockState updated = old.updateShape(dir.getOpposite(), state, this, neighbor, pos);
                if (!old.equals(updated)) {
                    if (old.getBlock() instanceof DoublePlantBlock) transientRemovals++;
                    states.put(neighbor, updated);
                }
            }
            return true;
        }

        private void updateAll() {
            for (int pass = 0; pass < 3; pass++) {
                for (BlockPos pos : List.copyOf(states.keySet())) {
                    BlockState state = getBlockState(pos);
                    for (Direction dir : Direction.values()) {
                        state = state.updateShape(dir, getBlockState(pos.relative(dir)), this, pos, pos.relative(dir));
                    }
                    if (getBlockState(pos).getBlock() instanceof DoublePlantBlock && !getBlockState(pos).equals(state)) {
                        transientRemovals++;
                    }
                    states.put(pos, state);
                }
            }
        }

        @Override
        public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {
        }

        @Override
        public void scheduleTick(BlockPos pos, Block block, int delay) {
        }

        @Override
        public boolean destroyBlock(BlockPos pos, boolean drops, net.minecraft.world.entity.Entity entity, int recursion) {
            destroys.add(pos.immutable());
            if (getBlockState(pos).isAir()) return false;
            return setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL, recursion);
        }
    }
}
