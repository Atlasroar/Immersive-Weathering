package com.ordana.immersive_weathering.piles;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.reg.ModItems;
import com.ordana.immersive_weathering.sand.SandHosts;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Actual Fabric registration and woven item placement, with bounded storage instead of a saved world. */
public final class PilePlacementIntegrationTest {
    private static final BlockPos POS = new BlockPos(0, 64, 0);
    private static int checks;

    public static void verify() throws Exception {
        require(CommonConfigs.SNOW_HOSTS.get() && CommonConfigs.SAND_HOSTS.get(), "Mixing defaults must be enabled");
        require(SnowHosts.entries().size() == 116 && SandHosts.entries().size() == 116,
                "Real Fabric host registrations must be complete");
        net.minecraft.core.registries.BuiltInRegistries.BLOCK.bindTags(Map.of(
                net.minecraft.tags.BlockTags.DEAD_BUSH_MAY_PLACE_ON,
                List.of(Blocks.SAND.builtInRegistryHolder(), Blocks.RED_SAND.builtInRegistryHolder())));
        List<String> failures = new ArrayList<>();
        for (Block mineral : List.of(Blocks.SNOW, ModBlocks.SAND_LAYER_BLOCK.get(), ModBlocks.RED_SAND_LAYER_BLOCK.get())) {
            for (int layers = 1; layers <= 8; layers++) {
                for (Block plant : List.of(Blocks.POPPY, Blocks.DANDELION, Blocks.OXEYE_DAISY, Blocks.DEAD_BUSH,
                        Blocks.FERN, Blocks.GRASS, Blocks.WHEAT,
                        ModBlocks.FROSTY_GRASS.get(), ModBlocks.FROSTY_FERN.get())) {
                    Fixture level = world(plant);
                    level.states.put(POS, mineral.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
                    ItemStack item = new ItemStack(plant.asItem(), 2);
                    var context = new DirectionalPlaceContext(level, POS, Direction.DOWN, item, Direction.UP);
                    var result = ((BlockItem) item.getItem()).place(context);
                    BlockState actual = level.getBlockState(POS);
                    if (mineral == Blocks.SNOW && layers == 8) {
                        require(actual.getBlock() == Blocks.SNOW && item.getCount() == 2,
                                "Full snow cannot be replaced by a plant");
                        continue;
                    }
                    boolean retained = actual.getBlock() instanceof PileCoveredBlock
                            && PilePlants.raw(actual).getBlock() == plant
                            && actual.getValue(SnowLayerBlock.LAYERS) == layers && sameMineral(actual, mineral);
                    if (!retained) failures.add(mineral + " layers=" + layers + " plant=" + plant
                            + " result=" + result + " actual=" + actual);
                    else require(item.getCount() == 1 && result.consumesAction(), "Native item consumption/result");
                }
            }
        }
        if (!failures.isEmpty()) throw new AssertionError("Woven placement lost piles (" + failures.size()
                + " cases):\n" + String.join("\n", failures));
        plantFirst();
        wallRoots();
        placementControls();
        frostConversion();
        System.out.println("Fabric pile placement: " + checks + " checks passed (woven mixins, no saved world)");
    }

    private static void plantFirst() throws Exception {
        for (Block mineral : List.of(Blocks.SNOW, ModBlocks.SAND_LAYER_BLOCK.get(), ModBlocks.RED_SAND_LAYER_BLOCK.get())) {
            for (Block plant : List.of(Blocks.POPPY, Blocks.OXEYE_DAISY, Blocks.DEAD_BUSH,
                    Blocks.FERN, Blocks.GRASS, Blocks.WHEAT)) {
                Fixture level = world(plant);
                level.states.put(POS, plant.defaultBlockState());
                for (int layers = 1; layers <= 8; layers++) {
                    ItemStack item = new ItemStack(mineral.asItem(), 2);
                    InteractionResult result = ((BlockItem) item.getItem()).place(
                            new DirectionalPlaceContext(level, POS, Direction.DOWN, item, Direction.UP));
                    BlockState actual = level.getBlockState(POS);
                    require(result.consumesAction() && item.getCount() == 1, "Adding piles consumes exactly one item");
                    require(actual.getBlock() instanceof PileCoveredBlock && sameMineral(actual, mineral)
                                    && PilePlants.raw(actual).getBlock() == plant
                                    && actual.getValue(SnowLayerBlock.LAYERS) == layers,
                            "Plant-first placement and repeated stacking retain host and mineral depth");
                }
            }
        }
    }

    private static void wallRoots() throws Exception {
        require(Items.HANGING_ROOTS instanceof com.ordana.immersive_weathering.fabric.CeilingAndWallBlockItem,
                "Use the actual overriding Fabric roots item");
        for (Block mineral : List.of(Blocks.SNOW, ModBlocks.SAND_LAYER_BLOCK.get(), ModBlocks.RED_SAND_LAYER_BLOCK.get())) {
            for (int layers : List.of(1, 4, 7)) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    Fixture level = world();
                    level.states.put(POS, mineral.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers));
                    level.states.put(POS.relative(direction), Blocks.STONE.defaultBlockState());
                    ItemStack item = new ItemStack(Items.HANGING_ROOTS, 2);
                    var context = new DirectionalPlaceContext(level, POS, direction, item, direction.getOpposite());
                    BlockState expected = ModBlocks.HANGING_ROOTS_WALL.get().getStateForPlacement(context);
                    require(expected != null, "Raw wall-root placement is valid");
                    InteractionResult result = ((BlockItem) item.getItem()).place(context);
                    BlockState actual = level.getBlockState(POS);
                    require(result.consumesAction() && item.getCount() == 1
                                    && actual.getBlock() instanceof PileCoveredBlock
                                    && PilePlants.raw(actual) == expected && sameMineral(actual, mineral)
                                    && actual.getValue(SnowLayerBlock.LAYERS) == layers,
                            "Overridden getPlacementState must preserve mineral, depth and root facing");
                }
            }
        }
    }

    private static void placementControls() throws Exception {
        for (Block mineral : List.of(Blocks.SNOW, ModBlocks.SAND_LAYER_BLOCK.get(), ModBlocks.RED_SAND_LAYER_BLOCK.get())) {
            Fixture level = world();
            BlockState old = mineral.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 3);
            level.states.put(POS, old);
            level.obstructed = true;
            ItemStack flower = new ItemStack(Items.POPPY, 2);
            InteractionResult blocked = ((BlockItem) flower.getItem()).place(
                    new DirectionalPlaceContext(level, POS, Direction.DOWN, flower, Direction.UP));
            require(!blocked.consumesAction() && level.getBlockState(POS) == old && flower.getCount() == 2
                    && level.writes == 0, "Do not revive obstructed placement or consume items");
            level.obstructed = false;
            ItemStack stone = new ItemStack(Items.STONE, 2);
            InteractionResult replaced = ((BlockItem) stone.getItem()).place(
                    new DirectionalPlaceContext(level, POS, Direction.DOWN, stone, Direction.UP));
            require(replaced.consumesAction() && level.getBlockState(POS).getBlock() == Blocks.STONE
                    && stone.getCount() == 1, "Ordinary solids still replace eligible bare piles");
        }
    }

    private static void frostConversion() throws Exception {
        for (Block mineral : List.of(Blocks.SNOW, ModBlocks.SAND_LAYER_BLOCK.get(), ModBlocks.RED_SAND_LAYER_BLOCK.get())) {
            for (Block plant : List.of(Blocks.GRASS, Blocks.FERN)) {
                for (int layers : List.of(1, 4, 8)) {
                    for (boolean rejected : List.of(false, true)) {
                        Fixture level = world();
                        BlockState old = mineral == Blocks.SNOW ? SnowHosts.cover(plant.defaultBlockState(), layers)
                                : SandHosts.cover(plant.defaultBlockState(), (com.ordana.immersive_weathering.blocks.SandLayerBlock) mineral, layers);
                        level.states.put(POS, old);
                        level.failWrites = rejected;
                        TestPlayer player = player();
                        ItemStack frost = new ItemStack(ModItems.FROST_ITEM.get(), 2);
                        player.held = frost;
                        var hit = new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false);
                        InteractionResult result = frost.getItem().useOn(new ItemUse(level, player, frost, hit));
                        if (rejected) {
                            require(result == InteractionResult.FAIL && level.getBlockState(POS) == old
                                            && frost.getCount() == 2 && level.sounds == 0 && level.writes == 0,
                                    "Rejected frost writes must preserve cover and item with no effects");
                        } else {
                            BlockState actual = level.getBlockState(POS);
                            Block expected = plant == Blocks.GRASS ? ModBlocks.FROSTY_GRASS.get() : ModBlocks.FROSTY_FERN.get();
                            require(result.consumesAction() && frost.getCount() == 1 && level.writes == 1
                                            && level.sounds == 1 && PilePlants.raw(actual).getBlock() == expected
                                            && sameMineral(actual, mineral) && actual.getValue(SnowLayerBlock.LAYERS) == layers,
                                    "Frost item converts the host without losing its mineral or depth");
                            player.held = new ItemStack(Items.FLINT_AND_STEEL);
                            player.abilities.instabuild = true;
                            InteractionResult thawed = actual.use(level, player, InteractionHand.MAIN_HAND, hit);
                            BlockState restored = level.getBlockState(POS);
                            require(thawed.consumesAction() && PilePlants.raw(restored).getBlock() == plant
                                            && sameMineral(restored, mineral) && restored.getValue(SnowLayerBlock.LAYERS) == layers,
                                    "Native thaw interaction restores the host without stripping its cover");
                        }
                    }
                }
            }
        }
    }

    private static boolean sameMineral(BlockState state, Block mineral) {
        return mineral == Blocks.SNOW ? state.getBlock() instanceof com.ordana.immersive_weathering.snow.SnowCoveredBlock
                : SandHosts.pile(state) == mineral;
    }

    private static Unsafe unsafe() throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    private static TestPlayer player() throws Exception {
        TestPlayer player = (TestPlayer) unsafe().allocateInstance(TestPlayer.class);
        player.abilities = new net.minecraft.world.entity.player.Abilities();
        return player;
    }

    private static Fixture world() throws Exception {
        Unsafe unsafe = unsafe();
        Fixture level = (Fixture) unsafe.allocateInstance(Fixture.class);
        level.states = new HashMap<>();
        level.states.put(POS.below(), Blocks.FARMLAND.defaultBlockState());
        level.border = new WorldBorder();
        Field random = net.minecraft.world.level.Level.class.getDeclaredField("random");
        unsafe.putObject(level, unsafe.objectFieldOffset(random), new net.minecraft.world.level.levelgen.LegacyRandomSource(1));
        return level;
    }

    private static Fixture world(Block plant) throws Exception {
        Fixture level = world();
        if (plant == Blocks.DEAD_BUSH) level.states.put(POS.below(), Blocks.SAND.defaultBlockState());
        return level;
    }

    private static void require(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    private static final class Fixture extends ServerLevel {
        private Map<BlockPos, BlockState> states;
        private WorldBorder border;
        private boolean obstructed, failWrites;
        private int writes, sounds;
        private Fixture() { super(null, null, null, null, null, null, null, false, 0, List.of(), false, null); throw new AssertionError(); }
        @Override public BlockState getBlockState(BlockPos pos) { return states.getOrDefault(pos, Blocks.AIR.defaultBlockState()); }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion) {
            if (failWrites) return false;
            writes++;
            states.put(pos.immutable(), state);
            return true;
        }
        @Override public int getMinBuildHeight() { return -64; }
        @Override public int getHeight() { return 384; }
        @Override public int getRawBrightness(BlockPos pos, int darkness) { return 15 - darkness; }
        @Override public WorldBorder getWorldBorder() { return border; }
        @Override public net.minecraft.world.level.material.FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
        @Override public boolean isUnobstructed(BlockState state, BlockPos pos, CollisionContext context) { return !obstructed; }
        @Override public net.minecraft.world.flag.FeatureFlagSet enabledFeatures() { return net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS; }
        @Override public void scheduleTick(BlockPos pos, Block block, int delay) {}
        @Override public void playSound(net.minecraft.world.entity.player.Player player, double x, double y, double z,
                                        net.minecraft.sounds.SoundEvent sound, net.minecraft.sounds.SoundSource source,
                                        float volume, float pitch) { sounds++; }
        @Override public void gameEvent(net.minecraft.world.level.gameevent.GameEvent event, net.minecraft.world.phys.Vec3 pos,
                                        net.minecraft.world.level.gameevent.GameEvent.Context context) {}
    }

    private static final class ItemUse extends UseOnContext {
        private ItemUse(Fixture level, TestPlayer player, ItemStack stack, BlockHitResult hit) {
            super(level, player, InteractionHand.MAIN_HAND, stack, hit);
        }
    }

    private static final class TestPlayer extends net.minecraft.server.level.ServerPlayer {
        private net.minecraft.world.entity.player.Abilities abilities;
        private ItemStack held;
        private TestPlayer() { super(null, null, null); throw new AssertionError(); }
        @Override public net.minecraft.world.entity.player.Abilities getAbilities() { return abilities; }
        @Override public ItemStack getItemInHand(InteractionHand hand) { return held; }
        @Override public void awardStat(net.minecraft.stats.Stat<?> stat, int amount) {}
    }
}
