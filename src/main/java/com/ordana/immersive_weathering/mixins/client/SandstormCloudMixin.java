package com.ordana.immersive_weathering.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ordana.immersive_weathering.client.SandstormEffects;
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
public abstract class SandstormCloudMixin {
    @Shadow @Nullable private ClientLevel level;

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void sandstormClouds(PoseStack pose, Matrix4f projection, float partialTick,
                                 double x, double y, double z, CallbackInfo ci) {
        if (level != null && SandstormEffects.hideClouds(level, BlockPos.containing(x, y, z), partialTick)) ci.cancel();
    }
}
