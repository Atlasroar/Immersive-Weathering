package com.ordana.immersive_weathering.data.block_growths.growths;

import com.mojang.serialization.Codec;
import com.ordana.immersive_weathering.data.block_growths.TickSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Test-only possible-block fixture, without IW registry/configuration initialization. */
public final class ConfigurableBlockGrowth implements IBlockGrowth {
    public static final Codec<ConfigurableBlockGrowth> CODEC =
            Codec.unit(new ConfigurableBlockGrowth(Set.of()));
    private final Set<Block> possibleBlocks;

    public ConfigurableBlockGrowth(Set<Block> possibleBlocks) {
        this.possibleBlocks = possibleBlocks;
    }

    public Set<Block> getPossibleBlocks() {
        return possibleBlocks;
    }

    @Override
    public Iterable<? extends Block> getOwners() {
        return null;
    }

    @Override
    public Collection<TickSource> getTickSources() {
        return List.of();
    }

    @Override
    public void tryGrowing(BlockPos pos, BlockState state, ServerLevel level, Supplier<Holder<Biome>> biome) {
        throw new AssertionError("Fixture must not dispatch configurable growths");
    }
}
