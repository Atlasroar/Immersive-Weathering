package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.sandstorm.SandstormSlowing;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SandstormLivingMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void sandstormSlow(CallbackInfo ci) {
        SandstormSlowing.tick((LivingEntity) (Object) this);
    }
}
