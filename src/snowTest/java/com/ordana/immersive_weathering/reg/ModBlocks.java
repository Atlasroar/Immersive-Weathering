package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.blocks.DuneGrassBlock;
import com.ordana.immersive_weathering.blocks.WeedsBlock;
import com.ordana.immersive_weathering.blocks.frosted.FrostyGrassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Substitute registration only; the plants themselves are production classes. */
public final class ModBlocks {
    private static final List<Runnable> REGISTRATIONS = new ArrayList<>();
    private static boolean plantsResolved;

    private static <T> Supplier<T> fixed(T value) { return () -> value; }
    private static <T> Supplier<T> plant(T value) {
        return () -> {
            if (!plantsResolved) throw new IllegalStateException("Plant supplier resolved before registration");
            return value;
        };
    }

    public static final Supplier<Block> WEEDS = plant(new WeedsBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)));
    public static final Supplier<Block> DUNE_GRASS = plant(new DuneGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)));
    public static final Supplier<Block> FROSTY_GRASS = plant(new FrostyGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS).randomTicks()));
    public static final Supplier<Block> FROSTY_FERN = plant(new FrostyGrassBlock(BlockBehaviour.Properties.copy(Blocks.FERN).randomTicks()));
    public static final Supplier<com.ordana.immersive_weathering.blocks.IvyBlock> IVY =
            plant(new com.ordana.immersive_weathering.blocks.IvyBlock(BlockBehaviour.Properties.copy(Blocks.VINE)));
    public static final Supplier<Block> MOSS =
            plant(new com.ordana.immersive_weathering.blocks.MossMultifaceBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN)));
    public static final Supplier<Block> HANGING_ROOTS_WALL =
            plant(new com.ordana.immersive_weathering.blocks.WallRootsBlock(BlockBehaviour.Properties.copy(Blocks.HANGING_ROOTS)));
    public static final Supplier<Block> FROST = fixed(Blocks.AIR);
    public static final Supplier<Block> FROSTY_GLASS = fixed(Blocks.GLASS);
    public static final Supplier<Block> FROSTY_GLASS_PANE = fixed(Blocks.GLASS_PANE);

    public static void resolvePlants() { plantsResolved = true; }

    public static <T extends Block> Supplier<T> regBlock(String name, Supplier<T> factory) {
        class Deferred implements Supplier<T> {
            private T block;
            @Override public T get() {
                if (block == null) throw new IllegalStateException("Snow supplier resolved before registration: " + name);
                return block;
            }
            void register() {
                block = net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK,
                        "immersive_weathering:" + name, factory.get());
            }
        }
        Deferred deferred = new Deferred();
        REGISTRATIONS.add(deferred::register);
        return deferred;
    }

    public static void registerSnowBlocks() {
        REGISTRATIONS.forEach(Runnable::run);
        REGISTRATIONS.clear();
    }
}
