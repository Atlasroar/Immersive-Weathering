/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt for pinned source and changes.
 */
package com.ordana.immersive_weathering.sandstorm;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

public final class Sandstorms {
    public enum Type { NONE, REGULAR, RED }

    public static final TagKey<Biome> REGULAR = TagKey.create(Registries.BIOME, ImmersiveWeathering.res("regular_sandstorms"));
    public static final TagKey<Biome> RED = TagKey.create(Registries.BIOME, ImmersiveWeathering.res("red_sandstorms"));
    public static final TagKey<EntityType<?>> IMMUNE = TagKey.create(Registries.ENTITY_TYPE, ImmersiveWeathering.res("sandstorm_slow_immune"));

    public static boolean ownsStorms() {
        return !FabricLoader.getInstance().isModLoaded("scorchful");
    }

    public static Type biomeType(Holder<Biome> biome) {
        if (biome.value().hasPrecipitation()) return Type.NONE;
        if (biome.is(REGULAR)) return Type.REGULAR;
        return biome.is(RED) ? Type.RED : Type.NONE;
    }

    public static boolean visualStorm(Level level, BlockPos pos) {
        return level.isRaining() && biomeType(level.getBiome(pos)) != Type.NONE;
    }

    public static Type exposedStorm(Level level, BlockPos pos) {
        if (!level.isRaining() || !level.canSeeSky(pos)
                || level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) > pos.getY()) return Type.NONE;
        return biomeType(level.getBiome(pos));
    }

    private Sandstorms() {}
}
