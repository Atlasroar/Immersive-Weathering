package com.ordana.immersive_weathering.piles.integration.mixins;

import com.ordana.immersive_weathering.piles.PilePlacementIntegrationTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.Main.class)
public class PileIntegrationMixin {
    @Inject(method = "main", at = @At("RETURN"), remap = false)
    private static void verifyPiles(String[] args, CallbackInfo ci) throws Exception {
        if (!java.util.Arrays.asList(args).contains("--initSettings")) {
            throw new IllegalStateException("Pile integration tests require init-only server startup");
        }
        PilePlacementIntegrationTest.verify();
    }
}
