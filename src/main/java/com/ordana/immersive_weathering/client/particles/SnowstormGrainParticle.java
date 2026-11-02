/*
 * Small velocity/gravity adaptation from TheDeathlyCow's Immersive Storms, LGPL-3.0.
 * Source: commit 66c883b5c1facba2219c9a31e1f93acfe2e333c9,
 * src/main/java/com/thedeathlycow/immersive/storms/client/particle/DustGrainParticle.java.
 * Modified for IW on 2026-11-02: 1.20.1 particle API, simple registration and pale snow color.
 * Pinned source and changes: freezing-wind-NOTICE.txt.
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

public final class SnowstormGrainParticle extends DustParticleBase<DustParticleOptions> {
    private static final DustParticleOptions OPTIONS = new DustParticleOptions(new Vector3f(0.9F, 0.95F, 1F), 4);

    private SnowstormGrainParticle(ClientLevel level, double x, double y, double z,
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
            return new SnowstormGrainParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
