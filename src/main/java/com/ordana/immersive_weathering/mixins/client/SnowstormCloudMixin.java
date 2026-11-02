package com.ordana.immersive_weathering.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ordana.immersive_weathering.client.SnowstormEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class SnowstormCloudMixin {
    @Shadow @Nullable private ClientLevel level;

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void snowstormClouds(PoseStack pose, Matrix4f projection, float partialTick,
                                 double x, double y, double z, CallbackInfo ci) {
        if (level != null && SnowstormEffects.clearCamera(Minecraft.getInstance().gameRenderer.getMainCamera())
                && SnowstormEffects.hideClouds(level, BlockPos.containing(x, y, z), partialTick)) ci.cancel();
    }
}
