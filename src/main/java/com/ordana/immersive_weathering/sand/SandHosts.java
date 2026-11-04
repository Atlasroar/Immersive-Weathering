package com.ordana.immersive_weathering.sand;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModBlocks;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class SandHosts {
    private static final Map<Block, List<Supplier<SandCoveredBlock>>> HOSTS = new LinkedHashMap<>();

    public static List<Block> hosts() {
        return SnowHosts.hosts();
    }

    public static void init() {
        for (Block host : SnowHosts.vanillaHosts()) {
            String name = BuiltInRegistries.BLOCK.getKey(host).getPath();
            register(name, () -> host);
        }
        com.ordana.immersive_weathering.piles.PilePlants.immersive().entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).forEach(entry -> register(entry.getKey(), entry.getValue()));
    }

    private static void register(String name, Supplier<? extends Block> host) {
        for (String color : List.of("sand", "red_sand")) {
            ModBlocks.regBlock(color + "_covered_" + name, () -> {
                Block block = host.get();
                SandCoveredBlock covered = SandCoveredBlock.create(block, (SandLayerBlock)
                        (color.equals("sand") ? ModBlocks.SAND_LAYER_BLOCK.get() : ModBlocks.RED_SAND_LAYER_BLOCK.get()));
                HOSTS.computeIfAbsent(block, key -> new java.util.ArrayList<>()).add(() -> covered);
                return covered;
            });
        }
    }

    public static Map<Block, List<Supplier<SandCoveredBlock>>> entries() {
        return Collections.unmodifiableMap(HOSTS);
    }

    public static boolean supported(BlockState state) {
        return !state.hasBlockEntity() && state.getFluidState().isEmpty() && raw(state).getFluidState().isEmpty()
                && (state.getBlock() instanceof SandCoveredBlock || HOSTS.containsKey(state.getBlock()));
    }

    public static BlockState raw(BlockState state) {
        return com.ordana.immersive_weathering.piles.PilePlants.raw(state);
    }

    @Nullable
    public static SandLayerBlock pile(BlockState state) {
        if (state.getBlock() instanceof SandLayerBlock pile) return pile;
        return state.getBlock() instanceof SandCoveredBlock covered ? covered.pile() : null;
    }

    public static int layers(BlockState state) {
        return pile(state) == null ? 0 : state.getValue(LayerBlock.LAYERS_8);
    }

    @Nullable
    public static BlockState cover(BlockState state, SandLayerBlock pile, int layers) {
        if (layers < 1 || layers > 8 || !supported(state)) return null;
        if (state.getBlock() instanceof SandCoveredBlock covered) {
            return covered.pile() == pile ? state.setValue(LayerBlock.LAYERS_8, layers) : null;
        }
        for (Supplier<SandCoveredBlock> supplier : HOSTS.get(state.getBlock())) {
            SandCoveredBlock covered = supplier.get();
            if (covered.pile() != pile) continue;
            for (var property : state.getProperties()) {
                if (property != BlockStateProperties.WATERLOGGED && !covered.defaultBlockState().hasProperty(property)) {
                    throw new IllegalStateException("Sand cover cannot preserve " + property + " of " + state);
                }
            }
            return SnowHosts.copy(state, covered.defaultBlockState()).setValue(LayerBlock.LAYERS_8, layers);
        }
        throw new IllegalArgumentException("Unregistered sand pile: " + pile);
    }

    public static boolean canAdd(BlockPlaceContext context, BlockState state) {
        if (!CommonConfigs.SAND_HOSTS.get() || !supported(state) || layers(state) >= 8) return false;
        return context.getItemInHand().getItem() instanceof BlockItem item
                && item.getBlock() instanceof SandLayerBlock incoming
                && (pile(state) == null || pile(state) == incoming);
    }

    @Nullable
    public static BlockState placement(BlockPlaceContext context, @Nullable BlockState placement) {
        if (placement == null || !CommonConfigs.SAND_HOSTS.get()) return placement;
        BlockState old = context.getLevel().getBlockState(context.getClickedPos());
        SandLayerBlock pile = pile(old);
        if (pile == null || context.getItemInHand().is(pile.asItem())) return placement;
        BlockState host = raw(old);
        if (host.getBlock() instanceof net.minecraft.world.level.block.PinkPetalsBlock
                && placement.is(host.getBlock()) && old.canBeReplaced(context)) {
            placement = host.setValue(net.minecraft.world.level.block.PinkPetalsBlock.AMOUNT,
                    Math.min(4, host.getValue(net.minecraft.world.level.block.PinkPetalsBlock.AMOUNT) + 1));
        }
        BlockState covered = cover(placement, pile, layers(old));
        return covered == null ? placement : covered;
    }

    private SandHosts() {}
}
