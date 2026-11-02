package com.ordana.immersive_weathering.snow;

import com.ordana.immersive_weathering.reg.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SnowHosts {
    private static final Map<Block, Supplier<SnowCoveredBlock>> HOSTS = new LinkedHashMap<>();

    public static List<Block> vanillaHosts() {
        return List.of(
                Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE,
                Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.MANGROVE_FENCE, Blocks.CHERRY_FENCE,
                Blocks.BAMBOO_FENCE, Blocks.CRIMSON_FENCE, Blocks.WARPED_FENCE, Blocks.NETHER_BRICK_FENCE,
                Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BRICK_WALL,
                Blocks.PRISMARINE_WALL, Blocks.RED_SANDSTONE_WALL, Blocks.MOSSY_STONE_BRICK_WALL,
                Blocks.GRANITE_WALL, Blocks.STONE_BRICK_WALL, Blocks.NETHER_BRICK_WALL, Blocks.ANDESITE_WALL,
                Blocks.RED_NETHER_BRICK_WALL, Blocks.SANDSTONE_WALL, Blocks.END_STONE_BRICK_WALL,
                Blocks.DIORITE_WALL, Blocks.BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE_WALL,
                Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.COBBLED_DEEPSLATE_WALL, Blocks.POLISHED_DEEPSLATE_WALL,
                Blocks.DEEPSLATE_BRICK_WALL, Blocks.DEEPSLATE_TILE_WALL, Blocks.MUD_BRICK_WALL,
                Blocks.GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
                Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY,
                Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
                Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                Blocks.WITHER_ROSE, Blocks.TORCHFLOWER, Blocks.PINK_PETALS,
                Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
                Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.MANGROVE_PROPAGULE,
                Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.DEAD_BUSH);
    }

    public static List<Block> immersiveHosts() {
        return List.of(ModBlocks.WEEDS.get(), ModBlocks.DUNE_GRASS.get(),
                ModBlocks.FROSTY_GRASS.get(), ModBlocks.FROSTY_FERN.get());
    }

    public static List<Block> hosts() {
        return java.util.stream.Stream.concat(vanillaHosts().stream(), immersiveHosts().stream()).toList();
    }

    public static void init() {
        for (Block block : vanillaHosts()) registerHost(block);
        registerImmersiveHost("weeds", ModBlocks.WEEDS);
        registerImmersiveHost("dune_grass", ModBlocks.DUNE_GRASS);
        registerImmersiveHost("frosty_grass", ModBlocks.FROSTY_GRASS);
        registerImmersiveHost("frosty_fern", ModBlocks.FROSTY_FERN);
    }

    private static void registerImmersiveHost(String name, Supplier<? extends Block> host) {
        // Moonlight resolves block suppliers during registration, after commonInit returns.
        ModBlocks.regBlock("snow_covered_" + name, () -> {
            Block block = host.get();
            SnowCoveredBlock covered = SnowCoveredBlock.create(block);
            register(block, () -> covered);
            return covered;
        });
    }

    private static void registerHost(Block block) {
        String name = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
        register(block, ModBlocks.regBlock("snow_covered_" + name, () -> SnowCoveredBlock.create(block)));
    }

    private SnowHosts() {}

    /** Explicit integration point; callers must register a matching, non-block-entity state wrapper. */
    public static void register(Block host, Supplier<SnowCoveredBlock> wrapper) {
        if (host.defaultBlockState().hasBlockEntity() || host instanceof SnowCoveredBlock || HOSTS.containsKey(host)) {
            throw new IllegalArgumentException("Invalid or duplicate snow host: " + host);
        }
        HOSTS.put(host, wrapper);
    }

    public static Map<Block, Supplier<SnowCoveredBlock>> entries() {
        return Collections.unmodifiableMap(HOSTS);
    }

    public static boolean supported(BlockState state) {
        return !state.hasBlockEntity() && state.getFluidState().isEmpty()
                && (state.getBlock() instanceof SnowCoveredBlock || HOSTS.containsKey(state.getBlock()));
    }

    public static BlockState raw(BlockState state) {
        return state.getBlock() instanceof SnowCoveredBlock block ? copy(state, block.host().defaultBlockState()) : state;
    }

    @Nullable
    public static BlockState cover(BlockState state, int layers) {
        if (layers < 1 || layers > 8 || !supported(state)) return null;
        if (state.getBlock() instanceof SnowCoveredBlock) return state.setValue(SnowLayerBlock.LAYERS, layers);
        SnowCoveredBlock block = HOSTS.get(state.getBlock()).get();
        if (block.host() != state.getBlock()) throw new IllegalStateException("Snow wrapper has the wrong host: " + block);
        for (Property<?> property : state.getProperties()) {
            if (property != BlockStateProperties.WATERLOGGED && !block.defaultBlockState().hasProperty(property)) {
                throw new IllegalStateException("Snow wrapper cannot preserve property " + property + " of " + state);
            }
        }
        return copy(state, block.defaultBlockState()).setValue(SnowLayerBlock.LAYERS, layers);
    }

    public static BlockState copy(BlockState from, BlockState to) {
        for (Property<?> property : from.getProperties()) {
            if (to.hasProperty(property)) to = copyProperty(from, to, property);
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }

    public static int layers(BlockState state) {
        return state.is(Blocks.SNOW) || state.getBlock() instanceof SnowCoveredBlock
                ? state.getValue(SnowLayerBlock.LAYERS) : 0;
    }

    public static boolean canAddSnow(BlockPlaceContext context, BlockState state) {
        return SnowSettings.mixing() && context.getItemInHand().is(Items.SNOW)
                && supported(state) && layers(state) < 8;
    }

    public static boolean hostSurvives(BlockState host, LevelReader level, BlockPos pos) {
        if (host.getBlock() instanceof DoublePlantBlock
                && host.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = raw(level.getBlockState(pos.below()));
            return below.is(host.getBlock()) && below.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER;
        }
        return host.canSurvive(level, pos);
    }

    public static BlockState doubleShape(BlockState host, net.minecraft.core.Direction direction,
                                         BlockState neighbor, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = host.getValue(DoublePlantBlock.HALF);
        boolean partner = direction == (half == DoubleBlockHalf.LOWER
                ? net.minecraft.core.Direction.UP : net.minecraft.core.Direction.DOWN);
        BlockState other = raw(neighbor);
        if (partner && (!other.is(host.getBlock()) || other.getValue(DoublePlantBlock.HALF) == half)) {
            return Blocks.AIR.defaultBlockState();
        }
        return hostSurvives(host, level, pos) ? host : Blocks.AIR.defaultBlockState();
    }
}
