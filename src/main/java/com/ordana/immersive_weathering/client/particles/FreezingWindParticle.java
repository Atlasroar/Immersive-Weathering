/*
 * Frostiful wind artwork/age animation by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see freezing-wind-NOTICE.txt.
 */
package com.ordana.immersive_weathering.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public final class FreezingWindParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private FreezingWindParticle(ClientLevel level, double x, double y, double z,
                                 double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, dx, dy, dz);
        this.sprites = sprites;
        xd = dx;
        yd = dy;
        zd = dz;
        lifetime = 12;
        quadSize = 0.6F;
        hasPhysics = true;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) { this.sprites = sprites; }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new FreezingWindParticle(level, x, y, z, dx, dy, dz, sprites);
        }
    }
}
