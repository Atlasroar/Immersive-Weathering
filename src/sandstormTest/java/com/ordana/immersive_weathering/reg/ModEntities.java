package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.entities.FallingLayerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

/** Replaces platform registration, not production falling-layer behavior. */
public final class ModEntities {
    private static final EntityType<FallingLayerEntity> TYPE = EntityType.Builder
            .<FallingLayerEntity>of(FallingLayerEntity::new, MobCategory.MISC).sized(0.98f, 0.98f).build("iw_sand_test");
    public static final Supplier<EntityType<FallingLayerEntity>> FALLING_LAYER = () -> TYPE;
}
