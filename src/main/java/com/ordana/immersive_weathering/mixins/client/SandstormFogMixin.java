package com.ordana.immersive_weathering.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.ordana.immersive_weathering.client.SandstormEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class SandstormFogMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void sandstormColor(Camera camera, float partialTick, ClientLevel level, int distance,
                                       float darken, CallbackInfo ci) {
        Vec3 color = SandstormEffects.fogColor(level, camera, partialTick, new Vec3(fogRed, fogGreen, fogBlue));
        fogRed = (float) color.x;
        fogGreen = (float) color.y;
        fogBlue = (float) color.z;
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void sandstormDistance(Camera camera, FogRenderer.FogMode mode, float distance,
                                          boolean thickFog, float partialTick, CallbackInfo ci) {
        SandstormEffects.fogDistance(camera, mode, distance, thickFog, partialTick);
    }
}
