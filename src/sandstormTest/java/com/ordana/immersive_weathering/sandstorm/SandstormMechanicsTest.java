package com.ordana.immersive_weathering.sandstorm;

import com.mojang.serialization.Lifecycle;
import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import com.ordana.immersive_weathering.sand.SandHosts;
import com.ordana.immersive_weathering.sand.SandCoveredBlock;
import com.ordana.immersive_weathering.sand.SandPhysics;
import com.ordana.immersive_weathering.sand.SandWeather;
import com.ordana.immersive_weathering.entities.FallingLayerEntity;
import com.ordana.immersive_weathering.client.SandstormEffects;
import com.ordana.immersive_weathering.client.WindTimer;
import com.ordana.immersive_weathering.configs.ClientConfigs;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.SharedConstants;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.*;

/** Production rules, native Mojang states/attributes; storage/config/platform initialization are fixtures.
 * Does not claim Mixin weaving, actual renderer/sound-device or in-world acceptance coverage. */
public final class SandstormMechanicsTest {
    private static final Unsafe UNSAFE = unsafe();
    private static final BlockPos POS = new BlockPos(0, 64, 0);
    private static MappedRegistry<Biome> BIOMES;
    private static Holder<Biome> dry, wet, red, rainy;
    private static net.minecraft.world.level.storage.loot.LootDataManager loot;
    private static int cases;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Field boot = Bootstrap.class.getDeclaredField("isBootstrapped");
        boot.setAccessible(true);
        boot.setBoolean(null, true);
        BuiltInRegistries.REGISTRY.keySet();
        Registry.register(BuiltInRegistries.BLOCK, "immersive_weathering:sand_layer_block", ModBlocks.SAND);
        Registry.register(BuiltInRegistries.BLOCK, "immersive_weathering:red_sand_layer_block", ModBlocks.RED);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, "iw_storm:dust_grain",
                com.ordana.immersive_weathering.reg.ModParticles.TYPE);
        for (var pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            var item = new BlockItem(pile, new Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, BuiltInRegistries.BLOCK.getKey(pile), item);
            item.registerBlocks(Item.BY_BLOCK, item);
        }
        SandHosts.init();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, "iw_storm:falling",
                com.ordana.immersive_weathering.reg.ModEntities.FALLING_LAYER.get());
        boot.setBoolean(null, false);
        Bootstrap.bootStrap();
        BIOMES = new MappedRegistry<>(Registries.BIOME, Lifecycle.stable());
        for (var pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            pile.getStateDefinition().getPossibleStates().forEach(BlockState::initCache);
        }
        SandHosts.entries().values().forEach(covers -> covers.forEach(covered ->
                covered.get().getStateDefinition().getPossibleStates().forEach(BlockState::initCache)));
        dry = biome("dry", false);
        red = biome("red", false);
        wet = biome("wet", true);
        rainy = biome("rainy_untagged", true);
        BIOMES.freeze();
        bindBlockTags();
        loadSandLoot();
        tags(false);
        weather();
        chunkTick();
        deposition();
        sandCovers();
        sandPlacement();
        sandShovels();
        sandFalling();
        sandRain();
        sandLoot();
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

    private static void bindBlockTags() {
        List<Holder<Block>> fences = new ArrayList<>(), wood = new ArrayList<>();
        SandHosts.entries().forEach((host, covers) -> {
            if (!(host instanceof FenceBlock)) return;
            fences.add(host.builtInRegistryHolder());
            covers.forEach(covered -> fences.add(covered.get().builtInRegistryHolder()));
            if (host != Blocks.NETHER_BRICK_FENCE) {
                wood.add(host.builtInRegistryHolder());
                covers.forEach(covered -> wood.add(covered.get().builtInRegistryHolder()));
            }
        });
        BuiltInRegistries.BLOCK.bindTags(Map.of(
                net.minecraft.tags.BlockTags.SAND, List.of(Blocks.SAND.builtInRegistryHolder(), Blocks.RED_SAND.builtInRegistryHolder()),
                net.minecraft.tags.BlockTags.DEAD_BUSH_MAY_PLACE_ON, List.of(Blocks.SAND.builtInRegistryHolder(), Blocks.RED_SAND.builtInRegistryHolder()),
                net.minecraft.tags.BlockTags.FENCES, fences, net.minecraft.tags.BlockTags.WOODEN_FENCES, wood));
    }

    private static void loadSandLoot() throws Exception {
        loot = new net.minecraft.world.level.storage.loot.LootDataManager();
        Map<net.minecraft.world.level.storage.loot.LootDataId<?>, Object> tables = new HashMap<>();
        List<Block> blocks = new ArrayList<>(SandHosts.hosts());
        blocks.addAll(List.of(ModBlocks.SAND, ModBlocks.RED));
        for (Block block : blocks) {
            var id = block.getLootTable();
            try (var stream = SandstormMechanicsTest.class.getResourceAsStream(
                    "/data/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json")) {
                check(stream != null, "Missing real native/IW loot table " + id);
                var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                tables.put(new net.minecraft.world.level.storage.loot.LootDataId<>(net.minecraft.world.level.storage.loot.LootDataType.TABLE, id),
                        net.minecraft.world.level.storage.loot.LootDataType.TABLE.deserialize(id, json).orElseThrow());
            }
        }
        Field elements = net.minecraft.world.level.storage.loot.LootDataManager.class.getDeclaredField("elements");
        elements.setAccessible(true);
        elements.set(loot, Map.copyOf(tables));
    }

    private static void sandLoot() throws Exception {
        for (SandLayerBlock pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            for (Block host : SandHosts.hosts()) {
                for (int layers = 1; layers <= 8; layers++) {
                    Fixture level = world();
                    BlockState state = SandHosts.cover(host.defaultBlockState(), pile, layers);
                    var builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, Vec3.atCenterOf(POS))
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, Items.SHEARS.getDefaultInstance());
                    List<ItemStack> drops = state.getDrops(builder);
                    Map<Item, Integer> counts = new HashMap<>();
                    drops.forEach(stack -> counts.merge(stack.getItem(), stack.getCount(), Integer::sum));
                    Item sand = layers == 8 ? (pile == ModBlocks.SAND ? Items.SAND : Items.RED_SAND) : pile.asItem();
                    check(counts.equals(Map.of(host.asItem(), 1, sand, layers == 8 ? 1 : layers)), "Exact native host plus existing pile loot economy");
                }
            }
        }
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

    private static void sandCovers() throws Exception {
        check(SandHosts.hosts().size() == 13 && SandHosts.entries().size() == 13, "Only 12 fences and dead bush");
        Fixture level = world();
        level.states.put(POS.below(), Blocks.SAND.defaultBlockState());
        for (Block host : SandHosts.hosts()) {
            for (BlockState raw : host.getStateDefinition().getPossibleStates()) {
                if (!raw.getFluidState().isEmpty()) {
                    check(!SandHosts.supported(raw) && SandHosts.cover(raw, ModBlocks.SAND, 1) == null, "Waterlogged hosts are rejected");
                    continue;
                }
                for (SandLayerBlock pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
                    for (int layers = 1; layers <= 8; layers++) {
                        BlockState covered = SandHosts.cover(raw, pile, layers);
                        check(covered != null && SandHosts.raw(covered) == raw, "Native state round trip");
                        check(SandHosts.pile(covered) == pile && SandHosts.layers(covered) == layers, "Color/layer round trip");
                        BlockState sand = pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, layers);
                        check(!Shapes.joinIsNotEmpty(covered.getCollisionShape(level, POS),
                                Shapes.or(raw.getCollisionShape(level, POS), sand.getCollisionShape(level, POS)), BooleanOp.NOT_SAME),
                                "Native collision plus exact sand layer shape");
                        for (Rotation rotation : Rotation.values()) {
                            check(SandHosts.raw(covered.rotate(rotation)) == raw.rotate(rotation), "Rotation preserves all host connections");
                            check(SandHosts.layers(covered.rotate(rotation)) == layers, "Rotation retains sand");
                        }
                        for (Mirror mirror : Mirror.values()) {
                            check(SandHosts.raw(covered.mirror(mirror)) == raw.mirror(mirror), "Mirror preserves host connections");
                        }
                        SandLayerBlock other = pile == ModBlocks.SAND ? ModBlocks.RED : ModBlocks.SAND;
                        check(SandHosts.cover(covered, other, layers) == null, "Never convert or mix colors");
                        check(covered.canSurvive(level, POS) == raw.canSurvive(level, POS), "Native survival contract");
                    }
                }
            }
        }
        for (Block block : List.of(Blocks.CACTUS, Blocks.GRASS, Blocks.OAK_SAPLING, Blocks.SANDSTONE_WALL, Blocks.CHEST)) {
            check(!SandHosts.supported(block.defaultBlockState()), "Unregistered host stays excluded: " + block);
        }
    }

    private static BlockPlaceContext context(Fixture level, ItemStack stack) {
        return new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false)) {
            @Override public BlockPos getClickedPos() { return POS; }
        };
    }

    private static void sandPlacement() throws Exception {
        for (SandLayerBlock pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            for (Block host : SandHosts.hosts()) {
                Fixture level = world();
                level.states.put(POS.below(), Blocks.SAND.defaultBlockState());
                for (int layers = 1; layers <= 8; layers++) {
                    BlockState state = pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, layers);
                    level.states.put(POS, state);
                    BlockPlaceContext hostContext = context(level, host.asItem().getDefaultInstance());
                    check(state.canBeReplaced(hostContext), "Every layer allows host placement");
                    BlockState shared = SandHosts.placement(hostContext, host.defaultBlockState());
                    check(SandHosts.raw(shared).is(host) && SandHosts.layers(shared) == layers, "Host placed into pile retains all layers");
                    level.states.put(POS, layers == 1 ? host.defaultBlockState() : SandHosts.cover(host.defaultBlockState(), pile, layers - 1));
                    BlockPlaceContext sandContext = context(level, pile.asItem().getDefaultInstance());
                    check(SandHosts.canAdd(sandContext, level.getBlockState(POS)), "Pile placed into native/covered host");
                    check(SandHosts.layers(pile.getStateForPlacement(sandContext)) == layers, "Manual stack has no weather cap");
                    level.states.put(POS, SandHosts.cover(host.defaultBlockState(), pile, 8));
                    check(!SandHosts.canAdd(context(level, pile.asItem().getDefaultInstance()), level.getBlockState(POS)), "Full covered pile cannot receive ninth layer");
                }
                for (int cap = 1; cap <= 8; cap++) {
                    level.states.put(POS, host.defaultBlockState());
                    for (int layer = 1; layer <= cap; layer++) {
                        check(SandAccumulation.deposit(level, POS.above(), pile, cap),
                                "Weather deposits around " + host + " cap=" + cap + " depth=" + layer
                                        + " survives=" + host.defaultBlockState().canSurvive(level, POS));
                        check(SandHosts.layers(level.states.get(POS)) == layer, "Exact covered weather depth");
                    }
                    int writes = level.writes;
                    check(!SandAccumulation.deposit(level, POS.above(), pile, cap) && level.writes == writes, "Covered cap is no-write");
                    check(!SandAccumulation.deposit(level, POS.above(), pile, 1) && SandHosts.layers(level.states.get(POS)) == cap,
                            "Lower cap never trims a cover");
                }
            }
            for (int layers = 1; layers <= 8; layers++) {
                Fixture level = world();
                BlockState state = pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, layers);
                level.states.put(POS, state);
                for (Block block : List.of(Blocks.STONE, Blocks.OAK_PLANKS, Blocks.GLASS, Blocks.CHEST, Blocks.TORCH)) {
                    check(state.canBeReplaced(context(level, block.asItem().getDefaultInstance())), "All depths accept ordinary blocks");
                }
                check(state.canBeReplaced(context(level, pile.asItem().getDefaultInstance())) == (layers < 8), "Raw manual cap stays eight");
                BlockState fence = SandHosts.cover(Blocks.OAK_FENCE.defaultBlockState(), pile, layers);
                BlockState bush = SandHosts.cover(Blocks.DEAD_BUSH.defaultBlockState(), pile, layers);
                check(!fence.canBeReplaced(context(level, Items.STONE.getDefaultInstance())), "Do not replace covered fence with another block");
                check(bush.canBeReplaced(context(level, Items.STONE.getDefaultInstance())), "Covered bush keeps native replacement");
            }
        }
        Fixture level = world();
        level.states.put(POS, Blocks.OAK_FENCE.defaultBlockState());
        CommonConfigs.mixing = false;
        check(!SandHosts.canAdd(context(level, ModBlocks.SAND.asItem().getDefaultInstance()), level.states.get(POS)), "Mixing toggle stops manual covering");
        check(!SandAccumulation.deposit(level, POS.above(), ModBlocks.SAND, 8), "Mixing toggle stops weather covering");
        CommonConfigs.mixing = true;
        level.states.put(POS, ModBlocks.SAND.defaultBlockState().setValue(LayerBlock.LAYERS_8, 8));
        CommonConfigs.replacement = false;
        check(!level.states.get(POS).canBeReplaced(context(level, Items.STONE.getDefaultInstance())), "Replacement toggle restores deep-pile protection");
        CommonConfigs.replacement = true;
    }

    private static void sandShovels() throws Exception {
        for (SandLayerBlock pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            for (Block host : SandHosts.hosts()) {
                for (boolean creative : List.of(false, true)) {
                    Fixture level = world();
                    ProbePlayer player = new ProbePlayer(level, creative);
                    BlockState state = SandHosts.cover(host.defaultBlockState(), pile, 4);
                    level.states.put(POS, state);
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false);
                    level.failWrites = true;
                    check(state.use(level, player, InteractionHand.MAIN_HAND, hit) == net.minecraft.world.InteractionResult.FAIL,
                            "Rejected shovel write reports failure");
                    check(level.states.get(POS) == state && level.entities.isEmpty() && player.shovel.getDamageValue() == 0,
                            "Rejected shovel write retains cover without drops or durability cost");
                    level.failWrites = false;
                    check(state.use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction(), "Shovel strips cover");
                    check(level.states.get(POS) == host.defaultBlockState(), "Shovel restores exact native host");
                    check(player.shovel.getDamageValue() == (creative ? 0 : 1), "Only Survival consumes shovel durability");
                    int count = level.entities.stream().filter(entity -> entity instanceof net.minecraft.world.entity.item.ItemEntity)
                            .mapToInt(entity -> ((net.minecraft.world.entity.item.ItemEntity) entity).getItem().getCount()).sum();
                    check(count == (creative ? 0 : 4), "Shovel drops only the removed sand in Survival");
                }
            }
        }
    }

    private static void sandFalling() throws Exception {
        for (SandLayerBlock pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            for (int existing = 0; existing <= 8; existing++) {
                for (int incoming = 1; incoming <= 8; incoming++) {
                    for (Block host : SandHosts.hosts()) {
                        Fixture level = world();
                        level.states.put(POS.below(), Blocks.SAND.defaultBlockState());
                        level.states.put(POS, existing == 0 ? host.defaultBlockState() : SandHosts.cover(host.defaultBlockState(), pile, existing));
                        int remainder = SandPhysics.deposit(level, POS, pile, incoming);
                        int expected = Math.min(8, existing + incoming);
                        check(SandHosts.layers(level.states.get(POS)) == expected, "Falling sand fills host to eight");
                        check(remainder == existing + incoming - expected, "Exact overflow retained");
                        check(SandHosts.raw(level.states.get(POS)).is(host), "Falling sand never moves host");
                    }
                }
            }
            Fixture level = world();
            BlockState fence = SandHosts.cover(Blocks.OAK_FENCE.defaultBlockState(), pile, 6);
            level.states.put(POS, fence);
            level.states.remove(POS.below());
            SandPhysics.checkSupport(level, POS, fence);
            check(level.entities.size() == 1 && level.states.get(POS).is(Blocks.OAK_FENCE), "Unsupported sand falls without moving fence");
            FallingLayerEntity falling = (FallingLayerEntity) level.entities.get(0);
            check(SandHosts.layers(falling.getBlockState()) == 6 && falling.getBlockState().is(pile), "Detached exact color/layers");
            var saved = new net.minecraft.nbt.CompoundTag();
            falling.saveWithoutId(saved);
            FallingLayerEntity restored = new FallingLayerEntity(level);
            restored.load(saved);
            check(restored.getBlockState() == falling.getBlockState(), "Falling color/layer save round trip");
            level = world();
            level.states.put(POS, Blocks.WATER.defaultBlockState());
            check(SandPhysics.deposit(level, POS, pile, 8) == 8 && level.states.get(POS).is(Blocks.WATER), "Sand does not freeze or replace water");
            level = world();
            level.failWrites = true;
            check(SandPhysics.deposit(level, POS, pile, 4) == 4 && !level.states.containsKey(POS), "Rejected write retains all falling layers");
            level = world();
            level.states.put(POS, (pile == ModBlocks.SAND ? ModBlocks.RED : ModBlocks.SAND).defaultBlockState());
            check(SandPhysics.deposit(level, POS, pile, 3) == 3, "Falling sand cannot mix colors");
            level = world();
            level.states.put(POS, Blocks.OAK_FENCE.defaultBlockState());
            ProbeFalling landing = new ProbeFalling(level, pile, 5);
            landing.ground = true;
            landing.tick();
            check(landing.time == 1 && landing.isRemoved() && SandHosts.layers(level.states.get(POS)) == 5,
                    "Actual falling-entity dispatch lands sand into host");
            level = world();
            ProbeFalling timeout = new ProbeFalling(level, pile, 3);
            timeout.time = 600;
            timeout.tick();
            check(timeout.time == 601 && timeout.isRemoved()
                    && level.entities.stream().anyMatch(entity -> entity instanceof net.minecraft.world.entity.item.ItemEntity item
                    && item.getItem().is(pile.asItem()) && item.getItem().getCount() == 3), "Falling timeout preserves all layers as items");
            level = world();
            level.rules.getRule(GameRules.RULE_DOENTITYDROPS).set(false, null);
            timeout = new ProbeFalling(level, pile, 3);
            timeout.time = 600;
            timeout.tick();
            check(timeout.isRemoved() && level.entities.isEmpty(), "Falling drops respect doEntityDrops");
        }
    }

    private static void sandRain() throws Exception {
        var rolls = new net.minecraft.world.level.levelgen.LegacyRandomSource(1) {
            @Override public double nextDouble() { return 0; }
        };
        for (SandLayerBlock pile : List.of(ModBlocks.SAND, ModBlocks.RED)) {
            for (Block host : SandHosts.hosts()) {
                Fixture level = world();
                level.biome = rainy;
                for (int layers = 8; layers >= 1; layers--) {
                    BlockState state = SandHosts.cover(host.defaultBlockState(), pile, layers);
                    level.states.put(POS, state);
                    level.states.get(POS).randomTick(level, POS, rolls);
                    check(SandHosts.layers(level.states.get(POS)) == layers - 1, "Production random tick removes exactly one layer");
                    check(SandHosts.raw(level.states.get(POS)).is(host), "Last erosion layer restores native host");
                }
            }
            Fixture level = world();
            BlockState state = pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, 8);
            level.biome = rainy;
            level.states.put(POS, state);
            pile.randomTick(state, level, POS, rolls);
            check(SandHosts.layers(level.states.get(POS)) == 7, "Uncovered sand random ticks erode");
            state = pile.defaultBlockState();
            level.states.put(POS, state);
            check(SandWeather.erode(level, POS, state, rolls) && level.states.get(POS).isAir(), "Last bare layer becomes air without drops");
            for (Holder<Biome> immune : List.of(dry, red, wet)) {
                level.biome = immune;
                level.rainAt = true;
                check(!SandWeather.erode(level, POS, state, rolls), "Either storm tag protects even when precipitation is forced");
            }
            level.biome = rainy;
            BIOMES.bindTags(Map.of(Sandstorms.REGULAR, List.of(), Sandstorms.RED, List.of()));
            level.sky = false;
            check(!SandWeather.erode(level, POS, state, rolls), "Roof blocks erosion");
            level.sky = true;
            level.rain = false;
            check(!SandWeather.erode(level, POS, state, rolls), "Clear weather preserves piles");
            level.rain = true;
            level.rainAt = false;
            check(!SandWeather.erode(level, POS, state, rolls), "Snowfall/dry precipitation cannot erode");
            level.rainAt = true;
            CommonConfigs.erosion = false;
            check(!SandWeather.erode(level, POS, state, rolls), "Live erosion toggle");
            CommonConfigs.erosion = true;
            CommonConfigs.erosionChance = 0;
            check(!SandWeather.erode(level, POS, state, rolls), "Zero erosion chance");
            CommonConfigs.erosionChance = 0.125;
            var boundary = new net.minecraft.world.level.levelgen.LegacyRandomSource(1) {
                @Override public double nextDouble() { return 0.125; }
            };
            check(!SandWeather.erode(level, POS, state, boundary), "Exact chance boundary is rejected");
            tags(false);
        }
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
        level.rainAt = true;
        level.entities = new ArrayList<>();
        level.rules = new GameRules();
        level.server = (FixtureServer) UNSAFE.allocateInstance(FixtureServer.class);
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
        private boolean rain, sky, loaded, rainAt, failWrites;
        private List<Entity> entities;
        private GameRules rules;
        private FixtureServer server;
        private int top, writes;
        private int samples, roll;
        private Fixture() { super(null, null, null, null, null, null, null, false, 0, List.of(), false, null); throw new AssertionError(); }
        @Override public BlockState getBlockState(BlockPos pos) {
            check(loaded && !isOutsideBuildHeight(pos), "No invalid storage read");
            return states.getOrDefault(pos, Blocks.AIR.defaultBlockState());
        }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            if (failWrites) return false;
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
        @Override public boolean isRainingAt(BlockPos pos) { return rain && sky && rainAt; }
        @Override public boolean addFreshEntity(Entity entity) { entities.add(entity); return true; }
        @Override public GameRules getGameRules() { return rules; }
        @Override public net.minecraft.server.MinecraftServer getServer() { return server; }
        @Override public RandomSource getRandomSequence(ResourceLocation id) { return random; }
        @Override public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() { return net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS; }
        @Override public void scheduleTick(BlockPos pos, Block block, int delay) {}
        @Override public void levelEvent(net.minecraft.world.entity.player.Player player, int event, BlockPos pos, int data) {}
        @Override public void gameEvent(net.minecraft.world.level.gameevent.GameEvent event, Vec3 pos,
                                        net.minecraft.world.level.gameevent.GameEvent.Context context) {}
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

    private static final class FixtureServer extends net.minecraft.server.dedicated.DedicatedServer {
        private FixtureServer() { super(null, null, null, null, null, null, null, null); throw new AssertionError(); }
        @Override public net.minecraft.world.level.storage.loot.LootDataManager getLootData() { return loot; }
    }

    private static final class ProbePlayer extends net.minecraft.world.entity.player.Player {
        private final ItemStack shovel = new ItemStack(Items.IRON_SHOVEL);
        private final boolean creative;
        private ProbePlayer(Level level, boolean creative) {
            super(level, POS, 0, new com.mojang.authlib.GameProfile(new java.util.UUID(0, 1), "sand-test"));
            this.creative = creative;
        }
        @Override public boolean isCreative() { return creative; }
        @Override public boolean isSpectator() { return false; }
        @Override public ItemStack getItemInHand(InteractionHand hand) { return shovel; }
    }

    private static final class ProbeFalling extends FallingLayerEntity {
        private boolean ground;
        private ProbeFalling(Fixture level, SandLayerBlock pile, int layers) {
            super(level, POS, pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, layers));
        }
        @Override public void move(MoverType type, Vec3 delta) {
            if (!ground) setPos(getX() + delta.x, getY() + delta.y, getZ() + delta.z);
        }
        @Override public boolean onGround() { return ground; }
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
