package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.fire.EntityFire;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerFireMixin {
    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", ordinal = 0))
    private boolean directMelee(Entity target, DamageSource source, float damage) {
        boolean success = target.hurt(source, damage);
        EntityFire.afterMeleeHit((Player) (Object) this, target, success);
        return success;
    }
}
