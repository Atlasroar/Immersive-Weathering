package com.ordana.immersive_weathering.snow;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.entities.FallingLayerEntity;
import com.ordana.immersive_weathering.mixins.SnowDoublePlantMixin;
import com.ordana.immersive_weathering.mixins.SnowHostPlacementMixin;
import com.ordana.immersive_weathering.mixins.SnowLayerBlockMixin;
import com.ordana.immersive_weathering.mixins.SnowPlantLootPredicateMixin;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.BooleanOp;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/** No Fabric/chunk-engine emulation: execute real transitions against explicit storage fixtures. */
public final class SnowMechanicsTest {
    private static final BlockPos POS = new BlockPos(0, 64, 0);
    private static final Unsafe UNSAFE = unsafe();
    private static int cases;
    private static ProbeSnow probe;
    private static ProbeSapling saplingProbe;
    private static ProbePlant plantProbe;
    private static net.minecraft.world.level.storage.loot.LootDataManager loot;

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Field bootstrapped = Bootstrap.class.getDeclaredField("isBootstrapped");
        bootstrapped.setAccessible(true);
        bootstrapped.setBoolean(null, true);
        BuiltInRegistries.REGISTRY.keySet();
        SnowHosts.init();
        check(SnowHosts.entries().size() == 68, "Common initialization resolved deferred IW plants");
        com.ordana.immersive_weathering.reg.ModBlocks.resolvePlants();
        List<Block> immersive = SnowHosts.immersiveHosts();
        var weedsFire = net.mehvahdjukaar.moonlight.api.platform.RegHelper
                .registeredFire(com.ordana.immersive_weathering.reg.ModBlocks.WEEDS.get());
        check(weedsFire.spread() == 60 && weedsFire.flammability() == 100, "Weed fire registration changed");
        List<String> immersiveNames = List.of("weeds", "dune_grass", "frosty_grass", "frosty_fern");
        for (int i = 0; i < immersive.size(); i++) {
            Block host = Registry.register(BuiltInRegistries.BLOCK, "immersive_weathering:" + immersiveNames.get(i), immersive.get(i));
            var item = new net.minecraft.world.item.BlockItem(host, new net.minecraft.world.item.Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, "immersive_weathering:" + immersiveNames.get(i), item);
            item.registerBlocks(net.minecraft.world.item.Item.BY_BLOCK, item);
        }
        com.ordana.immersive_weathering.reg.ModBlocks.registerSnowBlocks();
        check(SnowHosts.entries().size() == 72, "Deferred IW snow registrations are incomplete");
        saplingProbe = registerProbe("sapling", new ProbeSapling());
        plantProbe = registerProbe("plant", new ProbePlant());
        probe = Registry.register(BuiltInRegistries.BLOCK, "iw_snow_test:probe", new ProbeSnow());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, "iw_snow_test:falling",
                com.ordana.immersive_weathering.reg.ModEntities.FALLING_LAYER.get());
        bootstrapped.setBoolean(null, false);
        Bootstrap.bootStrap();
        SnowHosts.entries().keySet().forEach(block -> block.getStateDefinition().getPossibleStates().forEach(BlockState::initCache));
        SnowHosts.entries().values().forEach(block -> block.get().getStateDefinition().getPossibleStates().forEach(BlockState::initCache));
        bindTags();
        loadLoot();
        resources(java.nio.file.Path.of(args[0]));
        mappingsAndShapes();
        coveredPlantColors();
        placement();
        plantGrowth();
        nativePlantIdentity();
        pairs();
        falling();
        weather();
        lifecycleAndDrops();
        System.out.println("Snow mechanics: " + cases + " production state/placement/physics/weather cases passed");
    }

    private static <T extends Block> T registerProbe(String name, T host) {
        Registry.register(BuiltInRegistries.BLOCK, "iw_snow_test:" + name, host);
        SnowCoveredBlock covered = SnowCoveredBlock.create(host);
        Registry.register(BuiltInRegistries.BLOCK, "iw_snow_test:covered_" + name, covered);
        SnowHosts.register(host, () -> covered);
        return host;
    }

    private static void resources(java.nio.file.Path root) throws Exception {
        for (Block host : SnowHosts.hosts()) {
            String name = BuiltInRegistries.BLOCK.getKey(host).getPath();
            java.nio.file.Path path = root.resolve("assets/immersive_weathering/blockstates/snow_covered_" + name + ".json");
            var json = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(path)).getAsJsonObject();
            for (var entry : json.getAsJsonArray("multipart")) {
                var id = new net.minecraft.resources.ResourceLocation(entry.getAsJsonObject().getAsJsonObject("apply").get("model").getAsString());
                if (id.getNamespace().equals("minecraft")) {
                    check(SnowMechanicsTest.class.getResource("/assets/minecraft/models/" + id.getPath() + ".json") != null, "Missing vanilla model " + id);
                } else {
                    check(java.nio.file.Files.isRegularFile(root.resolve("assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json")), "Missing composite model " + id);
                }
            }
            cases++;
        }
    }

    private static void loadLoot() throws Exception {
        loot = new net.minecraft.world.level.storage.loot.LootDataManager();
        Map<net.minecraft.world.level.storage.loot.LootDataId<?>, Object> tables = new HashMap<>();
        List<Block> blocks = new ArrayList<>(SnowHosts.hosts());
        blocks.add(Blocks.SNOW);
        for (Block block : blocks) {
            var id = block.getLootTable();
            try (var stream = SnowMechanicsTest.class.getResourceAsStream("/data/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json")) {
                check(stream != null, "Missing vanilla loot table " + id);
                var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                tables.put(new net.minecraft.world.level.storage.loot.LootDataId<>(net.minecraft.world.level.storage.loot.LootDataType.TABLE, id),
                        net.minecraft.world.level.storage.loot.LootDataType.TABLE.deserialize(id, json).orElseThrow());
            }
        }
        Field elements = net.minecraft.world.level.storage.loot.LootDataManager.class.getDeclaredField("elements");
        elements.setAccessible(true);
        elements.set(loot, Map.copyOf(tables));
    }

    private static void coveredPlantColors() throws Exception {
        Method colorState = com.ordana.immersive_weathering.ImmersiveWeatheringClient.class
                .getDeclaredMethod("snowHostColorState", BlockState.class);
        colorState.setAccessible(true);
        Map<Block, Block> frosty = Map.of(Blocks.GRASS,
                com.ordana.immersive_weathering.reg.ModBlocks.FROSTY_GRASS.get(), Blocks.FERN,
                com.ordana.immersive_weathering.reg.ModBlocks.FROSTY_FERN.get());
        for (Block host : List.of(Blocks.GRASS, Blocks.FERN, Blocks.POPPY, Blocks.OAK_FENCE,
                com.ordana.immersive_weathering.reg.ModBlocks.FROSTY_GRASS.get(),
                com.ordana.immersive_weathering.reg.ModBlocks.FROSTY_FERN.get())) {
            BlockState raw = host.defaultBlockState();
            for (int layers = 1; layers <= 8; layers++) {
                BlockState covered = SnowHosts.cover(raw, layers);
                BlockState appearance = (BlockState) colorState.invoke(null, covered);
                check(appearance.equals(frosty.getOrDefault(host, host).defaultBlockState()),
                        "Covered plant delegated the wrong color state: " + host);
                check(SnowHosts.raw(covered).equals(raw), "Frosty appearance changed the underlying host");
                check(SnowHosts.layers(covered) == layers, "Frosty appearance changed snow count");
                cases++;
            }
        }
    }

    private static void lifecycleAndDrops() throws Exception {
        for (Block host : SnowHosts.hosts()) {
            for (int layers = 1; layers <= 8; layers++) {
                BlockState raw = host.defaultBlockState();
                BlockState covered = SnowHosts.cover(raw, layers);
                Fixture level = world();
                level.states.put(POS, covered);
                if (host instanceof DoublePlantBlock) level.states.put(POS.above(),
                        raw.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
                level.light = 12;
                covered.randomTick(level, POS, level.random);
                check(level.getBlockState(POS).equals(raw), "Melt lost host " + host);
                check(level.entities.isEmpty(), "Melt duplicated drops");
                check(covered.getBlock().getCloneItemStack(level, POS, covered).getItem() == host.asItem(), "Pick-block returns wrapper");
                check(covered.getPistonPushReaction() == raw.getPistonPushReaction(), "Piston reaction changed");
                for (ItemStack tool : List.of(Items.DIAMOND_SHOVEL.getDefaultInstance(),
                        Items.DIAMOND_PICKAXE.getDefaultInstance(), Items.SHEARS.getDefaultInstance())) {
                    Entity breaker = new ProbeFalling(level, 1);
                    level.random.setSeed(8123);
                    List<ItemStack> expectedStacks = new ArrayList<>(Block.getDrops(raw, level, POS, null, breaker, tool));
                    expectedStacks.addAll(Block.getDrops(snow(layers), level, POS, null, breaker, tool));
                    Map<net.minecraft.world.item.Item, Integer> expected = counts(expectedStacks);
                    level.random.setSeed(8123);
                    List<ItemStack> actual = covered.getDrops(new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, Vec3.atCenterOf(POS))
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, breaker)
                            .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, tool));
                    Map<net.minecraft.world.item.Item, Integer> result = counts(actual);
                    check(result.equals(expected), "Composite loot mismatch " + host + " " + tool + ": " + result + " != " + expected);
                    cases++;
                }
            }
        }
        Method fallback = SnowPlantLootPredicateMixin.class.getDeclaredMethod("mixedPlantLocation",
                ServerLevel.class, BlockPos.class, CallbackInfoReturnable.class);
        fallback.setAccessible(true);
        for (Block host : List.of(Blocks.TALL_GRASS, Blocks.LARGE_FERN)) {
            SnowPlantLootPredicateMixin predicate = new SnowPlantLootPredicateMixin();
            for (var entry : Map.of("blocks", Set.of(host), "properties",
                    net.minecraft.advancements.critereon.StatePropertiesPredicate.ANY, "nbt",
                    net.minecraft.advancements.critereon.NbtPredicate.ANY).entrySet()) {
                Field field = SnowPlantLootPredicateMixin.class.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(predicate, entry.getValue());
            }
            Fixture level = world();
            level.states.put(POS, SnowHosts.cover(host.defaultBlockState(), 4));
            CallbackInfoReturnable<Boolean> result = new CallbackInfoReturnable<>("test", true, false);
            fallback.invoke(predicate, level, POS, result);
            check(result.getReturnValue(), "Mixed plant loot location rejected");
            cases++;
        }
        CommonConfigs.falling = false;
        Fixture unsupported = world();
        BlockState covered = SnowHosts.cover(Blocks.OAK_FENCE.defaultBlockState(), 4);
        unsupported.states.put(POS, covered);
        unsupported.states.remove(POS.below());
        SnowPhysics.checkSupport(unsupported, POS, covered);
        check(unsupported.entities.isEmpty() && unsupported.getBlockState(POS).is(Blocks.OAK_FENCE), "Falling-disable retained unsupported snow");
        CommonConfigs.falling = true;
        for (Block host : SnowHosts.hosts()) {
            if (!(host instanceof DoublePlantBlock)) continue;
            for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
                for (int layers = 1; layers <= 8; layers++) {
                    Fixture orphan = world();
                    BlockState state = SnowHosts.cover(host.defaultBlockState()
                            .setValue(DoublePlantBlock.HALF, half), layers);
                    orphan.states.put(POS, state);
                    state.tick(orphan, POS, orphan.random);
                    check(orphan.getBlockState(POS).equals(snow(layers)),
                            "Scheduled orphan resolution retained " + host + " " + half);
                    check(orphan.entities.stream().noneMatch(FallingLayerEntity.class::isInstance),
                            "Supported orphan snow fell");
                    cases++;
                }
            }
        }
    }

    private static Map<net.minecraft.world.item.Item, Integer> counts(List<ItemStack> stacks) {
        Map<net.minecraft.world.item.Item, Integer> result = new HashMap<>();
        for (ItemStack stack : stacks) result.merge(stack.getItem(), stack.getCount(), Integer::sum);
        return result;
    }

    private static void bindTags() {
        Map<net.minecraft.tags.TagKey<Block>, List<Holder<Block>>> tags = new HashMap<>();
        tags.put(BlockTags.DIRT, List.of(Blocks.DIRT.builtInRegistryHolder()));
        tags.put(BlockTags.MUSHROOM_GROW_BLOCK, List.of(Blocks.MYCELIUM.builtInRegistryHolder()));
        tags.put(BlockTags.SAND, List.of(Blocks.SAND.builtInRegistryHolder()));
        for (var entry : Map.of(BlockTags.FENCES, FenceBlock.class, BlockTags.WOODEN_FENCES, FenceBlock.class,
                BlockTags.WALLS, WallBlock.class).entrySet()) {
            List<Holder<Block>> values = new ArrayList<>();
            SnowHosts.entries().forEach((raw, covered) -> {
                if (entry.getValue().isInstance(raw)
                        && !(entry.getKey() == BlockTags.WOODEN_FENCES && raw == Blocks.NETHER_BRICK_FENCE)) {
                    values.add(raw.builtInRegistryHolder());
                    values.add(covered.get().builtInRegistryHolder());
                }
            });
            tags.put(entry.getKey(), values);
        }
        BuiltInRegistries.BLOCK.bindTags(tags);
        BuiltInRegistries.FLUID.bindTags(Map.of(FluidTags.WATER,
                List.of(Fluids.WATER.builtInRegistryHolder(), Fluids.FLOWING_WATER.builtInRegistryHolder())));
    }

    private static void mappingsAndShapes() throws Exception {
        Fixture level = world();
        for (Block host : SnowHosts.hosts()) {
            for (BlockState raw : host.getStateDefinition().getPossibleStates()) {
                if (!raw.getFluidState().isEmpty()) {
                    check(SnowHosts.cover(raw, 1) == null, "Accepted wet host");
                    continue;
                }
                for (int layers = 1; layers <= 8; layers++) {
                    BlockState covered = SnowHosts.cover(raw, layers);
                    check(covered != null && SnowHosts.raw(covered).equals(raw), "Lost host state");
                    check(SnowHosts.layers(covered) == layers, "Lost layers");
                    BlockState decoded = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), NbtUtils.writeBlockState(covered));
                    check(decoded.equals(covered), "NBT round trip changed snow state");
                    var expected = Shapes.or(raw.getCollisionShape(level, POS),
                            snow(layers).getCollisionShape(level, POS));
                    check(!Shapes.joinIsNotEmpty(expected, covered.getCollisionShape(level, POS), BooleanOp.NOT_SAME), "Wrong collision union");
                    check(covered.getShape(level, POS) != null && covered.getBlockSupportShape(level, POS) != null, "Null shape");
                    for (Rotation rotation : Rotation.values()) {
                        check(SnowHosts.raw(covered.rotate(rotation)).equals(raw.rotate(rotation)), "Rotation lost host state");
                        check(SnowHosts.layers(covered.rotate(rotation)) == layers, "Rotation lost snow");
                    }
                    cases++;
                }
            }
        }
        for (Block invalid : List.of(Blocks.CHEST, Blocks.FURNACE, Blocks.OAK_FENCE_GATE, Blocks.OAK_SLAB,
                Blocks.OAK_STAIRS, Blocks.WHEAT, Blocks.SEAGRASS, Blocks.VINE, Blocks.STONE, Blocks.WATER)) {
            check(SnowHosts.cover(invalid.defaultBlockState(), 1) == null, "Wrapped unsupported " + invalid);
            cases++;
        }
        check(SnowHosts.cover(Blocks.GRASS.defaultBlockState(), 0) == null
                && SnowHosts.cover(Blocks.GRASS.defaultBlockState(), 9) == null, "Invalid layer range");
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState raw = Blocks.OAK_FENCE.defaultBlockState();
            BlockState neighbor = SnowHosts.cover(raw, 3);
            check(((FenceBlock) Blocks.OAK_FENCE).connectsTo(neighbor, false, direction), "Raw fence does not connect to snow fence");
            BlockState updated = neighbor.updateShape(direction, raw, level, POS, POS.relative(direction));
            check(SnowHosts.raw(updated).getValue(switch (direction) {
                case NORTH -> FenceBlock.NORTH;
                case EAST -> FenceBlock.EAST;
                case SOUTH -> FenceBlock.SOUTH;
                case WEST -> FenceBlock.WEST;
                default -> throw new AssertionError(direction);
            }), "Snow fence does not connect to raw");
            cases++;
        }
    }

    private static void placement() throws Exception {
        Method placement = SnowHostPlacementMixin.class.getDeclaredMethod("snowHostPlacement",
                BlockPlaceContext.class, CallbackInfoReturnable.class);
        placement.setAccessible(true);
        Method replace = SnowLayerBlockMixin.class.getDeclaredMethod("replacePartialSnow", BlockState.class,
                BlockPlaceContext.class, CallbackInfoReturnable.class);
        replace.setAccessible(true);
        Method cover = SnowLayerBlockMixin.class.getDeclaredMethod("coverHost", BlockPlaceContext.class, CallbackInfoReturnable.class);
        cover.setAccessible(true);
        ProbeSnow mixin = probe;
        for (int layers = 1; layers <= 8; layers++) {
            Fixture level = world();
            level.states.put(POS, snow(layers));
            BlockPlaceContext context = context(level, Items.OAK_FENCE.getDefaultInstance());
            for (boolean enabled : new boolean[]{true, false}) {
                CommonConfigs.replacement = enabled;
                CallbackInfoReturnable<Boolean> replacement = new CallbackInfoReturnable<>("test", true, false);
                replace.invoke(mixin, snow(layers), context, replacement);
                check(replacement.getReturnValue() == (enabled && layers < 8), "Partial replacement gate");
                CommonConfigs.mixing = enabled;
                CallbackInfoReturnable<BlockState> result = new CallbackInfoReturnable<>("test", true, Blocks.OAK_FENCE.defaultBlockState());
                placement.invoke(new SnowHostPlacementMixin(), context, result);
                check((result.getReturnValue().getBlock() instanceof SnowCoveredBlock) == (enabled && layers < 8), "Host into snow gate");
                cases++;
            }
            CommonConfigs.mixing = true;
            level.states.put(POS, SnowHosts.cover(Blocks.OAK_FENCE.defaultBlockState(), layers));
            BlockPlaceContext snowContext = context(level, Items.SNOW.getDefaultInstance());
            CallbackInfoReturnable<BlockState> result = new CallbackInfoReturnable<>("test", true);
            cover.invoke(mixin, snowContext, result);
            check(result.isCancelled() == (layers < 8), "Snow into host cap");
            if (layers < 8) check(SnowHosts.layers(result.getReturnValue()) == layers + 1, "Snow stacking count");
            check(!level.getBlockState(POS).canBeReplaced(context), "Nonreplaceable snow fence overwritten");
            cases++;
        }
        CommonConfigs.mixing = CommonConfigs.replacement = true;
        Fixture level = world();
        level.states.put(POS, Blocks.OAK_FENCE.defaultBlockState());
        CallbackInfoReturnable<BlockState> result = new CallbackInfoReturnable<>("test", true);
        cover.invoke(mixin, context(level, Items.SNOW.getDefaultInstance()), result);
        check(SnowHosts.raw(result.getReturnValue()).is(Blocks.OAK_FENCE), "Raw host snow placement");
        for (Block host : SnowHosts.hosts()) {
            for (int layers = 0; layers < 8; layers++) {
                Fixture stacked = world();
                stacked.states.put(POS, layers == 0 ? host.defaultBlockState() : SnowHosts.cover(host.defaultBlockState(), layers));
                BlockPlaceContext snowContext = context(stacked, Items.SNOW.getDefaultInstance());
                CallbackInfoReturnable<BlockState> stackedResult = new CallbackInfoReturnable<>("test", true);
                cover.invoke(mixin, snowContext, stackedResult);
                check(stackedResult.isCancelled(), "Snow-into-host path was not selected");
                placement.invoke(new SnowHostPlacementMixin(), snowContext, stackedResult);
                check(SnowHosts.layers(stackedResult.getReturnValue()) == layers + 1,
                        "Outer item placement undid snow stacking on " + host);
                check(SnowHosts.raw(stackedResult.getReturnValue()).is(host), "Snow stacking changed the host");
                cases++;
            }
        }
        coveredReplacement(placement);
    }

    private static void coveredReplacement(Method placement) throws Exception {
        List<Block> hosts = new ArrayList<>(List.of(Blocks.OAK_FENCE, Blocks.COBBLESTONE_WALL));
        SnowHosts.hosts().stream().filter(host -> host instanceof BushBlock).forEach(hosts::add);
        List<Block> incoming = new ArrayList<>(hosts);
        incoming.addAll(List.of(Blocks.STONE, Blocks.CHEST, Blocks.SNOW));
        for (Block host : hosts) {
            for (BlockState raw : host.getStateDefinition().getPossibleStates()) {
                if (!raw.getFluidState().isEmpty()) continue;
                if (!(host instanceof BushBlock) && !raw.equals(host.defaultBlockState())) continue;
                for (int layers = 1; layers <= 8; layers++) {
                    for (boolean replacement : new boolean[]{true, false}) {
                        for (boolean mixing : new boolean[]{true, false}) {
                            CommonConfigs.replacement = replacement;
                            CommonConfigs.mixing = mixing;
                            Fixture level = world();
                            BlockState covered = SnowHosts.cover(raw, layers);
                            level.states.put(POS, covered);
                            for (Block next : incoming) {
                                BlockPlaceContext context = context(level, next.asItem().getDefaultInstance());
                                boolean expected = layers < 8 && (next == Blocks.SNOW ? mixing
                                        : replacement && raw.canBeReplaced(context));
                                check(covered.canBeReplaced(context) == expected,
                                        "Covered replacement diverged: " + raw + " layers=" + layers + " item=" + next
                                                + " replacement=" + replacement + " mixing=" + mixing);
                                BlockState nextState = next.defaultBlockState();
                                CallbackInfoReturnable<BlockState> result =
                                        new CallbackInfoReturnable<>("test", true, nextState);
                                placement.invoke(new SnowHostPlacementMixin(), context, result);
                                boolean preserve = expected && mixing && SnowHosts.supported(nextState);
                                BlockState preservedState = nextState;
                                if (expected && raw.getBlock() instanceof PinkPetalsBlock && nextState.is(raw.getBlock())) {
                                    preservedState = raw.setValue(PinkPetalsBlock.AMOUNT,
                                            Math.min(4, raw.getValue(PinkPetalsBlock.AMOUNT) + 1));
                                }
                                check(result.getReturnValue().equals(preserve
                                                ? SnowHosts.cover(preservedState, layers) : preservedState),
                                        "Replacement lost cover or wrapped an ineligible target: " + raw + " -> " + next);
                                cases++;
                            }
                            check(!covered.canBeReplaced(context(level, Items.SHEARS.getDefaultInstance())),
                                    "Non-block item replaced covered host");
                            cases++;
                            BlockState wet = Blocks.OAK_FENCE.defaultBlockState()
                                    .setValue(FenceBlock.WATERLOGGED, true);
                            CallbackInfoReturnable<BlockState> wetResult =
                                    new CallbackInfoReturnable<>("test", true, wet);
                            placement.invoke(new SnowHostPlacementMixin(), context(level, Items.OAK_FENCE.getDefaultInstance()), wetResult);
                            check(wetResult.getReturnValue().equals(wet), "Wrapped wet replacement");
                            CallbackInfoReturnable<BlockState> failed = new CallbackInfoReturnable<>("test", true);
                            placement.invoke(new SnowHostPlacementMixin(), context(level, Items.OAK_FENCE.getDefaultInstance()), failed);
                            check(failed.getReturnValue() == null, "Revived failed item placement");
                            cases += 2;
                        }
                    }
                }
            }
        }
        CommonConfigs.mixing = CommonConfigs.replacement = true;
    }

    private static void pairs() throws Exception {
        Method preserve = SnowDoublePlantMixin.class.getDeclaredMethod("preserveUpperSnow", net.minecraft.world.level.Level.class,
                BlockPos.class, BlockState.class, net.minecraft.world.entity.LivingEntity.class, ItemStack.class,
                org.spongepowered.asm.mixin.injection.callback.CallbackInfo.class);
        preserve.setAccessible(true);
        for (Block block : SnowHosts.hosts()) {
            if (!(block instanceof DoublePlantBlock)) continue;
            for (Block next : SnowHosts.hosts()) {
                if (!(next instanceof DoublePlantBlock)) continue;
                for (int layers = 1; layers <= 8; layers++) {
                    for (boolean replacement : new boolean[]{true, false}) {
                        for (boolean mixing : new boolean[]{true, false}) {
                            CommonConfigs.replacement = replacement;
                            CommonConfigs.mixing = mixing;
                            Fixture level = world();
                            BlockState upper = SnowHosts.cover(block.defaultBlockState()
                                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), layers);
                            level.states.put(POS.above(), upper);
                            ItemStack item = next.asItem().getDefaultInstance();
                            boolean expected = mixing && layers < 8 && upper.canBeReplaced(
                                    new net.minecraft.world.item.context.DirectionalPlaceContext(
                                            level, POS.above(), Direction.DOWN, item, Direction.UP));
                            var ci = new org.spongepowered.asm.mixin.injection.callback.CallbackInfo("test", true);
                            preserve.invoke(new SnowDoublePlantMixin(), level, POS, next.defaultBlockState(), null, item, ci);
                            check(ci.isCancelled() == expected, "Upper replacement preservation gate");
                            check(level.getBlockState(POS.above()).equals(expected
                                            ? SnowHosts.cover(next.defaultBlockState()
                                                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), layers) : upper),
                                    "Upper replacement lost snow or changed an ineligible target");
                            cases++;
                        }
                    }
                }
            }
            CommonConfigs.mixing = CommonConfigs.replacement = true;
            for (boolean coveredLower : new boolean[]{true, false}) {
                for (boolean coveredUpper : new boolean[]{true, false}) {
                    Fixture level = world();
                    BlockState lower = block.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
                    BlockState upper = block.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
                    level.states.put(POS, coveredLower ? SnowHosts.cover(lower, 3) : lower);
                    level.states.put(POS.above(), coveredUpper ? SnowHosts.cover(upper, 2) : upper);
                    check(SnowHosts.hostSurvives(lower, level, POS) && SnowHosts.hostSurvives(upper, level, POS.above()), "Mixed pair survival");
                    Map<BlockPos, BlockState> before = Map.copyOf(level.states);
                    if (coveredLower) level.getBlockState(POS).getBlock().setPlacedBy(level, POS,
                            level.getBlockState(POS), null, Items.SNOW.getDefaultInstance());
                    if (coveredUpper) level.getBlockState(POS.above()).getBlock().setPlacedBy(level, POS.above(),
                            level.getBlockState(POS.above()), null, Items.SNOW.getDefaultInstance());
                    check(before.equals(level.states), "Snow-item callback reset a partner or created a third half");
                    for (Direction dir : Direction.values()) {
                        check(!SnowHosts.doubleShape(upper, dir, level.getBlockState(POS.above().relative(dir)), level, POS.above()).isAir(), "Mixed upper update");
                        check(!SnowHosts.doubleShape(lower, dir, level.getBlockState(POS.relative(dir)), level, POS).isAir(), "Mixed lower update");
                    }
                    level.states.put(POS.below(), Blocks.AIR.defaultBlockState());
                    check(!SnowHosts.hostSurvives(lower, level, POS), "Unsupported plant survived");
                    level.states.remove(POS);
                    check(!SnowHosts.hostSurvives(upper, level, POS.above()), "Orphan upper survived");
                    cases++;
                }
            }
        }
    }

    private static void falling() throws Exception {
        for (Block host : List.of(Blocks.SNOW, Blocks.OAK_FENCE, Blocks.COBBLESTONE_WALL, Blocks.GRASS)) {
            for (int existing = host == Blocks.SNOW ? 1 : 0; existing <= 8; existing++) {
                for (int incoming = 1; incoming <= 8; incoming++) {
                    Fixture level = world();
                    BlockState raw = host.defaultBlockState();
                    BlockState old = host == Blocks.SNOW ? snow(existing) : existing == 0 ? raw : SnowHosts.cover(raw, existing);
                    level.states.put(POS, old);
                    int remaining = SnowPhysics.deposit(level, POS, incoming);
                    check(SnowHosts.layers(level.getBlockState(POS)) + remaining == existing + incoming, "Lost landing layers");
                    check(SnowHosts.layers(level.getBlockState(POS)) <= 8 && level.getBlockState(POS.above()).isAir(), "Buried next cell");
                    if (host != Blocks.SNOW) check(SnowHosts.raw(level.getBlockState(POS)).equals(raw), "Landing destroyed host");
                    cases++;
                }
            }
        }
        Fixture level = world();
        level.states.put(POS, snow(8));
        check(SnowPhysics.deposit(level, POS.above(), 8) == 8 && level.getBlockState(POS.above()).isAir(), "Overflow buried column");
        level.states.put(POS, SnowHosts.cover(Blocks.GRASS.defaultBlockState(), 8));
        check(SnowPhysics.supported(level, POS.above()), "Full covered snow does not support manual snow");
        check(SnowPhysics.deposit(level, POS.above(), 8) == 8, "Covered column overflow buried next cell");
        for (BlockState water : List.of(Blocks.WATER.defaultBlockState(),
                Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1),
                Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED, true))) {
            check(SnowPhysics.sourceWater(water) == (water.is(Blocks.WATER) && water.getFluidState().isSource()), "Source classification");
            level = world();
            level.states.put(POS, water);
            check(SnowPhysics.deposit(level, POS, 5) == 5 && level.getBlockState(POS).equals(water), "Landing replaced liquid/waterlogged host");
            cases++;
        }
        level = world();
        level.states.put(POS, SnowHosts.cover(Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 6));
        level.states.put(POS.below(), Blocks.AIR.defaultBlockState());
        BlockState host = SnowHosts.raw(level.getBlockState(POS));
        SnowPhysics.checkSupport(level, POS, level.getBlockState(POS));
        check(level.getBlockState(POS).equals(host), "Falling moved host");
        check(level.entities.size() == 1 && SnowHosts.layers(((FallingLayerEntity) level.entities.get(0)).getBlockState()) == 6, "Detached wrong layers");
        CompoundTag tag = new CompoundTag();
        level.entities.get(0).saveWithoutId(tag);
        FallingLayerEntity reloaded = new FallingLayerEntity(level);
        reloaded.load(tag);
        check(SnowHosts.layers(reloaded.getBlockState()) == 6, "Falling NBT lost layers");
        for (int layers = 1; layers <= 8; layers++) {
            for (BlockState water : List.of(Blocks.WATER.defaultBlockState(),
                    Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3),
                    Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED, true))) {
                Fixture fluidWorld = world();
                fluidWorld.states.put(POS, water);
                ProbeFalling entity = new ProbeFalling(fluidWorld, layers);
                entity.setPos(0.5, 65.2, 0.5);
                entity.setNoGravity(true);
                entity.setDeltaMovement(0, -0.7, 0);
                entity.tick();
                boolean source = SnowPhysics.sourceWater(water);
                check(entity.isRemoved(), "Fluid contact did not finish falling snow");
                check(fluidWorld.getBlockState(POS).equals(source ? Blocks.ICE.defaultBlockState() : water), "Wrong ice conversion");
                int dropped = fluidWorld.entities.stream().filter(ItemEntity.class::isInstance)
                        .map(ItemEntity.class::cast).mapToInt(item -> item.getItem().getCount()).sum();
                check(dropped == (source ? 0 : layers), "Fluid contact lost or duplicated snow layers");
                cases++;
            }
            Fixture landing = world();
            landing.states.put(POS, snow(7));
            ProbeFalling entity = new ProbeFalling(landing, layers);
            entity.setPos(0.5, 64.9, 0.5);
            entity.setNoGravity(true);
            entity.ground = true;
            entity.tick();
            int dropped = landing.entities.stream().filter(ItemEntity.class::isInstance)
                    .map(ItemEntity.class::cast).mapToInt(item -> item.getItem().getCount()).sum();
            check(entity.isRemoved() && SnowHosts.layers(landing.getBlockState(POS)) + dropped == layers + 7, "Entity landing conservation");
            cases++;
        }
    }

    private static void weather() throws Exception {
        Biome cold = biome(-0.5f), warm = biome(1);
        for (int rule : new int[]{0, 1, 2, 8, 24}) {
            for (int cap : new int[]{1, 3, 8}) {
                for (boolean raining : new boolean[]{false, true}) {
                    for (boolean thunder : new boolean[]{false, true}) {
                        for (boolean ordinary : new boolean[]{false, true}) {
                            Fixture level = world();
                            level.raining = raining;
                            level.thunder = thunder;
                            level.rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(rule, null);
                            CommonConfigs.cap = cap;
                            CommonConfigs.ordinary = ordinary;
                            level.states.put(POS, snow(1));
                            for (int tick = 0; tick < 12; tick++) SnowWeather.sample(cold, level,
                                    level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, POS));
                            int expected = rule > 0 && raining && (thunder || ordinary) ? SnowWeather.effectiveCap(cap, rule) : 1;
                            check(SnowHosts.layers(level.getBlockState(POS)) == expected, "Weather policy mismatch");
                            check(level.getBlockState(POS.above()).isAir(), "Weather exceeded one block");
                            cases++;
                        }
                    }
                }
            }
        }
        CommonConfigs.cap = 8;
        CommonConfigs.ordinary = false;
        for (String veto : List.of("warm", "light", "roof", "unloaded", "disabled")) {
            Fixture level = world();
            level.states.put(POS, snow(2));
            if (veto.equals("light")) level.light = 12;
            if (veto.equals("roof")) level.roof = true;
            if (veto.equals("unloaded")) level.loaded = false;
            CommonConfigs.accumulation = !veto.equals("disabled");
            SnowWeather.sample(veto.equals("warm") ? warm : cold, level, POS);
            check(SnowHosts.layers(level.getBlockState(POS)) == 2, "Ignored " + veto);
            cases++;
        }
        CommonConfigs.accumulation = true;
        Fixture level = world();
        level.thunder = false;
        check(SnowWeather.sample(cold, level, POS), "Suppressed vanilla first snow in ordinary snowfall");
        for (Block host : SnowHosts.hosts()) {
            level = world();
            if (host instanceof MushroomBlock) level.states.put(POS.below(), Blocks.MYCELIUM.defaultBlockState());
            level.states.put(POS, host.defaultBlockState());
            if (host instanceof DoublePlantBlock) level.states.put(POS.above(),
                    host.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
            for (int tick = 0; tick < 12; tick++) SnowWeather.sample(cold, level,
                    level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, POS));
            check(SnowHosts.layers(level.getBlockState(POS)) == 8
                    && SnowHosts.raw(level.getBlockState(POS)).is(host), "Host accumulation lost host or cap " + host);
            cases++;
        }
    }

    private static void plantGrowth() throws Exception {
        Block weeds = com.ordana.immersive_weathering.reg.ModBlocks.WEEDS.get();
        for (int layers = 1; layers <= 8; layers++) {
            Fixture level = world();
            level.states.put(POS, SnowHosts.cover(weeds.defaultBlockState(), layers));
            for (int ticks = 0; ticks < 1000 && SnowHosts.raw(level.getBlockState(POS)).getValue(CropBlock.AGE) < 7; ticks++) {
                BlockState state = level.getBlockState(POS);
                state.randomTick(level, POS, level.random);
                check(SnowHosts.layers(level.getBlockState(POS)) == layers, "Weed aging removed snow");
            }
            check(SnowHosts.raw(level.getBlockState(POS)).getValue(CropBlock.AGE) == 7, "Covered weeds stopped aging");
            level.states.put(POS, SnowHosts.cover(weeds.defaultBlockState(), layers));
            ((BonemealableBlock) level.getBlockState(POS).getBlock())
                    .performBonemeal(level, level.random, POS, level.getBlockState(POS));
            check(SnowHosts.layers(level.getBlockState(POS)) == layers
                    && SnowHosts.raw(level.getBlockState(POS)).getValue(CropBlock.AGE) > 0, "Weed bonemeal lost state or snow");
            level = world();
            level.states.put(POS, SnowHosts.cover(Blocks.OAK_SAPLING.defaultBlockState(), layers));
            for (int ticks = 0; ticks < 100 && SnowHosts.raw(level.getBlockState(POS)).getValue(SaplingBlock.STAGE) == 0; ticks++) {
                BlockState state = level.getBlockState(POS);
                state.randomTick(level, POS, level.random);
            }
            check(SnowHosts.layers(level.getBlockState(POS)) == layers
                    && SnowHosts.raw(level.getBlockState(POS)).getValue(SaplingBlock.STAGE) == 1, "Sapling stage lost snow");
            cases += 3;
        }
        for (boolean grows : new boolean[]{true, false}) {
            saplingProbe.grows = grows;
            for (int layers = 1; layers <= 8; layers++) {
                Fixture level = world();
                List<BlockPos> positions = List.of(POS, POS.east(), POS.south(), POS.east().south());
                for (int i = 0; i < positions.size(); i++) {
                    level.states.put(positions.get(i), SnowHosts.cover(saplingProbe.defaultBlockState()
                            .setValue(SaplingBlock.STAGE, 1), 1 + (layers + i) % 8));
                }
                BlockState state = level.getBlockState(POS);
                ((BonemealableBlock) state.getBlock()).performBonemeal(level, level.random, POS, state);
                for (int i = 0; i < positions.size(); i++) {
                    BlockState result = level.getBlockState(positions.get(i));
                    check(grows ? result.is(Blocks.OAK_LOG)
                                    : SnowHosts.raw(result).is(saplingProbe)
                                        && SnowHosts.layers(result) == 1 + (layers + i) % 8,
                            "2x2 growth rollback lost a cover or overwrote a trunk");
                    cases++;
                }
            }
        }
        Fixture level = world();
        BlockState state = SnowHosts.cover(plantProbe.defaultBlockState(), 3);
        level.states.put(POS, state);
        state.entityInside(level, POS, new ProbeFalling(level, 1));
        state.getBlock().animateTick(state, level, POS, level.random);
        check(plantProbe.contacts == 1 && plantProbe.animations == 1, "Plant contact/visual effects were swallowed");
        cases++;
    }

    private static void nativePlantIdentity() throws Exception {
        com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original =
                arguments -> ((BlockState) arguments[0]).is((Block) arguments[1]);
        for (Class<?> mixin : List.of(com.ordana.immersive_weathering.mixins.CropBlockMixin.class,
                com.ordana.immersive_weathering.mixins.SnowMushroomMixin.class,
                com.ordana.immersive_weathering.mixins.SnowMegaSaplingMixin.class)) {
            Method identity = mixin.getDeclaredMethod("snowHostIdentity", BlockState.class, Block.class,
                    com.llamalad7.mixinextras.injector.wrapoperation.Operation.class);
            identity.setAccessible(true);
            for (Block host : List.of(com.ordana.immersive_weathering.reg.ModBlocks.WEEDS.get(),
                    Blocks.BROWN_MUSHROOM, Blocks.SPRUCE_SAPLING)) {
                for (int layers = 1; layers <= 8; layers++) {
                    check((Boolean) identity.invoke(null, SnowHosts.cover(host.defaultBlockState(), layers), host, original),
                            "Wrapped plant did not count toward native growth rules");
                    check(!(Boolean) identity.invoke(null, SnowHosts.cover(host.defaultBlockState(), layers), Blocks.STONE, original),
                            "Wrapped plant matched a different native block");
                    check((Boolean) identity.invoke(null, host.defaultBlockState(), host, original),
                            "Uncovered native identity changed");
                    cases += 3;
                }
            }
        }
    }

    private static Biome biome(float temperature) {
        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(temperature).downfall(0.5f)
                .specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build())
                .mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(BiomeGenerationSettings.EMPTY).build();
    }

    private static BlockState snow(int layers) { return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers); }

    private static BlockPlaceContext context(Fixture level, ItemStack stack) {
        return new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, stack,
                new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false)) {
            @Override public BlockPos getClickedPos() { return POS; }
        };
    }

    private static Fixture world() throws Exception {
        Fixture world = (Fixture) UNSAFE.allocateInstance(Fixture.class);
        world.states = new HashMap<>();
        world.entities = new ArrayList<>();
        world.states.put(POS.below(), Blocks.DIRT.defaultBlockState());
        world.rules = new GameRules();
        world.server = (FixtureServer) UNSAFE.allocateInstance(FixtureServer.class);
        world.loaded = world.raining = world.thunder = true;
        Field random = net.minecraft.world.level.Level.class.getDeclaredField("random");
        UNSAFE.putObject(world, UNSAFE.objectFieldOffset(random), RandomSource.create(123));
        return world;
    }

    private static void check(boolean passed, String message) { if (!passed) throw new AssertionError(message); }
    private static Unsafe unsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (ReflectiveOperationException e) { throw new ExceptionInInitializerError(e); }
    }

    private static final class ProbeSnow extends SnowLayerBlockMixin {
        private ProbeSnow() { super(BlockBehaviour.Properties.of()); }
    }

    private static final class ProbeSapling extends SaplingBlock {
        private boolean grows;
        private ProbeSapling() {
            super(new net.minecraft.world.level.block.grower.OakTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING));
        }
        @Override public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
            for (BlockPos target : List.of(pos, pos.east(), pos.south(), pos.east().south())) {
                level.setBlockAndUpdate(target, grows ? Blocks.OAK_LOG.defaultBlockState() : state);
            }
        }
    }

    private static final class ProbePlant extends BushBlock {
        private int contacts, animations;
        private ProbePlant() { super(BlockBehaviour.Properties.copy(Blocks.POPPY)); }
        @Override public void entityInside(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, Entity entity) {
            check(state.is(this), "Contact received a composite instead of the native host");
            contacts++;
        }
        @Override public void animateTick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
            check(state.is(this), "Animation received a composite instead of the native host");
            animations++;
        }
    }

    private static final class ProbeFalling extends FallingLayerEntity {
        private boolean ground;
        private ProbeFalling(Fixture level, int layers) { super(level, POS.above(), snow(layers)); }
        @Override public void move(MoverType type, Vec3 delta) {
            setPos(getX() + delta.x, getY() + delta.y, getZ() + delta.z);
        }
        @Override public boolean onGround() { return ground; }
    }

    private static final class Fixture extends ServerLevel {
        private Map<BlockPos, BlockState> states;
        private List<Entity> entities;
        private GameRules rules;
        private boolean raining, thunder, loaded, roof;
        private int light;
        private FixtureServer server;
        private Fixture() {
            super(null, null, null, null, null, null, null, false, 0, List.of(), false, null);
            throw new AssertionError("Fixture constructor");
        }
        @Override public BlockState getBlockState(BlockPos pos) { return states.getOrDefault(pos, Blocks.AIR.defaultBlockState()); }
        @Override public FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) { states.put(pos.immutable(), state); return true; }
        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public boolean isLoaded(BlockPos pos) { return loaded; }
        @Override public int getBrightness(LightLayer layer, BlockPos pos) { return layer == LightLayer.BLOCK ? light : 15; }
        @Override public int getMaxLocalRawBrightness(BlockPos pos) { return 15; }
        @Override public boolean isRaining() { return raining; }
        @Override public boolean isThundering() { return thunder; }
        @Override public boolean isRainingAt(BlockPos pos) { return false; }
        @Override public BlockPos getHeightmapPos(Heightmap.Types type, BlockPos pos) {
            if (roof) return POS.above(4);
            int height = states.entrySet().stream().filter(entry -> entry.getKey().getX() == pos.getX()
                            && entry.getKey().getZ() == pos.getZ() && type.isOpaque().test(entry.getValue()))
                    .mapToInt(entry -> entry.getKey().getY()).max().orElse(getMinBuildHeight() - 1) + 1;
            return new BlockPos(pos.getX(), height, pos.getZ());
        }
        @Override public GameRules getGameRules() { return rules; }
        @Override public RegistryAccess registryAccess() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
        @Override public net.minecraft.server.MinecraftServer getServer() { return server; }
        @Override public RandomSource getRandomSequence(net.minecraft.resources.ResourceLocation id) { return random; }
        @Override public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() { return net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS; }
        @Override public List<Entity> getEntities(Entity except, net.minecraft.world.phys.AABB box,
                                                  java.util.function.Predicate<? super Entity> predicate) { return List.of(); }
        @Override public void scheduleTick(BlockPos pos, Block block, int delay) {}
        @Override public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {}
        @Override public boolean addFreshEntity(Entity entity) { entities.add(entity); return true; }
    }

    private static final class FixtureServer extends net.minecraft.server.dedicated.DedicatedServer {
        private FixtureServer() { super(null, null, null, null, null, null, null, null); throw new AssertionError("Fixture constructor"); }
        @Override public net.minecraft.world.level.storage.loot.LootDataManager getLootData() { return loot; }
    }
}
