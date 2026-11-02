package com.ordana.immersive_weathering.snowstorm;

import com.ordana.immersive_weathering.client.SnowstormEffects;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.snow.SnowCoveredBlock;
import com.ordana.immersive_weathering.snow.SnowHosts;
import com.ordana.immersive_weathering.snow.SnowWeather;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/** Native biome/weather decisions and pure presentation bounds, not renderer or Mixin weaving coverage. */
public final class SnowstormMechanicsTest {
    private static int cases;
    private static final BlockPos POS = new BlockPos(0, 100, 0);
    private static SnowCoveredBlock fence;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Field boot = Bootstrap.class.getDeclaredField("isBootstrapped");
        boot.setAccessible(true);
        boot.setBoolean(null, true);
        BuiltInRegistries.REGISTRY.keySet();
        prepare();
        boot.setBoolean(null, false);
        Bootstrap.bootStrap();
        run();
    }

    public static void prepare() {
        fence = Registry.register(BuiltInRegistries.BLOCK, "iw_snowstorm:covered_fence",
                SnowCoveredBlock.create(Blocks.OAK_FENCE));
        SnowHosts.register(Blocks.OAK_FENCE, () -> fence);
    }

    public static void run() throws Exception {
        cases = 0;
        Biome cold = biome(-0.5F, true);
        check(Snowstorms.snowyBiome(cold, POS), "Native snowy cold biome");
        check(!Snowstorms.snowyBiome(biome(0.8F, true), POS), "Warm rain is not snow");
        check(!Snowstorms.snowyBiome(biome(-0.5F, false), POS), "Dry cold is not snow");
        check(Snowstorms.depositionLayers(1, 8, true, 1) == 3, "Storm adds one bonus layer");
        check(Snowstorms.depositionLayers(1, 8, false, 3) == 2, "Ordinary weather remains one layer");
        check(Snowstorms.depositionLayers(1, 8, true, -1) == 2, "Negative bonus clamps");
        check(Snowstorms.depositionLayers(1, 8, true, 100) == 5, "Bonus bounded to three");
        check(Snowstorms.depositionLayers(7, 8, true, 3) == 8, "Configured cap remains authoritative");
        check(Snowstorms.depositionLayers(1, 2, true, 3) == 2, "Gamerule-derived cap remains authoritative");

        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        Fixture level = (Fixture) unsafe.allocateInstance(Fixture.class);
        level.biome = Holder.direct(cold);
        level.loaded = level.rain = level.thunder = level.sky = true;
        level.top = 100;
        check(Snowstorms.isSnowstorm(level, POS), "Exposed thunder snowfall");
        CommonConfigs.stormEnabled = false;
        try {
            check(!Snowstorms.isSnowstorm(level, POS), "Common toggle disables snowstorm gameplay");
        } finally {
            CommonConfigs.stormEnabled = true;
        }
        level.thunder = false;
        check(!Snowstorms.isSnowstorm(level, POS), "Ordinary snowfall is not a snowstorm");
        level.thunder = true;
        level.rain = false;
        check(!Snowstorms.isSnowstorm(level, POS), "Thunder flag without rain rejected");
        level.rain = true;
        level.sky = false;
        check(!Snowstorms.isSnowstorm(level, POS), "Roof blocks gameplay");
        check(Snowstorms.visualStorm(level, POS), "Indoor presentation retains biome weather");
        level.sky = true;
        level.top = 101;
        check(!Snowstorms.isSnowstorm(level, POS), "Heightmap exposure gate");
        level.top = 100;
        level.loaded = false;
        check(!Snowstorms.isSnowstorm(level, POS), "Unloaded chunks never read biome");
        level.loaded = true;
        check(!Snowstorms.isSnowstorm(level, POS.atY(400)), "Outside build height rejected");

        check(SnowstormEffects.particleRarity(ParticleStatus.MINIMAL, 60) == 0, "Minimal disables particles");
        check(SnowstormEffects.particleRarity(ParticleStatus.DECREASED, 60) == 120, "Decreased halves trial rate");
        check(SnowstormEffects.particleRarity(ParticleStatus.ALL, 60) == 60, "All keeps trial rate");
        Vec3 fog = SnowstormEffects.fogDistances(0, 12, 8, 1, 16, 48);
        check(fog.x == 0 && fog.y == 8, "Fog never extends native fog or view distance");
        fog = SnowstormEffects.fogDistances(-8, 6, 32, 1, 8, 48);
        check(fog.x == -8 && fog.y == 6, "Stronger fog preserved");
        fog = SnowstormEffects.fogDistances(16, 64, 64, 0, 8, 48);
        check(fog.x == 16 && fog.y == 64, "Zero blend preserves vanilla");
        fog = SnowstormEffects.fogDistances(16, 64, 64, 1, 8, 48);
        check(fog.x == 8 && fog.y == 48, "Full storm targets");
        ProbeCamera camera = new ProbeCamera();
        check(!SnowstormEffects.clearCamera(camera), "Uninitialized camera guarded");
        ProbeLiving living = (ProbeLiving) unsafe.allocateInstance(ProbeLiving.class);
        camera.focused = living;
        check(SnowstormEffects.clearCamera(camera), "Clear native camera");
        for (FogType fluid : List.of(FogType.WATER, FogType.LAVA, FogType.POWDER_SNOW)) {
            camera.fluid = fluid;
            check(!SnowstormEffects.clearCamera(camera), "Fluid camera keeps native effects: " + fluid);
        }
        camera.fluid = FogType.NONE;
        living.blind = true;
        check(!SnowstormEffects.clearCamera(camera), "Blindness keeps native fog");
        living.blind = false;
        living.dark = true;
        check(!SnowstormEffects.clearCamera(camera), "Darkness keeps native fog");
        SnowstormEffects.SoundTimer timer = new SnowstormEffects.SoundTimer();
        Object firstWorld = new Object(), secondWorld = new Object();
        timer.world(firstWorld);
        check(timer.ready(80), "First world sound ready");
        check(!timer.ready(80), "Sound rate limited");
        timer.world(firstWorld);
        check(!timer.ready(80), "Same world retains cooldown");
        timer.world(secondWorld);
        check(timer.ready(80), "Replacement world discards stale cooldown");
        timer.reset();
        timer.world(secondWorld);
        check(timer.ready(80), "Disconnect/disabled reset discards cooldown");
        sampledDeposition(level, cold);
        System.out.println("Snowstorm checks passed: " + cases);
    }

    private static void sampledDeposition(Fixture level, Biome cold) {
        fence.getStateDefinition().getPossibleStates().forEach(BlockState::initCache);
        level.states = new HashMap<>();
        level.rules = new GameRules();
        level.rain = level.thunder = level.sky = level.loaded = true;
        level.top = POS.getY() + 1;
        level.biome = Holder.direct(cold);
        boolean oldAccumulation = CommonConfigs.accumulation, oldHosts = CommonConfigs.snowHosts;
        boolean oldOrdinary = CommonConfigs.withoutThunder, oldStorm = CommonConfigs.stormEnabled;
        int oldCap = CommonConfigs.snowCap, oldBonus = CommonConfigs.stormBonus;
        try {
            CommonConfigs.accumulation = CommonConfigs.snowHosts = CommonConfigs.stormEnabled = true;
            CommonConfigs.withoutThunder = false;
            for (boolean host : List.of(false, true)) {
                for (int cap : new int[]{1, 4, 8}) {
                    for (int rule : new int[]{0, 1, 3, 8}) {
                        for (int bonus : new int[]{0, 1, 3}) {
                            resetSample(level, host);
                            CommonConfigs.snowCap = cap;
                            CommonConfigs.stormBonus = bonus;
                            level.rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(rule, null);
                            int initial = host ? 0 : 1;
                            boolean vanilla = SnowWeather.sample(cold, level, POS.above());
                            int expected = rule == 0 ? initial : Math.max(initial,
                                    Math.min(SnowWeather.effectiveCap(cap, rule), initial + 1 + bonus));
                            check(!vanilla, "Existing snow/host is handled by sampled IW path");
                            check(SnowHosts.layers(level.getBlockState(POS)) == expected,
                                    "Sample host/cap/gamerule/bonus matrix");
                            check(level.writes == (expected > initial ? 1 : 0), "At most one sampled write");
                            if (host) check(SnowHosts.raw(level.getBlockState(POS))
                                    .equals(Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)),
                                    "Host properties retained");
                        }
                    }
                }
            }
            CommonConfigs.snowCap = 8;
            level.rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(1, null);
            for (int bonus : new int[]{0, 1, 3}) {
                CommonConfigs.stormBonus = bonus;
                resetSample(level, false);
                level.states.put(POS, Blocks.STONE.defaultBlockState());
                boolean vanilla = SnowWeather.sample(cold, level, POS.above());
                check(vanilla && level.writes == 0 && level.getBlockState(POS.above()).isAir(),
                        "Initial vanilla snow unchanged regardless of bonus");
                CommonConfigs.accumulation = false;
                check(SnowWeather.sample(cold, level, POS.above()) && level.writes == 0,
                        "Initial vanilla snow preserved when IW accumulation disabled");
                CommonConfigs.accumulation = true;
            }
            resetSample(level, false);
            CommonConfigs.accumulation = false;
            SnowWeather.sample(cold, level, POS.above());
            check(level.writes == 0, "Accumulation toggle remains authoritative");
            CommonConfigs.accumulation = true;
            resetSample(level, true);
            CommonConfigs.snowHosts = false;
            SnowWeather.sample(cold, level, POS.above());
            check(level.writes == 0, "Host mixing toggle remains authoritative");
            CommonConfigs.snowHosts = true;
            resetSample(level, false);
            level.thunder = false;
            SnowWeather.sample(cold, level, POS.above());
            check(level.writes == 0, "Ordinary accumulation remains disabled by default");
            CommonConfigs.withoutThunder = true;
            SnowWeather.sample(cold, level, POS.above());
            check(SnowHosts.layers(level.getBlockState(POS)) == 2, "Ordinary accumulation gains no storm bonus");
            resetSample(level, false);
            level.thunder = true;
            CommonConfigs.stormEnabled = false;
            SnowWeather.sample(cold, level, POS.above());
            check(SnowHosts.layers(level.getBlockState(POS)) == 2, "Disabled snowstorms preserve existing thunder accumulation");
            CommonConfigs.stormEnabled = true;
            resetSample(level, false);
            level.light = 12;
            SnowWeather.sample(cold, level, POS.above());
            check(level.writes == 0, "Native block light gate remains authoritative");
            resetSample(level, false);
            level.sky = false;
            SnowWeather.sample(cold, level, POS.above());
            check(SnowHosts.layers(level.getBlockState(POS)) == 2,
                    "Unexposed sample preserves baseline accumulation but gets no storm bonus");
            level.sky = true;
            resetSample(level, false);
            level.loaded = false;
            check(!SnowWeather.sample(cold, level, POS.above()) && level.writes == 0,
                    "Unloaded sample performs no writes");
            level.loaded = true;
            level.top++;
            check(!SnowWeather.sample(cold, level, POS.above()) && level.writes == 0,
                    "Non-heightmap sample performs no writes");
            level.top--;
            resetSample(level, true);
            level.states.put(POS, SnowHosts.cover(level.getBlockState(POS), 2));
            SnowWeather.sample(cold, level, POS.above());
            check(SnowHosts.layers(level.getBlockState(POS)) == 6, "Existing covered host receives bounded bonus");
            resetSample(level, false);
            level.states.put(POS, Blocks.STONE.defaultBlockState());
            level.rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(0, null);
            check(!SnowWeather.sample(cold, level, POS.above()) && level.writes == 0,
                    "Zero gamerule disables initial vanilla snow");
        } finally {
            CommonConfigs.accumulation = oldAccumulation;
            CommonConfigs.snowHosts = oldHosts;
            CommonConfigs.withoutThunder = oldOrdinary;
            CommonConfigs.stormEnabled = oldStorm;
            CommonConfigs.snowCap = oldCap;
            CommonConfigs.stormBonus = oldBonus;
        }
    }

    private static void resetSample(Fixture level, boolean host) {
        level.states.clear();
        level.states.put(POS.below(), Blocks.STONE.defaultBlockState());
        level.states.put(POS, host ? Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)
                : Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
        level.writes = level.light = 0;
    }

    private static Biome biome(float temperature, boolean rain) {
        return new Biome.BiomeBuilder().hasPrecipitation(rain).temperature(temperature).downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0)
                        .waterFogColor(0).skyColor(0).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(BiomeGenerationSettings.EMPTY).build();
    }

    private static void check(boolean condition, String message) {
        cases++;
        if (!condition) throw new AssertionError(message);
    }

    private static final class Fixture extends ServerLevel {
        private Holder<Biome> biome;
        private boolean loaded, rain, thunder, sky;
        private int top;
        private int light, writes;
        private Map<BlockPos, BlockState> states;
        private GameRules rules;

        private Fixture() {
            super(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            throw new AssertionError();
        }

        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public boolean hasChunkAt(BlockPos pos) { return loaded; }
        @Override public boolean isLoaded(BlockPos pos) { return loaded; }
        @Override public GameRules getGameRules() { return rules; }
        @Override public int getBrightness(LightLayer layer, BlockPos pos) { return layer == LightLayer.BLOCK ? light : 15; }
        @Override public BlockPos getHeightmapPos(Heightmap.Types type, BlockPos pos) { return pos.atY(top); }
        @Override public BlockState getBlockState(BlockPos pos) { return states.getOrDefault(pos, Blocks.AIR.defaultBlockState()); }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            writes++;
            states.put(pos.immutable(), state);
            return true;
        }
        @Override public List<Entity> getEntities(Entity except, net.minecraft.world.phys.AABB area,
                                                  java.util.function.Predicate<? super Entity> predicate) { return List.of(); }
        @Override public boolean isRaining() { return rain; }
        @Override public boolean isThundering() { return thunder; }
        @Override public boolean canSeeSky(BlockPos pos) { return sky; }
        @Override public int getHeight(Heightmap.Types type, int x, int z) { return top; }
        @Override public Holder<Biome> getBiome(BlockPos pos) {
            check(loaded, "Biome read never loads chunks");
            return biome;
        }
    }

    private static final class ProbeCamera extends Camera {
        private Entity focused;
        private FogType fluid = FogType.NONE;
        @Override public Entity getEntity() { return focused; }
        @Override public FogType getFluidInCamera() { return fluid; }
    }

    private static final class ProbeLiving extends LivingEntity {
        private boolean blind, dark;
        private ProbeLiving() { super(EntityType.PIG, null); throw new AssertionError(); }
        @Override public Iterable<ItemStack> getArmorSlots() { return List.of(); }
        @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
        @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}
        @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
        @Override public boolean hasEffect(net.minecraft.world.effect.MobEffect effect) {
            return blind && effect == MobEffects.BLINDNESS || dark && effect == MobEffects.DARKNESS;
        }
    }
}
