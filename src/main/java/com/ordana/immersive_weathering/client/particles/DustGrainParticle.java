/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt.
 */
package com.ordana.immersive_weathering.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3f;

public final class DustGrainParticle extends DustParticleBase<DustParticleOptions> {
    // Vanilla DustParticleOptions clamps the upstream requested scale of 10 to 4.
    private static final DustParticleOptions OPTIONS = new DustParticleOptions(new Vector3f(217 / 255F, 170 / 255F, 132 / 255F), 10);

    private DustGrainParticle(ClientLevel level, double x, double y, double z,
                              double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz, OPTIONS, sprites);
        xd = dx;
        yd = dy;
        zd = dz;
        gravity = 0.1F;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new DustGrainParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
