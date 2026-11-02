package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.fire.EntityFire;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractArrow.class)
public abstract class ArrowFireMixin {
    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean burningArrow(Entity target, DamageSource source, float damage) {
        boolean success = target.hurt(source, damage);
        EntityFire.afterProjectileHit((AbstractArrow) (Object) this, target, success);
        return success;
    }
}
