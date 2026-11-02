package com.ordana.immersive_weathering.reg;

import net.minecraft.core.particles.SimpleParticleType;
import java.util.function.Supplier;

/** Registration substitute only; retains vanilla particle limiting (overrideLimiter=false). */
public final class ModParticles {
    public static final SimpleParticleType TYPE = new SimpleParticleType(false) {};
    public static final Supplier<SimpleParticleType> DUST_GRAIN = () -> TYPE;
}
