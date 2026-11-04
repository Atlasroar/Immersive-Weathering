package com.ordana.immersive_weathering.piles;

import com.ordana.immersive_weathering.sand.SandCoveredBlock;
import com.ordana.immersive_weathering.sand.SandHosts;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/** Retain existing covers during native plant writes, never during explicit pile removal. */
public final class PileInteractions {
    private static final ThreadLocal<Level> ACTIVE = new ThreadLocal<>();

    public static <T> T call(Level level, Supplier<T> action) {
        Level previous = ACTIVE.get();
        ACTIVE.set(level);
        try {
            return action.get();
        } finally {
            if (previous == null) ACTIVE.remove();
            else ACTIVE.set(previous);
        }
    }

    public static void run(Level level, Runnable action) {
        call(level, () -> { action.run(); return null; });
    }

    public static BlockState retain(Level level, BlockPos pos, BlockState next) {
        if (ACTIVE.get() != level || next.getBlock() instanceof PileCoveredBlock) return next;
        BlockState old = level.getBlockState(pos);
        if (!(old.getBlock() instanceof PileCoveredBlock)) return next;
        int layers = old.getValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS);
        BlockState covered = old.getBlock() instanceof SandCoveredBlock sand
                ? SandHosts.cover(next, sand.pile(), layers) : SnowHosts.cover(next, layers);
        return covered == null ? next : covered;
    }

    private PileInteractions() {}
}
