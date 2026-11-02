package com.ordana.immersive_weathering.sandstorm;

import com.mojang.serialization.Lifecycle;
import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.client.SandstormEffects;
import com.ordana.immersive_weathering.client.WindTimer;
import com.ordana.immersive_weathering.configs.ClientConfigs;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.*;

/** Production rules, native Mojang states/attributes; storage/config/platform initialization are fixtures.
 * Does not claim Mixin weaving, actual renderer/sound-device or in-world acceptance coverage. */
public final class SandstormMechanicsTest {
    private static final Unsafe UNSAFE = unsafe();
    private static final BlockPos POS = new BlockPos(0, 64, 0);
    private static MappedRegistry<Biome> BIOMES;
    private static Holder<Biome> dry, wet, red;
    private static int cases;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Field boot = Bootstrap.class.getDeclaredField("isBootstrapped");
        boot.setAccessible(true);
        boot.setBoolean(null, true);
        BuiltInRegistries.REGISTRY.keySet();
        Registry.register(BuiltInRegistries.BLOCK, "iw_storm:sand", ModBlocks.SAND);
        Registry.register(BuiltInRegistries.BLOCK, "iw_storm:red", ModBlocks.RED);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "iw_storm:dust_grain",
                com.ordana.immersive_weathering.reg.ModParticles.TYPE);
        boot.setBoolean(null, false);
        Bootstrap.bootStrap();
        BIOMES = new MappedRegistry<>(Registries.BIOME, Lifecycle.stable());
        for (var pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            pile.getStateDefinition().getPossibleStates().forEach(BlockState::initCache);
        }
        dry = biome("dry", false);
        red = biome("red", false);
        wet = biome("wet", true);
        BIOMES.freeze();
        tags(false);
        weather();
        chunkTick();
        deposition();
        slowing();
        client();
        System.out.println("Sandstorm mechanics: " + cases + " production/native assertions passed");
    }

    private static Holder<Biome> biome(String id, boolean rain) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation("iw_storm", id));
        BIOMES.register(key, new Biome.BiomeBuilder().hasPrecipitation(rain).temperature(2).downfall(0)
                .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build())
                .generationSettings(BiomeGenerationSettings.EMPTY).mobSpawnSettings(MobSpawnSettings.EMPTY).build(), Lifecycle.stable());
        return BIOMES.getHolderOrThrow(key);
    }

    private static void tags(boolean overlap) {
        BIOMES.bindTags(Map.of(Sandstorms.REGULAR, overlap ? List.of(dry, wet, red) : List.of(dry, wet),
                Sandstorms.RED, List.of(red)));
    }

    private static void weather() throws Exception {
        Fixture level = world();
        check(Sandstorms.biomeType(dry) == Sandstorms.Type.REGULAR, "Dry regular tag");
        check(Sandstorms.biomeType(wet) == Sandstorms.Type.NONE, "Wet tag rejected");
        check(Sandstorms.biomeType(red) == Sandstorms.Type.RED, "Dry red tag");
        tags(true);
        check(Sandstorms.biomeType(red) == Sandstorms.Type.REGULAR, "Regular wins overlap");
        tags(false);
        check(Sandstorms.exposedStorm(level, POS) == Sandstorms.Type.REGULAR, "Rain, not thunder");
        level.rain = false;
        check(!Sandstorms.visualStorm(level, POS), "Clear weather");
        level.rain = true;
        level.sky = false;
        check(Sandstorms.exposedStorm(level, POS) == Sandstorms.Type.NONE, "Roof blocks gameplay");
        check(Sandstorms.visualStorm(level, POS), "Indoor biome visuals intentionally remain");
        level.sky = true;
        level.top = 65;
        check(Sandstorms.exposedStorm(level, POS) == Sandstorms.Type.NONE, "Heightmap exposure");
        level.top = 64;
        level.biome = wet;
        check(Sandstorms.exposedStorm(level, POS) == Sandstorms.Type.NONE, "Wet storm tag");
        BIOMES.bindTags(Map.of(Sandstorms.REGULAR, List.of(), Sandstorms.RED, List.of()));
        level.biome = dry;
        check(!Sandstorms.visualStorm(level, POS), "Tag reload read live");
        tags(false);
    }

    private static void deposition() throws Exception {
        for (var pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            for (int cap = 1; cap <= 8; cap++) {
                Fixture level = world();
                for (int i = 0; i < cap; i++) {
                    check(SandAccumulation.deposit(level, i == 0 ? POS : POS.above(), pile, cap), "Deposit one layer");
                    check(pile.getLayers(level.states.get(POS)) == i + 1, "Exact layer total");
                }

                int writes = level.writes;
                check(!SandAccumulation.deposit(level, POS.above(), pile, cap), "At cap no write");
                check(level.writes == writes && pile.getLayers(level.states.get(POS)) == cap, "Never reset cap to one");
                check(!SandAccumulation.deposit(level, POS.above(), pile, 1), "Lower cap preserves remainder");
                check(pile.getLayers(level.states.get(POS)) == cap, "No layer loss");
            }
        }
        for (Block block : List.of(Blocks.WATER, Blocks.LAVA, Blocks.GRASS, Blocks.CHEST, Blocks.STONE,
                Blocks.CAULDRON, Blocks.WATER_CAULDRON, Blocks.POWDER_SNOW_CAULDRON)) {
            Fixture level = world();
            level.states.put(POS, block.defaultBlockState());
            check(!SandAccumulation.deposit(level, POS, ModBlocks.SAND, 8), "Never replace " + block);
            check(level.writes == 0, "No write/precipitation on occupied target");
        }
        for (Block block : List.of(Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.OAK_SLAB,
                Blocks.CAULDRON, Blocks.OAK_LEAVES)) {
            Fixture level = world();
            level.states.put(POS.below(), block.defaultBlockState());
            check(!SandAccumulation.deposit(level, POS, ModBlocks.SAND, 8), "Invalid support " + block);
        }
        for (int layers = 1; layers <= 8; layers++) {
            Fixture level = world();
            level.states.put(POS, ModBlocks.RED.defaultBlockState().setValue(LayerBlock.LAYERS_8, layers));
            check(!SandAccumulation.deposit(level, POS.above(), ModBlocks.SAND, 8), "Different pile protected");
        }
        Fixture level = world();
        level.loaded = false;
        check(!SandAccumulation.deposit(level, POS, ModBlocks.SAND, 8), "No forced load/read");
        level.loaded = true;
        for (int y : new int[]{-65, -64, 320, 321}) {
            check(!SandAccumulation.deposit(level, POS.atY(y), ModBlocks.SAND, 8), "Height bound " + y);
        }
        level.border.setSize(1);
        check(!SandAccumulation.deposit(level, POS.offset(10, 0, 0), ModBlocks.SAND, 8), "World border");
        check(ModBlocks.SAND.shouldFall(ModBlocks.SAND.defaultBlockState(), Blocks.AIR.defaultBlockState()), "Existing falling rule");
        check(!ModBlocks.SAND.shouldFall(ModBlocks.SAND.defaultBlockState(), Blocks.STONE.defaultBlockState()), "Existing stable rule");
        check(ModBlocks.SAND.getMaxLayers() == 8, "No new pile economy");
    }

    private static void chunkTick() throws Exception {
        var chunk = (net.minecraft.world.level.chunk.LevelChunk) UNSAFE.allocateInstance(net.minecraft.world.level.chunk.LevelChunk.class);
        Field field = net.minecraft.world.level.chunk.ChunkAccess.class.getDeclaredField("chunkPos");
        UNSAFE.putObject(chunk, UNSAFE.objectFieldOffset(field), new net.minecraft.world.level.ChunkPos(0, 0));
        Fixture level = world();
        Field random = Level.class.getDeclaredField("random");
        UNSAFE.putObject(level, UNSAFE.objectFieldOffset(random), new net.minecraft.world.level.levelgen.LegacyRandomSource(1) {
            @Override public int nextInt(int bound) { check(bound == 16, "Exactly 1/16 deposition roll"); return level.roll; }
        });
        SandAccumulation.tickChunk(level, chunk);
        check(level.samples == 1 && level.writes == 1 && level.states.get(POS).is(ModBlocks.SAND), "One column/one regular deposit");
        level.states.remove(POS);
        level.biome = red;
        SandAccumulation.tickChunk(level, chunk);
        check(level.samples == 2 && level.writes == 2 && level.states.get(POS).is(ModBlocks.RED), "Red subtype actual chunk hook");
        level.roll = 1;
        SandAccumulation.tickChunk(level, chunk);
        check(level.writes == 2, "Unsuccessful roll no write");
        CommonConfigs.deposition = false;
        int samples = level.samples;
        SandAccumulation.tickChunk(level, chunk);
        check(level.samples == samples, "Disabled deposition no work");
        CommonConfigs.deposition = true;
        level.rain = false;
        SandAccumulation.tickChunk(level, chunk);
        check(level.samples == samples, "Clear weather no work");
    }

    private static void slowing() throws Exception {
        Fixture level = world();
        ProbeLiving living = new ProbeLiving(level);
        living.setPos(0, 64, 0);
        AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
        speed.setBaseValue(1);
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(Sandstorms.IMMUNE, List.of()));
        SandstormSlowing.tick(living);
        near(speed.getValue(), 0.7, "30 percent multiply total");
        for (int i = 0; i < 10; i++) SandstormSlowing.tick(living);
        check(speed.getModifiers().size() == 1, "No modifier stacking");
        CommonConfigs.slowAmount = 0.5;
        SandstormSlowing.tick(living);
        near(speed.getValue(), 0.5, "Live strength replacement");
        level.sky = false;
        SandstormSlowing.tick(living);
        near(speed.getValue(), 1, "Shelter removes");
        level.sky = true;
        SandstormSlowing.tick(living);
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(Sandstorms.IMMUNE, List.of(EntityType.PIG.builtInRegistryHolder())));
        SandstormSlowing.tick(living);
        check(speed.getModifier(SandstormSlowing.ID) == null, "New immunity cleans old modifier");
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(Sandstorms.IMMUNE, List.of()));
        SandstormSlowing.tick(living);
        CommonConfigs.slowing = false;
        SandstormSlowing.tick(living);
        check(speed.getModifier(SandstormSlowing.ID) == null, "Config disable cleans old modifier");
        CommonConfigs.slowing = true;
        SandstormSlowing.tick(living);
        level.rain = false;
        SandstormSlowing.tick(living);
        check(speed.getModifier(SandstormSlowing.ID) == null, "Weather cleanup");
        level.rain = true;
        SandstormSlowing.tick(living);
        level.biome = wet;
        SandstormSlowing.tick(living);
        check(speed.getModifier(SandstormSlowing.ID) == null, "Dimension/biome cleanup");
        AttributeModifier other = new AttributeModifier(UUID.randomUUID(), "other mod", 0.1, AttributeModifier.Operation.ADDITION);
        speed.addTransientModifier(other);
        SandstormSlowing.reconcile(speed, true, 0.3);
        SandstormSlowing.reconcile(speed, false, 0.3);
        check(speed.hasModifier(other), "Never removes another modifier");
        check(speed.save().getList("Modifiers", 10).isEmpty(), "Temporary modifiers not persisted");
        speed.removeModifier(other);
        SandstormSlowing.reconcile(speed, true, 0);
        check(speed.getModifier(SandstormSlowing.ID) == null, "Zero strength no modifier");
        CommonConfigs.slowAmount = 0.3;
        speed.addTransientModifier(new AttributeModifier(SandstormSlowing.ID, "stale wrong operation",
                -0.3, AttributeModifier.Operation.ADDITION));
        SandstormSlowing.reconcile(speed, true, 0.3);
        check(speed.getModifier(SandstormSlowing.ID).getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL,
                "Repair stale wrong-operation modifier");
        SandstormSlowing.reconcile(speed, false, 0.3);
        BuiltInRegistries.ENTITY_TYPE.bindTags(Map.of(Sandstorms.IMMUNE, List.of(EntityType.CAMEL.builtInRegistryHolder())));
        ProbeLiving camel = new ProbeLiving(level, EntityType.CAMEL);
        camel.setPos(0, 64, 0);
        level.biome = dry;
        SandstormSlowing.tick(camel);
        check(camel.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(SandstormSlowing.ID) == null, "Camel immunity");
    }

    private static void client() throws Exception {
        ClientFixture level = (ClientFixture) UNSAFE.allocateInstance(ClientFixture.class);
        level.biome = dry;
        level.rain = 1;
        level.loaded = true;
        random(level);
        ProbeCamera camera = new ProbeCamera();
        near(SandstormEffects.stormBlend(level, camera, 1), 1, "Full storm blend");
        level.rain = 0.5F;
        near(SandstormEffects.stormBlend(level, camera, 1), 0.5, "Weather gradient");
        level.rain = 1;
        level.boundary = true;
        double boundary = SandstormEffects.stormBlend(level, camera, 1);
        check(boundary > 0 && boundary < 1, "Cubic biome transition");
        level.boundary = false;
        ClientConfigs.fog = false;
        near(SandstormEffects.stormBlend(level, camera, 1), 0, "Fog toggle");
        check(SandstormEffects.hideClouds(level, POS, 1), "Clouds independent of fog");
        ClientConfigs.clouds = false;
        check(!SandstormEffects.hideClouds(level, POS, 1), "Cloud toggle");
        ClientConfigs.clouds = true;
        level.rain = 0.75F;
        check(!SandstormEffects.hideClouds(level, POS, 1), "Strict cloud threshold");
        ClientConfigs.fog = true;
        level.rain = 1;
        for (FogType fluid : List.of(FogType.WATER, FogType.LAVA, FogType.POWDER_SNOW)) {
            camera.fluid = fluid;
            near(SandstormEffects.stormBlend(level, camera, 1), 0, "Fluid fog preserved");
            SandstormEffects.fogDistance(camera, net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN, 256, false, 1);
            check(true, "Fluid guard returns without renderer access");
        }
        camera.fluid = FogType.NONE;
        SandstormEffects.fogDistance(camera, net.minecraft.client.renderer.FogRenderer.FogMode.FOG_SKY, 256, false, 1);
        check(true, "Sky fog unchanged without renderer access");
        SandstormEffects.fogDistance(camera, net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN, 256, true, 1);
        check(true, "Thick fog unchanged without renderer access");
        ProbeLiving focused = new ProbeLiving(world());
        camera.focused = focused;
        focused.blind = true;
        check(!SandstormEffects.clearCamera(camera), "Blindness guard");
        SandstormEffects.fogDistance(camera, net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN, 256, false, 1);
        focused.blind = false;
        focused.dark = true;
        check(!SandstormEffects.clearCamera(camera), "Darkness guard");
        SandstormEffects.fogDistance(camera, net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN, 256, false, 1);
        focused.dark = false;
        check(SandstormEffects.clearCamera(camera), "Healthy camera");
        Vec3 distances = SandstormEffects.fogDistances(192, 256, 256, 1);
        near(distances.x, 16, "Fog start");
        near(distances.y, 64, "Fog end");
        distances = SandstormEffects.fogDistances(-8, 12, 12, 1);
        near(distances.x, -8, "Stronger fog start unchanged");
        near(distances.y, 12, "Stronger fog end unchanged");
        Vec3 vanilla = new Vec3(0.2, 0.3, 0.4);
        Vec3 noon = SandstormEffects.fogColor(level, camera, 1, vanilla);
        level.day = 0.5F;
        Vec3 night = SandstormEffects.fogColor(level, camera, 1, vanilla);
        check(noon.lengthSqr() > night.lengthSqr(), "Time-of-day fog lighting");
        ClientConfigs.fog = false;
        check(SandstormEffects.fogColor(level, camera, 1, vanilla).equals(vanilla), "Disabled color unchanged");
        ClientConfigs.fog = true;
        ClientConfigs.start = 100;
        ClientConfigs.end = 20;
        distances = SandstormEffects.fogDistances(192, 256, 256, 1);
        check(distances.x <= distances.y, "Inverted config safe");
        ClientConfigs.start = 16;
        ClientConfigs.end = 64;
        Method particles = SandstormEffects.class.getDeclaredMethod("particles", ClientLevel.class, BlockPos.class, ParticleStatus.class);
        particles.setAccessible(true);
        ClientConfigs.rarity = 1;
        particles.invoke(null, level, POS, ParticleStatus.ALL);
        check(level.particles == 1600 && level.columns == 1600, "40x40 default bounded scan");
        level.particles = level.columns = 0;
        particles.invoke(null, level, POS, ParticleStatus.MINIMAL);
        check(level.particles == 0 && level.columns == 0, "Minimal no work");
        particles.invoke(null, level, POS, ParticleStatus.DECREASED);
        check(level.particles > 0 && level.particles < 1600, "Decreased halves trial probability");
        level.particles = level.columns = 0;
        level.loaded = false;
        particles.invoke(null, level, POS, ParticleStatus.ALL);
        check(level.particles == 0 && level.columns == 0, "No un-loaded queries");
        ClientConfigs.range = 0;
        level.loaded = true;
        particles.invoke(null, level, POS, ParticleStatus.ALL);
        check(level.columns == 0, "Range zero");
        particleFactory(level);
        WindTimer timer = new WindTimer();
        Object first = new Object(), second = new Object();
        check(!timer.tick(first, true, 0) && timer.tick(first, true, 0), "Intermittent wind");
        check(!timer.tick(first, true, 0), "Restart interval");
        check(!timer.tick(second, true, 0), "World transition resets timer");
        check(!timer.tick(second, false, 0), "Clear/disable resets timer");
        check(!timer.tick(second, true, 0), "No immediate stale playback");
        timer.reset();
        check(!timer.tick(second, true, 0), "Disconnect reset");
        ClientConfigs.range = 20;
        ClientConfigs.rarity = 60;
    }

    private static void particleFactory(ClientFixture level) throws Exception {
        Class<?> particleClass = com.ordana.immersive_weathering.client.particles.DustGrainParticle.class;
        Field optionsField = particleClass.getDeclaredField("OPTIONS");
        optionsField.setAccessible(true);
        var options = (net.minecraft.core.particles.DustParticleOptions) optionsField.get(null);
        near(options.getScale(), 4, "Actual production options scale clamp");
        near(options.getColor().x, 217 / 255F, "Production dust red");
        near(options.getColor().y, 170 / 255F, "Production dust green");
        near(options.getColor().z, 132 / 255F, "Production dust blue");
        var sprite = (net.minecraft.client.renderer.texture.TextureAtlasSprite)
                UNSAFE.allocateInstance(net.minecraft.client.renderer.texture.TextureAtlasSprite.class);
        net.minecraft.client.particle.SpriteSet sprites = new net.minecraft.client.particle.SpriteSet() {
            @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite get(int age, int life) { return sprite; }
            @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite get(net.minecraft.util.RandomSource random) { return sprite; }
        };
        var type = com.ordana.immersive_weathering.reg.ModParticles.TYPE;
        var particle = new com.ordana.immersive_weathering.client.particles.DustGrainParticle.Provider(sprites)
                .createParticle(type, level, 0, 64, 0, -1, 0, 0);
        for (var entry : Map.of("xd", -1d, "yd", 0d, "zd", 0d, "gravity", 0.1d).entrySet()) {
            Field field = net.minecraft.client.particle.Particle.class.getDeclaredField(entry.getKey());
            field.setAccessible(true);
            near(((Number) field.get(particle)).doubleValue(), entry.getValue(), "Native factory " + entry.getKey());
        }
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            type.writeToNetwork(buffer);
            check(buffer.readableBytes() == 0, "Simple serialized type needs no custom payload");
            check(type.getDeserializer().fromNetwork(type, buffer) == type, "Native particle deserialization");
        } finally {
            buffer.release();
        }
    }

    private static Fixture world() throws Exception {
        Fixture level = (Fixture) UNSAFE.allocateInstance(Fixture.class);
        level.states = new HashMap<>();
        level.states.put(POS.below(), Blocks.STONE.defaultBlockState());
        level.border = new WorldBorder();
        level.biome = dry;
        level.rain = level.sky = level.loaded = true;
        level.top = 64;
        random(level);
        return level;
    }

    private static void random(Level level) throws Exception {
        Field field = Level.class.getDeclaredField("random");
        UNSAFE.putObject(level, UNSAFE.objectFieldOffset(field), new net.minecraft.world.level.levelgen.LegacyRandomSource(1));
    }

    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
    }

    private static void check(boolean value, String message) { cases++; if (!value) throw new AssertionError(message); }
    private static void near(double value, double expected, String message) { check(Math.abs(value - expected) < 1e-6, message + ": " + value); }

    private static final class Fixture extends ServerLevel {
        private Map<BlockPos, BlockState> states;
        private WorldBorder border;
        private Holder<Biome> biome;
        private boolean rain, sky, loaded;
        private int top, writes;
        private int samples, roll;
        private Fixture() { super(null, null, null, null, null, null, null, false, 0, List.of(), false, null); throw new AssertionError(); }
        @Override public BlockState getBlockState(BlockPos pos) {
            check(loaded && !isOutsideBuildHeight(pos), "No invalid storage read");
            return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            writes++;
            states.put(pos.immutable(), state);
            return true;
        }
        @Override public List<Entity> getEntities(Entity except, AABB area, java.util.function.Predicate<? super Entity> predicate) { return List.of(); }
        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public boolean hasChunkAt(BlockPos pos) { return loaded; }
        @Override public boolean isRaining() { return rain; }
        @Override public boolean canSeeSky(BlockPos pos) { return sky; }
        @Override public int getHeight(Heightmap.Types type, int x, int z) { return top; }
        @Override public BlockPos getBlockRandomPos(int x, int y, int z, int mask) {
            check(mask == 15 && x == 0 && z == 0, "Sample bounded to ticking chunk");
            samples++;
            return POS;
        }
        @Override public WorldBorder getWorldBorder() { return border; }
        @Override public Holder<Biome> getBiome(BlockPos pos) { return biome; }
        @Override public RegistryAccess registryAccess() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
    }

    private static final class ProbeLiving extends LivingEntity {
        private boolean blind, dark;
        private ProbeLiving(Level level) { this(level, EntityType.PIG); }
        private ProbeLiving(Level level, EntityType<? extends LivingEntity> type) { super(type, level); }
        @Override public Iterable<ItemStack> getArmorSlots() { return List.of(); }
        @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
        @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}
        @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
        @Override public boolean hasEffect(net.minecraft.world.effect.MobEffect effect) {
            return (blind && effect == MobEffects.BLINDNESS) || (dark && effect == MobEffects.DARKNESS);
        }
    }

    private static final class ProbeCamera extends Camera {
        private FogType fluid = FogType.NONE;
        private Entity focused;
        @Override public FogType getFluidInCamera() { return fluid; }
        @Override public Entity getEntity() { return focused; }
        @Override public Vec3 getPosition() { return new Vec3(0, 64, 0); }
    }

    private static final class ClientFixture extends ClientLevel {
        private Holder<Biome> biome;
        private float rain;
        private float day;
        private boolean boundary, loaded = true;
        private int columns, particles;
        private ClientFixture() { super(null, null, null, null, 0, 0, null, null, false, 0); throw new AssertionError(); }
        @Override public float getRainLevel(float partialTick) { return rain; }
        @Override public float getTimeOfDay(float partialTick) { return day; }
        @Override public net.minecraft.client.renderer.DimensionSpecialEffects effects() {
            return new net.minecraft.client.renderer.DimensionSpecialEffects.OverworldEffects();
        }
        @Override public Holder<Biome> getBiome(BlockPos pos) { return boundary && pos.getX() < 0 ? wet : biome; }
        @Override public boolean hasChunkAt(BlockPos pos) { return loaded; }
        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public int getHeight(Heightmap.Types type, int x, int z) { columns++; return 64; }
        @Override public void addParticle(net.minecraft.core.particles.ParticleOptions type, double x, double y, double z, double dx, double dy, double dz) {
            check(dx == -1 && dy == 0 && dz == 0 && y >= 64, "Particle placement and velocity");
            particles++;
        }
        @Override public boolean isRaining() { return rain > 0; }
    }
}
