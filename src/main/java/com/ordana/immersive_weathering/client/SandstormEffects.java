/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt.
 */
package com.ordana.immersive_weathering.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ordana.immersive_weathering.configs.ClientConfigs;
import com.ordana.immersive_weathering.reg.ModParticles;
import com.ordana.immersive_weathering.reg.ModSoundEvents;
import com.ordana.immersive_weathering.sandstorm.Sandstorms;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;

public final class SandstormEffects {
    public static final Vec3 COLOR = Vec3.fromRGB24(0xD9AA84);
    private static final WindTimer WIND = new WindTimer();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) WIND.reset();
        });
        ClientTickEvents.END_WORLD_TICK.register(SandstormEffects::tick);
    }

    public static boolean clearCamera(Camera camera) {
        return camera.getFluidInCamera() == FogType.NONE
                && (!(camera.getEntity() instanceof LivingEntity living)
                || (!living.hasEffect(MobEffects.BLINDNESS) && !living.hasEffect(MobEffects.DARKNESS)));
    }

    public static double stormBlend(ClientLevel level, Camera camera, float partialTick) {
        if (!Sandstorms.ownsStorms() || !ClientConfigs.SANDSTORM_FOG.get() || !clearCamera(camera)) return 0;
        float rain = level.getRainLevel(partialTick);
        if (rain <= 0) return 0;
        return rain * CubicSampler.gaussianSampleVec3(camera.getPosition(), (x, y, z) ->
                Sandstorms.biomeType(level.getBiome(new BlockPos(x, y, z))) == Sandstorms.Type.NONE
                        ? Vec3.ZERO : new Vec3(1, 1, 1)).x;
    }

    public static Vec3 fogColor(ClientLevel level, Camera camera, float partialTick, Vec3 vanilla) {
        double blend = stormBlend(level, camera, partialTick);
        if (blend <= 0) return vanilla;
        float light = Mth.clamp(Mth.cos(level.getTimeOfDay(partialTick) * Mth.TWO_PI) * 2 + 0.5F, 0, 1);
        Vec3 dust = level.effects().getBrightnessDependentFogColor(COLOR, light);
        return vanilla.lerp(dust, blend);
    }

    public static void fogDistance(Camera camera, FogRenderer.FogMode mode, float viewDistance,
                                   boolean thickFog, float partialTick) {
        if (mode != FogRenderer.FogMode.FOG_TERRAIN || thickFog || !clearCamera(camera)) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        double blend = stormBlend(level, camera, partialTick);
        if (blend <= 0) return;
        float start = RenderSystem.getShaderFogStart();
        float end = RenderSystem.getShaderFogEnd();
        Vec3 distances = fogDistances(start, end, viewDistance, blend);
        RenderSystem.setShaderFogStart((float) distances.x);
        RenderSystem.setShaderFogEnd((float) distances.y);
        if (level.getRainLevel(partialTick) > 0.75F && distances.y < end) RenderSystem.setShaderFogShape(FogShape.SPHERE);
    }

    public static Vec3 fogDistances(float start, float end, float viewDistance, double blend) {
        float targetEnd = Math.min(viewDistance, ClientConfigs.SANDSTORM_FOG_END.get().floatValue());
        float targetStart = Math.min(targetEnd, ClientConfigs.SANDSTORM_FOG_START.get().floatValue());
        // Never weaken stronger vanilla/mod fog or extend the configured view distance.
        return new Vec3(Mth.lerp(blend, start, Math.min(start, targetStart)),
                Mth.lerp(blend, end, Math.min(end, targetEnd)), 0);
    }

    public static boolean hideClouds(ClientLevel level, BlockPos pos, float partialTick) {
        return Sandstorms.ownsStorms() && ClientConfigs.SANDSTORM_CLOUDS.get()
                && level.getRainLevel(partialTick) > 0.75F
                && Sandstorms.biomeType(level.getBiome(pos)) != Sandstorms.Type.NONE;
    }

    private static void tick(ClientLevel level) {
        Minecraft client = Minecraft.getInstance();
        if (client.isPaused()) return;
        boolean enabled = Sandstorms.ownsStorms() && level.isRaining();
        Camera camera = client.gameRenderer.getMainCamera();
        if (enabled && ClientConfigs.SANDSTORM_PARTICLES.get() && clearCamera(camera)) {
            particles(level, camera.getBlockPosition(), client.options.particles().get());
        }
        if (!WIND.tick(level, enabled && ClientConfigs.SANDSTORM_SOUNDS.get(), level.random.nextInt(3))) return;
        BlockPos cameraPos = camera.getBlockPosition();
        BlockPos sample = cameraPos.offset(level.random.nextInt(21) - 10, 0, level.random.nextInt(21) - 10);
        if (!level.hasChunkAt(sample)) return;
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, sample);
        if (Math.abs(top.getY() - cameraPos.getY()) <= 10 && Sandstorms.visualStorm(level, top)) {
            level.playLocalSound(top.getX() + 0.5, top.getY() + 0.5, top.getZ() + 0.5,
                    ModSoundEvents.WEATHER_SANDSTORM.get(), SoundSource.WEATHER, 0.1F, 0.5F, false);
        }
    }

    private static void particles(ClientLevel level, BlockPos camera, ParticleStatus status) {
        if (status == ParticleStatus.MINIMAL) return;
        int range = ClientConfigs.SANDSTORM_PARTICLE_RANGE.get();
        int rarity = ClientConfigs.SANDSTORM_PARTICLE_RARITY.get() * (status == ParticleStatus.DECREASED ? 2 : 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = camera.getX() - range; x < camera.getX() + range; x++) {
            for (int z = camera.getZ() - range; z < camera.getZ() + range; z++) {
                pos.set(x, camera.getY(), z);
                if (!level.hasChunkAt(pos) || level.random.nextInt(rarity) != 0) continue;
                int y = Math.max(camera.getY() + level.random.nextInt(range + 1) - range / 2,
                        level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z));
                pos.setY(y);
                if (!level.isOutsideBuildHeight(pos) && Sandstorms.visualStorm(level, pos)) {
                    level.addParticle(ModParticles.DUST_GRAIN.get(), x + level.random.nextDouble(),
                            y + level.random.nextDouble(), z + level.random.nextDouble(), -1, 0, 0);
                }
            }
        }
    }

    private SandstormEffects() {}
}
