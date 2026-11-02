package com.ordana.immersive_weathering.reg;

import com.ordana.immersive_weathering.wind.FreezingWindEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import java.util.function.Supplier;

public final class ModEntities {
    private static final EntityType<FreezingWindEntity> TYPE = EntityType.Builder
            .<FreezingWindEntity>of(FreezingWindEntity::new, MobCategory.MISC).sized(2, 2).build("iw_wind:wind");
    public static final Supplier<EntityType<FreezingWindEntity>> FREEZING_WIND = () -> TYPE;
}
