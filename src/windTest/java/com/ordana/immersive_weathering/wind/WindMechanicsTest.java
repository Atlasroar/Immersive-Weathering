package com.ordana.immersive_weathering.wind;

import com.mojang.authlib.GameProfile;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModEntities;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

/** Production rules/native block states; world storage/config initialization are fixtures, not a woven game. */
public final class WindMechanicsTest {
    private static final Unsafe UNSAFE = unsafe();
    private static final BlockPos POS = new BlockPos(0, 64, 0);
    private static final class DimensionFixture {
        static final net.minecraft.world.level.dimension.DimensionType TYPE =
            new net.minecraft.world.level.dimension.DimensionType(OptionalLong.empty(), true, false, false, true,
                    1, true, false, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD,
                    new net.minecraft.resources.ResourceLocation("minecraft:overworld"), 0,
                    new net.minecraft.world.level.dimension.DimensionType.MonsterSettings(false, true,
                            net.minecraft.util.valueproviders.UniformInt.of(0, 7), 0));
    }
    private static int cases;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Field boot = Bootstrap.class.getDeclaredField("isBootstrapped");
        boot.setAccessible(true);
        boot.setBoolean(null, true);
        BuiltInRegistries.REGISTRY.keySet();
        com.ordana.immersive_weathering.snowstorm.SnowstormMechanicsTest.prepare();
        int i = 0;
        for (var supplier : List.of(ModBlocks.UNLIT_TORCH, ModBlocks.UNLIT_SOUL_TORCH, ModBlocks.UNLIT_WALL_TORCH,
                ModBlocks.UNLIT_SOUL_WALL_TORCH, ModBlocks.THIN_ICE, ModBlocks.ICICLE)) {
            Registry.register(BuiltInRegistries.BLOCK, "iw_wind:block_" + i++, supplier.get());
        }
        Registry.register(BuiltInRegistries.ENTITY_TYPE, "iw_wind:wind", ModEntities.FREEZING_WIND.get());
        boot.setBoolean(null, false);
        Bootstrap.bootStrap();
        for (var supplier : List.of(ModBlocks.UNLIT_TORCH, ModBlocks.UNLIT_SOUL_TORCH, ModBlocks.UNLIT_WALL_TORCH,
                ModBlocks.UNLIT_SOUL_WALL_TORCH, ModBlocks.THIN_ICE, ModBlocks.ICICLE)) {
            supplier.get().getStateDefinition().getPossibleStates().forEach(BlockState::initCache);
        }
        BuiltInRegistries.BLOCK.bindTags(Map.of(
                BlockTags.FIRE, List.of(Blocks.FIRE.builtInRegistryHolder(), Blocks.SOUL_FIRE.builtInRegistryHolder()),
                BlockTags.CANDLES, List.of(Blocks.CANDLE.builtInRegistryHolder()),
                BlockTags.CANDLE_CAKES, List.of(Blocks.CANDLE_CAKE.builtInRegistryHolder()),
                BlockTags.CAMPFIRES, List.of(Blocks.CAMPFIRE.builtInRegistryHolder(), Blocks.SOUL_CAMPFIRE.builtInRegistryHolder())));
        probability();
        torches();
        blocks();
        lifecycle();
        contact();
        spawning();
        com.ordana.immersive_weathering.snowstorm.SnowstormMechanicsTest.run();
        System.out.println("Freezing wind: " + cases + " production/native assertions passed (no game/Mixin weaving)");
    }

    private static void probability() {
        WindSettings settings = WindSettings.current();
        near(settings.chance(2, 64, false), 0.001, "Hot biome baseline");
        near(settings.chance(-0.5F, 64, false), 0.02, "Cold biome additive bonus");
        near(settings.chance(-0.5F, 100, false), 0.04, "Cold mountain independent altitude bonus");
        near(settings.chance(-0.5F, 100, true), 0.12, "Thunder additive bonus");
        for (int y : new int[]{-64, 99, 100, 319, 4096}) {
            double previous = 1;
            for (float temperature = -3; temperature < 4; temperature += 0.05F) {
                double chance = settings.chance(temperature, y, false);
                check(chance >= 0 && chance <= 1 && chance <= previous + 1e-8, "Colder never less likely; bounded probability");
                previous = chance;
            }
        }
        near(new WindSettings(1, 1, 100, 1, 1).chance(-100, 200, true), 1, "Final chance cap");
        near(new WindSettings(0, 0, 100, 0, 0).chance(-100, 200, true), 0, "Zero controls");
    }

    private static void torches() throws Exception {
        Fixture level = world();
        for (boolean soul : new boolean[]{false, true}) {
            Block standing = soul ? Blocks.SOUL_TORCH : Blocks.TORCH;
            Block wall = soul ? Blocks.SOUL_WALL_TORCH : Blocks.WALL_TORCH;
            BlockState unlit = WindBlocks.unlitTorch(standing.defaultBlockState());
            check(unlit.getLightEmission() == 0, "Unlit means zero light");
            check(unlit.getBlock().getCloneItemStack(level, POS, unlit).is(soul ? Items.SOUL_TORCH : Items.TORCH), "Pick preserves identity");
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState litWall = wall.defaultBlockState().setValue(WallTorchBlock.FACING, direction);
                BlockState unlitWall = WindBlocks.unlitTorch(litWall);
                check(unlitWall.getValue(WallTorchBlock.FACING) == direction, "Every wall orientation preserved");
                for (Item ignition : List.of(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE, Items.TORCH, Items.SOUL_TORCH)) {
                    ProbePlayer player = new ProbePlayer(level, ignition);
                    unlitWall.use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false));
                    check(level.states.get(POS).equals(litWall), "Relight restores exact native wall identity/state");
                    check(player.held.getCount() == (ignition == Items.FIRE_CHARGE ? 1 : 2), "Charge consumed; torch not consumed");
                }
            }
            ProbePlayer player = new ProbePlayer(level, Items.FLINT_AND_STEEL);
            unlit.use(level, player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false));
            check(level.states.get(POS).is(standing), "Standing torch relights normally");
        }
        check(WindBlocks.unlitTorch(Blocks.REDSTONE_TORCH.defaultBlockState()) == null, "Redstone excluded");
        for (Block block : List.of(Blocks.FURNACE, Blocks.LANTERN, Blocks.REDSTONE_LAMP)) {
            check(!WindBlocks.applicableFlame(block.defaultBlockState()), "Lit machine/lantern excluded");
        }
    }

    private static void blocks() throws Exception {
        Fixture level = world();
        FreezingWindEntity gust = new FreezingWindEntity(ModEntities.FREEZING_WIND.get(), level);
        gust.setPos(0.5, 64, 0.5);
        for (Block flame : List.of(Blocks.FIRE, Blocks.SOUL_FIRE, Blocks.TORCH, Blocks.SOUL_TORCH,
                Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE, Blocks.CANDLE, Blocks.CANDLE_CAKE)) {
            BlockState state = flame.defaultBlockState();
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT)) {
                state = state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT, true);
            }
            level.states.put(POS, state);
            check(WindBlocks.extinguish(level, POS, state), "All applicable flames extinguished: " + flame);
            BlockState result = level.states.get(POS);
            check(!result.is(flame) || !result.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT), "Flame off");
        }
        check(WindBlocks.sourceWater(Blocks.WATER.defaultBlockState()), "Source water eligible");
        check(!WindBlocks.sourceWater(Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1)), "Flowing water excluded");
        check(!WindBlocks.sourceWater(Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.WATERLOGGED, true)), "Waterlogged excluded");
        level.states.clear();
        level.states.put(POS.below(), Blocks.WATER.defaultBlockState());
        CommonConfigs.icicles = 0;
        WindBlocks.interact(level, gust);
        check(level.states.get(POS.below()).is(ModBlocks.THIN_ICE.get()), "Source becomes actual IW thin ice");
        level.states.clear();
        level.states.put(POS.above(), Blocks.STONE.defaultBlockState());
        CommonConfigs.icicles = 1;
        WindBlocks.interact(level, gust);
        check(level.states.get(POS).is(ModBlocks.ICICLE.get()), "Any geometrically valid stone underside grows native icicle");
        level.states.clear();
        level.states.put(POS.above(), Blocks.STONE.defaultBlockState());
        CommonConfigs.disableIcicles = true;
        WindBlocks.interact(level, gust);
        check(!level.states.containsKey(POS), "Global icicle disable honored");
        CommonConfigs.disableIcicles = false;
        level.states.put(POS, Blocks.CHEST.defaultBlockState());
        WindBlocks.interact(level, gust);
        check(level.states.get(POS).is(Blocks.CHEST), "Occupied underside preserved");
        level.states.clear();
        level.states.put(POS.above(), Blocks.STONE.defaultBlockState());
        level.states.put(POS.above(2), Blocks.TORCH.defaultBlockState());
        WindBlocks.interact(level, gust);
        check(level.states.get(POS.above(2)).is(Blocks.TORCH), "Actual native ray blocks extinguishing through a solid ceiling");
        level.rules.getRule(GameRules.RULE_MOBGRIEFING).set(false, null);
        level.writes = level.reads = 0;
        WindBlocks.interact(level, gust);
        check(level.writes == 0 && level.reads == 0, "Gamerule stops all block reads/writes");
        level.rules.getRule(GameRules.RULE_MOBGRIEFING).set(true, null);
        level.loaded = false;
        WindBlocks.interact(level, gust);
        check(level.writes == 0 && level.reads == 0, "Unloaded cells never read or written");
        level.loaded = true;
        gust.setPos(0.5, 400, 0.5);
        WindBlocks.interact(level, gust);
        check(level.writes == 0 && level.reads == 0, "Build-height gate before storage");
        CommonConfigs.icicles = 0.05;
    }

    private static void lifecycle() throws Exception {
        Fixture level = world();
        FreezingWindEntity gust = new FreezingWindEntity(ModEntities.FREEZING_WIND.get(), level);
        gust.setPos(0.5, 64, 0.5);
        check(gust.fireImmune() && gust.isNoGravity() && !gust.shouldBeSaved(), "Ephemeral no-gravity/fire-immune gust");
        check(gust.getBbWidth() == 2 && gust.getBbHeight() == 2, "Authentic 2x2 gust");
        check(FreezingWind.loaded(level, gust.getBoundingBox()), "Loaded volume accepted");
        level.loaded = false;
        check(!FreezingWind.loaded(level, gust.getBoundingBox()), "No forced chunks");
        level.loaded = true;
        check(!FreezingWind.loaded(level, new AABB(0, 400, 0, 2, 402, 2)), "No out-of-height movement");
        double start = gust.getX();
        int ticks = 0;
        while (!gust.isRemoved() && ticks < 150) {
            gust.tickCount++;
            gust.tick();
            ticks++;
            if (!gust.isRemoved()) near(gust.getX(), start - ticks / 10, "Discrete one-block movement only every ten ticks");
        }
        check(gust.isRemoved() && ticks >= 80 && ticks <= 140, "Server-authoritative finite random life");
        level = world();
        gust = new FreezingWindEntity(ModEntities.FREEZING_WIND.get(), level);
        gust.setPos(0.5, 64, 0.5);
        CommonConfigs.enabled = false;
        gust.tick();
        check(gust.isRemoved(), "Runtime disable discards active gust");
        CommonConfigs.enabled = true;
    }

    private static void contact() throws Exception {
        Fixture level = world();
        FreezingWindEntity gust = new FreezingWindEntity(ModEntities.FREEZING_WIND.get(), level);
        gust.setPos(0.5, 64, 0.5);
        ProbePlayer target = new ProbePlayer(level, Items.STICK);
        target.setPos(0.5, 64, 0.5);
        level.entities.add(target);
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(FreezingWindEntity.HEAVY, List.of()));
        float health = target.getHealth();
        gust.tickCount = 5;
        gust.tick();
        near(target.getDeltaMovement().x, -0.16, "Native hit pushes west");
        var slow = target.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        check(slow != null && slow.getDuration() == 60 && slow.getAmplifier() == 0, "Actual hit applies IW vanilla Slowness default");
        check(target.getTicksFrozen() == 0 && target.getHealth() == health, "No freeze ticks or damage on hit");
        target.setDeltaMovement(Vec3.ZERO);
        target.removeAllEffects();
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(FreezingWindEntity.HEAVY, List.of(EntityType.PLAYER.builtInRegistryHolder())));
        gust.tick();
        check(target.getDeltaMovement().equals(Vec3.ZERO) && !target.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),
                "Datapack heavy immunity prevents push and effect");
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(FreezingWindEntity.HEAVY, List.of()));
        CommonConfigs.slowTicks = 0;
        gust.tick();
        check(!target.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN), "Zero duration disables effect");
        CommonConfigs.slowTicks = 60;
    }

    private static void spawning() throws Exception {
        Fixture level = world();
        ProbeChunk chunk = (ProbeChunk) UNSAFE.allocateInstance(ProbeChunk.class);
        Field position = net.minecraft.world.level.chunk.ChunkAccess.class.getDeclaredField("chunkPos");
        UNSAFE.putObject(chunk, UNSAFE.objectFieldOffset(position), new ChunkPos(0, 0));
        CommonConfigs.base = 1;
        CommonConfigs.liveCap = 1;
        level.night = false;
        FreezingWind.tickChunk(level, chunk);
        check(level.spawns.isEmpty(), "Ambient daytime forbidden even with deterministic probability");
        level.night = true;
        level.time = 1;
        FreezingWind.tickChunk(level, chunk);
        check(level.spawns.isEmpty(), "Exactly one chunk phase in each twenty ticks");
        level.time = 0;
        FreezingWind.tickChunk(level, chunk);
        check(level.spawns.size() == 1, "Exposed all-biome ambient night spawn");
        check(level.spawns.get(0).getY() >= 64 && level.spawns.get(0).getY() <= 72, "Surface through surface+8 spawn band");
        level.time = 20;
        FreezingWind.tickChunk(level, chunk);
        check(level.spawns.size() == 1, "Live cap persists across budget reset");
        level.spawns.get(0).discard();
        FreezingWind.tickChunk(level, chunk);
        check(level.spawns.size() == 2, "Removed/unloaded gust releases live slot without stale count");
        Fixture other = world();
        FreezingWind.tickChunk(other, chunk);
        check(other.spawns.size() == 1, "Dimension budgets independent");
        other.spawns.get(0).discard();
        CommonConfigs.liveCap = 32;
        FreezingWind.tickChunk(other, chunk);
        FreezingWind.tickChunk(other, chunk);
        check(other.spawns.size() == 2, "Two successful spawns per world per twenty-tick budget");
        other.time = 20;
        FreezingWind.tickChunk(other, chunk);
        check(other.spawns.size() == 3, "Per-world second resets successful-spawn budget");
        Fixture storm = world();
        storm.night = false;
        storm.thunder = storm.rain = true;
        storm.biome = nativeBiome(-0.5F);
        FreezingWind.tickChunk(storm, chunk);
        check(storm.spawns.size() == 1, "Cold thunder may generate daytime wind");
        storm.biome = nativeBiome(2);
        storm.time = 20;
        FreezingWind.tickChunk(storm, chunk);
        check(storm.spawns.size() == 1, "Hot thunder does not bypass ambient night gate");
        CommonConfigs.budget = 0;
        Fixture disabled = world();
        FreezingWind.tickChunk(disabled, chunk);
        check(disabled.spawns.isEmpty(), "Zero spawn budget exits");
        CommonConfigs.budget = 2;
        disabled.loaded = false;
        FreezingWind.tickChunk(disabled, chunk);
        check(disabled.spawns.isEmpty() && disabled.reads == 0, "No unloaded spawn storage reads");
        CommonConfigs.base = 0.001;
        CommonConfigs.liveCap = 32;
    }

    private static Holder<net.minecraft.world.level.biome.Biome> nativeBiome(float temperature) {
        return Holder.direct(new net.minecraft.world.level.biome.Biome.BiomeBuilder()
                .hasPrecipitation(true).temperature(temperature).downfall(0.5F)
                .specialEffects(new net.minecraft.world.level.biome.BiomeSpecialEffects.Builder()
                        .fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build())
                .generationSettings(net.minecraft.world.level.biome.BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(net.minecraft.world.level.biome.MobSpawnSettings.EMPTY).build());
    }

    private static Fixture world() throws Exception {
        Fixture level = (Fixture) UNSAFE.allocateInstance(Fixture.class);
        level.states = new HashMap<>();
        level.rules = new GameRules();
        level.entities = new ArrayList<>();
        level.spawns = new ArrayList<>();
        level.biome = nativeBiome(2);
        level.night = true;
        level.loaded = true;
        for (String name : List.of("random", "threadSafeRandom")) {
            Field field = Level.class.getDeclaredField(name);
            UNSAFE.putObject(level, UNSAFE.objectFieldOffset(field), new net.minecraft.world.level.levelgen.LegacyRandomSource(1));
        }
        return level;
    }

    private static final class Fixture extends ServerLevel {
        Map<BlockPos, BlockState> states;
        List<Entity> entities;
        List<Entity> spawns;
        Holder<net.minecraft.world.level.biome.Biome> biome;
        boolean night, rain, thunder;
        long time;
        GameRules rules;
        boolean loaded;
        int reads, writes;
        private Fixture() { super(null, null, null, null, null, null, null, false, 0, List.of(), false, null); throw new AssertionError(); }
        @Override public BlockState getBlockState(BlockPos pos) {
            check(loaded && !isOutsideBuildHeight(pos), "No invalid storage read");
            reads++;
            return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }
        @Override public FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            writes++;
            states.put(pos.immutable(), state);
            return true;
        }
        @Override public boolean removeBlock(BlockPos pos, boolean moving) { return setBlock(pos, Blocks.AIR.defaultBlockState(), 3); }
        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public net.minecraft.world.level.dimension.DimensionType dimensionType() { return DimensionFixture.TYPE; }
        @Override public boolean hasChunksAt(BlockPos min, BlockPos max) { return loaded; }
        @Override public boolean hasChunkAt(BlockPos pos) { return loaded; }
        @Override public boolean hasChunksAt(int x1, int z1, int x2, int z2) { return loaded; }
        @Override public BlockGetter getChunkForCollisions(int x, int z) { return this; }
        @Override public List<Entity> getEntities(Entity except, AABB area, java.util.function.Predicate<? super Entity> predicate) { return List.of(); }
        @Override public <T extends Entity> List<T> getEntitiesOfClass(Class<T> type, AABB area, java.util.function.Predicate<? super T> predicate) {
            return entities.stream().filter(type::isInstance).map(type::cast)
                    .filter(entity -> area.intersects(entity.getBoundingBox()) && predicate.test(entity)).toList();
        }
        @Override public boolean canSeeSky(BlockPos pos) { return true; }
        @Override public int getHeight(net.minecraft.world.level.levelgen.Heightmap.Types type, int x, int z) { return 64; }
        @Override public boolean isNight() { return night; }
        @Override public boolean isRaining() { return rain; }
        @Override public boolean isThundering() { return thunder; }
        @Override public long getGameTime() { return time; }
        @Override public Holder<net.minecraft.world.level.biome.Biome> getBiome(BlockPos pos) { return biome; }
        @Override public boolean addFreshEntity(Entity entity) { spawns.add(entity); return true; }
        @Override public int getBrightness(LightLayer layer, BlockPos pos) { return 0; }
        @Override public GameRules getGameRules() { return rules; }
        @Override public WorldBorder getWorldBorder() { return new WorldBorder(); }
        @Override public RegistryAccess registryAccess() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
        @Override public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() { return net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS; }
        @Override public void gameEvent(GameEvent event, Vec3 pos, GameEvent.Context context) {}
        @Override public void playSeededSound(Player player, double x, double y, double z, Holder<SoundEvent> event, SoundSource source, float volume, float pitch, long seed) {}
        @Override public net.minecraft.util.profiling.ProfilerFiller getProfiler() { return InactiveProfiler.INSTANCE; }
    }

    private static final class ProbeChunk extends net.minecraft.world.level.chunk.LevelChunk {
        private ProbeChunk() { super(null, null); throw new AssertionError(); }
        @Override public int getHeight(net.minecraft.world.level.levelgen.Heightmap.Types type, int x, int z) { return 63; }
    }

    private static final class ProbePlayer extends Player {
        final ItemStack held;
        ProbePlayer(Level level, Item item) {
            super(level, POS, 0, new GameProfile(new UUID(0, 1), "wind-test"));
            held = new ItemStack(item, 2);
        }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
        @Override public ItemStack getItemInHand(InteractionHand hand) { return held; }
        @Override public boolean mayUseItemAt(BlockPos pos, Direction face, ItemStack item) { return true; }
    }

    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
    }
    private static void check(boolean value, String message) { cases++; if (!value) throw new AssertionError(message); }
    private static void near(double value, double expected, String message) { check(Math.abs(value - expected) < 1e-6, message); }
}
