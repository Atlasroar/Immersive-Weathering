/*
 * Immersive Storms presentation adaptation by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; pinned source and changes: freezing-wind-NOTICE.txt.
 */
package com.ordana.immersive_weathering.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ordana.immersive_weathering.configs.ClientConfigs;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModParticles;
import com.ordana.immersive_weathering.reg.ModSoundEvents;
import com.ordana.immersive_weathering.snowstorm.Snowstorms;
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

public final class SnowstormEffects {
    private static final Vec3 COLOR = Vec3.fromRGB24(0xDCE8EF);
    private static final SoundTimer SOUND = new SoundTimer();

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) reset();
        });
        ClientTickEvents.END_WORLD_TICK.register(SnowstormEffects::tick);
    }

    public static boolean clearCamera(Camera camera) {
        return camera.getEntity() != null && camera.getFluidInCamera() == FogType.NONE
                && (!(camera.getEntity() instanceof LivingEntity living)
                || !living.hasEffect(MobEffects.BLINDNESS) && !living.hasEffect(MobEffects.DARKNESS));
    }

    public static double stormBlend(ClientLevel level, Camera camera, float partialTick) {
        if (!Snowstorms.ownsPresentation() || !CommonConfigs.SNOWSTORM_ENABLED.get()
                || !ClientConfigs.SNOWSTORM_FOG.get() || !clearCamera(camera)
                || !level.isRaining() || !level.isThundering()) return 0;
        double strength = Math.min(level.getRainLevel(partialTick), level.getThunderLevel(partialTick));
        if (strength <= 0) return 0;
        return strength * CubicSampler.gaussianSampleVec3(camera.getPosition(), (x, y, z) -> {
            BlockPos pos = new BlockPos(x, y, z);
            return !level.isOutsideBuildHeight(pos) && level.hasChunkAt(pos)
                    && Snowstorms.snowyBiome(level.getBiome(pos).value(), pos)
                    ? new Vec3(1, 1, 1) : Vec3.ZERO;
        }).x;
    }

    public static Vec3 fogColor(ClientLevel level, Camera camera, float partialTick, Vec3 vanilla) {
        double blend = stormBlend(level, camera, partialTick);
        if (blend <= 0) return vanilla;
        float light = Mth.clamp(Mth.cos(level.getTimeOfDay(partialTick) * Mth.TWO_PI) * 2 + 0.5F, 0, 1);
        return vanilla.lerp(level.effects().getBrightnessDependentFogColor(COLOR, light), blend);
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
        Vec3 distances = fogDistances(start, end, viewDistance, blend,
                ClientConfigs.SNOWSTORM_FOG_START.get(), ClientConfigs.SNOWSTORM_FOG_END.get());
        RenderSystem.setShaderFogStart((float) distances.x);
        RenderSystem.setShaderFogEnd((float) distances.y);
        if (distances.y < end) RenderSystem.setShaderFogShape(FogShape.SPHERE);
    }

    public static Vec3 fogDistances(float start, float end, float viewDistance, double blend,
                                    double configuredStart, double configuredEnd) {
        float targetEnd = (float) Math.min(Math.max(0, viewDistance), configuredEnd);
        float targetStart = (float) Math.min(targetEnd, configuredStart);
        return new Vec3(Mth.lerp(Mth.clamp(blend, 0, 1), start, Math.min(start, targetStart)),
                Mth.lerp(Mth.clamp(blend, 0, 1), end, Math.min(end, targetEnd)), 0);
    }

    public static boolean hideClouds(ClientLevel level, BlockPos pos, float partialTick) {
        return Snowstorms.ownsPresentation() && ClientConfigs.SNOWSTORM_CLOUDS.get()
                && Math.min(level.getRainLevel(partialTick), level.getThunderLevel(partialTick)) > 0.75F
                && Snowstorms.visualStorm(level, pos);
    }

    public static int particleRarity(ParticleStatus status, int configured) {
        return status == ParticleStatus.MINIMAL ? 0
                : Math.max(1, Math.min(1200, configured)) * (status == ParticleStatus.DECREASED ? 2 : 1);
    }

    private static void tick(ClientLevel level) {
        Minecraft client = Minecraft.getInstance();
        SOUND.world(level);
        if (client.level != level || client.isPaused()) return;
        Camera camera = client.gameRenderer.getMainCamera();
        boolean enabled = Snowstorms.ownsPresentation() && CommonConfigs.SNOWSTORM_ENABLED.get()
                && clearCamera(camera) && level.isRaining() && level.isThundering();
        if (!enabled) {
            SOUND.reset();
            return;
        }
        if (ClientConfigs.SNOWSTORM_PARTICLES.get()) {
            particles(level, camera.getBlockPosition(), client.options.particles().get());
        }
        if (!ClientConfigs.SNOWSTORM_SOUNDS.get()) {
            SOUND.reset();
            return;
        }
        if (!SOUND.ready(80 + level.random.nextInt(80))) return;
        BlockPos cameraPos = camera.getBlockPosition();
        BlockPos sample = cameraPos.offset(level.random.nextInt(21) - 10, 0, level.random.nextInt(21) - 10);
        if (!level.hasChunkAt(sample)) return;
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, sample);
        if (Math.abs(top.getY() - cameraPos.getY()) <= 10 && Snowstorms.isSnowstorm(level, top)) {
            // Reuse IW's existing wind event and recordings; no new audio dependency.
            level.playLocalSound(top.getX() + 0.5, top.getY() + 0.5, top.getZ() + 0.5,
                    ModSoundEvents.WEATHER_SANDSTORM.get(), SoundSource.WEATHER, 0.1F, 0.7F, false);
        }
    }

    private static void particles(ClientLevel level, BlockPos camera, ParticleStatus status) {
        int rarity = particleRarity(status, ClientConfigs.SNOWSTORM_PARTICLE_RARITY.get());
        if (rarity == 0) return;
        int range = Mth.clamp(ClientConfigs.SNOWSTORM_PARTICLE_RANGE.get(), 0, 32);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = camera.getX() - range; x < camera.getX() + range; x++) {
            for (int z = camera.getZ() - range; z < camera.getZ() + range; z++) {
                pos.set(x, camera.getY(), z);
                if (!level.hasChunkAt(pos) || level.random.nextInt(rarity) != 0) continue;
                int y = Math.max(camera.getY() + level.random.nextInt(range + 1) - range / 2,
                        level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z));
                pos.setY(y);
                if (Snowstorms.isSnowstorm(level, pos) && level.getFluidState(pos).isEmpty()) {
                    level.addParticle(ModParticles.SNOWSTORM_GRAIN.get(), x + level.random.nextDouble(),
                            y + level.random.nextDouble(), z + level.random.nextDouble(), -0.65, -0.08, 0);
                }
            }
        }
    }

    private static void reset() {
        SOUND.reset();
    }

    /** One-shot audio has no retained sound handle; only this world-scoped cooldown persists. */
    public static final class SoundTimer {
        private Object world;
        private int cooldown;

        public void world(Object current) {
            if (world != current) {
                reset();
                world = current;
            }
        }

        public boolean ready(int delay) {
            if (cooldown > 0) {
                cooldown--;
                return false;
            }
            cooldown = Math.max(0, delay);
            return true;
        }

        public void reset() {
            world = null;
            cooldown = 0;
        }
    }

    private SnowstormEffects() {}
}
