/*
 * Adapted from Frostiful WindManager by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see freezing-wind-NOTICE.txt.
 */
package com.ordana.immersive_weathering.wind;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModEntities;
import com.ordana.immersive_weathering.snowstorm.Snowstorms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public final class FreezingWind {
    private static final Map<ServerLevel, Budget> BUDGETS = new WeakHashMap<>();

    private FreezingWind() {}

    public static void tickChunk(ServerLevel level, LevelChunk chunk) {
        if (!CommonConfigs.WIND_ENABLED.get() || !level.dimensionType().natural() || !level.dimensionType().hasSkyLight()
                || Math.floorMod(level.getGameTime() + chunk.getPos().toLong(), 20) != 0
                || CommonConfigs.WIND_SPAWN_BUDGET.get() == 0 || CommonConfigs.WIND_LIVE_CAP.get() == 0) return;
        Budget budget = BUDGETS.computeIfAbsent(level, key -> new Budget());
        long second = Math.floorDiv(level.getGameTime(), 20);
        if (budget.second != second) {
            budget.second = second;
            budget.spawned = 0;
        }
        budget.live.removeIf(ref -> {
            FreezingWindEntity gust = ref.get();
            return gust == null || gust.isRemoved();
        });
        if (budget.spawned >= CommonConfigs.WIND_SPAWN_BUDGET.get()
                || budget.live.size() >= CommonConfigs.WIND_LIVE_CAP.get()) return;
        int x = chunk.getPos().getMinBlockX() + level.random.nextInt(16);
        int z = chunk.getPos().getMinBlockZ() + level.random.nextInt(16);
        int surface = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x & 15, z & 15) + 1;
        int y = Math.min(surface + level.random.nextInt(9), level.getMaxBuildHeight() - 3);
        if (y < surface || y < level.getMinBuildHeight()) return;
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.hasChunksAt(pos.offset(-2, -1, -2), pos.offset(2, 3, 2)) || !level.canSeeSky(pos)) return;
        boolean storm = Snowstorms.isSnowstorm(level, pos);
        if (!level.isNight() && !storm) return;
        double chance = WindSettings.current().chance(level.getBiome(pos).value().getBaseTemperature(), y, storm);
        if (level.random.nextDouble() >= chance) return;
        FreezingWindEntity gust = ModEntities.FREEZING_WIND.get().create(level);
        if (gust == null) throw new IllegalStateException("Freezing wind entity factory returned null");
        gust.setPos(x + 0.5, y, z + 0.5);
        if (!level.noCollision(gust, gust.getBoundingBox()) || !level.getWorldBorder().isWithinBounds(gust.getBoundingBox())) return;
        if (level.addFreshEntity(gust)) {
            budget.spawned++;
            budget.live.add(new WeakReference<>(gust));
        }
    }

    public static boolean loaded(ServerLevel level, AABB box) {
        BlockPos min = BlockPos.containing(box.minX - 1, box.minY - 1, box.minZ - 1);
        BlockPos max = BlockPos.containing(box.maxX + 1, box.maxY + 1, box.maxZ + 1);
        return !level.isOutsideBuildHeight(min) && !level.isOutsideBuildHeight(max) && level.hasChunksAt(min, max);
    }

    private static final class Budget {
        private long second = Long.MIN_VALUE;
        private int spawned;
        private final ArrayList<WeakReference<FreezingWindEntity>> live = new ArrayList<>();
    }
}
