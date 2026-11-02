package com.ordana.immersive_weathering.reg;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import java.util.function.Supplier;

public final class ModParticles {
    public static final Supplier<SimpleParticleType> FREEZING_WIND = () -> ParticleTypes.CLOUD;
    public static final Supplier<SimpleParticleType> FREEZING_WIND_FLIPPED = FREEZING_WIND;
}
