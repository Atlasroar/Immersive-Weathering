package com.ordana.immersive_weathering.sandstorm;

import com.ordana.immersive_weathering.piles.PileCoveredBlock;
import com.ordana.immersive_weathering.piles.PilePlants;
import com.ordana.immersive_weathering.sand.SandHosts;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class PilePerformanceBenchmark {
    private static volatile int sink;
    private static final int OPERATIONS = 1_000_000;

    static void run() {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!bean.isThreadAllocatedMemorySupported()) throw new IllegalStateException("Allocation measurement unavailable");
        bean.setThreadAllocatedMemoryEnabled(true);
        int states = 0;
        for (var entry : SnowHosts.entries().entrySet()) {
            List<PileCoveredBlock> covers = new ArrayList<>();
            covers.add(entry.getValue().get());
            SandHosts.entries().get(entry.getKey()).forEach(cover -> covers.add(cover.get()));
            for (PileCoveredBlock cover : covers) {
                for (BlockState state : cover.getStateDefinition().getPossibleStates()) {
                    if (PilePlants.raw(state) != baseline(state)) throw new AssertionError("Incorrect raw host " + state);
                    states++;
                }
            }
        }
        System.out.println("Raw-host identity verified for " + states + " registered mineral states");
        for (Block host : List.of(Blocks.GRASS, Blocks.WHEAT, Blocks.VINE, Blocks.COBBLESTONE_WALL)) {
            List<BlockState> all = new ArrayList<>(SnowHosts.entries().get(host).get().getStateDefinition().getPossibleStates());
            SandHosts.entries().get(host).forEach(cover -> all.addAll(cover.get().getStateDefinition().getPossibleStates()));
            BlockState[] workload = all.toArray(BlockState[]::new);
            long warmup = System.nanoTime() + 1_000_000_000L;
            do {
                measure(bean, workload, false);
                measure(bean, workload, true);
            } while (System.nanoTime() < warmup);
            double[][] time = new double[2][7], allocation = new double[2][7];
            for (int round = 0; round < 7; round++) {
                for (int order = 0; order < 2; order++) {
                    int variant = (round + order) & 1;
                    double[] measured = measure(bean, workload, variant == 1);
                    time[variant][round] = measured[0];
                    allocation[variant][round] = measured[1];
                }
            }
            if (median(allocation[1]) != 0) throw new AssertionError("Raw-host lookup allocated for " + host);
            System.out.printf(Locale.ROOT,
                    "%s property-copy=%.1f ns/op %.1f B/op production=%.1f ns/op %.1f B/op%n",
                    host, median(time[0]), median(allocation[0]), median(time[1]), median(allocation[1]));
        }
    }

    private static BlockState baseline(BlockState state) {
        return SnowHosts.copy(state, ((PileCoveredBlock) state.getBlock()).host().defaultBlockState());
    }

    private static double[] measure(com.sun.management.ThreadMXBean bean, BlockState[] states, boolean production) {
        long thread = Thread.currentThread().getId();
        long bytes = bean.getThreadAllocatedBytes(thread), start = System.nanoTime();
        int result = 0, index = 0;
        for (int i = 0; i < OPERATIONS; i++) {
            BlockState state = states[index];
            result ^= System.identityHashCode(production ? PilePlants.raw(state) : baseline(state));
            if (++index == states.length) index = 0;
        }
        long elapsed = System.nanoTime() - start, allocated = bean.getThreadAllocatedBytes(thread) - bytes;
        sink = result;
        return new double[]{(double) elapsed / OPERATIONS, (double) allocated / OPERATIONS};
    }

    private static double median(double[] values) {
        Arrays.sort(values);
        return values[values.length / 2];
    }

    private PilePerformanceBenchmark() {}
}
