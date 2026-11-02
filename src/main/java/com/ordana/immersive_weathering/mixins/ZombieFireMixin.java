package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.fire.EntityFire;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Zombie.class)
public abstract class ZombieFireMixin {
    @Redirect(method = "doHurtTarget", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
    private void meleeIgnition(Entity target, int seconds) {
        target.setSecondsOnFire(seconds);
        if (seconds > 0 && EntityFire.authorized((Zombie) (Object) this)) EntityFire.authorize(target);
    }
}
