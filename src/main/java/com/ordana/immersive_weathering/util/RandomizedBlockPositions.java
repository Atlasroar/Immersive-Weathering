package com.ordana.immersive_weathering.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Invocation-local traversal in the same order as the shuffled Manhattan list.
 * Only immutable, world-independent geometry is retained.
 */
public final class RandomizedBlockPositions {
    private static final int MAX_ENTRIES = 32;
    private static final int MAX_POSITIONS = 4096;
    private static final Map<Radii, BlockPos[]> TEMPLATES = new HashMap<>();
    private static int retainedPositions;

    private RandomizedBlockPositions() {
    }

    public static Iterator<BlockPos> iterator(BlockPos center, int radiusX, int radiusY, int radiusZ) {
        BlockPos[] offsets = template(radiusX, radiusY, radiusZ);
        if (offsets == null) {
            return WeatheringHelper.grabBlocksAroundRandomly(center, radiusX, radiusY, radiusZ).iterator();
        }
        int[] order = new int[offsets.length];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Random random = new Random(Mth.getSeed(center));
        // Match Collections.shuffle's backwards Fisher-Yates calls exactly.
        for (int i = order.length; i > 1; i--) {
            int other = random.nextInt(i);
            int previous = order[i - 1];
            order[i - 1] = order[other];
            order[other] = previous;
        }
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < order.length;
            }

            @Override
            public BlockPos next() {
                if (!hasNext()) throw new NoSuchElementException();
                BlockPos offset = offsets[order[index++]];
                return new BlockPos(x + offset.getX(), y + offset.getY(), z + offset.getZ());
            }
        };
    }

    private static synchronized BlockPos[] template(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 ||
                x > MAX_POSITIONS || y > MAX_POSITIONS || z > MAX_POSITIONS) return null;
        long size = (2L * x + 1) * (2L * y + 1) * (2L * z + 1);
        if (size > MAX_POSITIONS) return null;
        Radii key = new Radii(x, y, z);
        BlockPos[] existing = TEMPLATES.get(key);
        if (existing != null) return existing;
        if (TEMPLATES.size() >= MAX_ENTRIES || retainedPositions + size > MAX_POSITIONS) return null;
        BlockPos[] offsets = BlockPos.withinManhattanStream(BlockPos.ZERO, x, y, z)
                .map(BlockPos::new).toArray(BlockPos[]::new);
        TEMPLATES.put(key, offsets);
        retainedPositions += offsets.length;
        return offsets;
    }

    private record Radii(int x, int y, int z) {
    }
}
