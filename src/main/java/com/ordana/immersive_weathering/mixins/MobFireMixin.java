package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.fire.EntityFire;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobFireMixin {
    @Inject(method = "doHurtTarget", at = @At("RETURN"))
    private void fireAspect(Entity target, CallbackInfoReturnable<Boolean> cir) {
        EntityFire.afterMeleeHit((Mob) (Object) this, target, cir.getReturnValue());
    }
}
