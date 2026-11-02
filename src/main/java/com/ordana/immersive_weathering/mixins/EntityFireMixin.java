package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.fire.EntityFire;
import com.ordana.immersive_weathering.fire.FireCarrier;
import com.ordana.immersive_weathering.fire.IgnitionHistory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityFireMixin implements FireCarrier {
    @Unique
    private final IgnitionHistory immersiveWeathering$fire = new IgnitionHistory();

    @Override
    public IgnitionHistory immersiveWeathering$ignitionHistory() {
        return immersiveWeathering$fire;
    }

    @Inject(method = "setRemainingFireTicks", at = @At("TAIL"))
    private void resetIgnition(int ticks, CallbackInfo ci) {
        if (!((Entity) (Object) this).level().isClientSide) immersiveWeathering$fire.update(ticks);
    }

    @Inject(method = "setSecondsOnFire", at = @At("TAIL"))
    private void projectileIgnition(int seconds, CallbackInfo ci) {
        // Vanilla sunlight never ignites a projectile. Living callers need explicit provenance.
        if ((Object) this instanceof Projectile && seconds > 0) EntityFire.authorize((Entity) (Object) this);
    }

    @Inject(method = "lavaHurt", at = @At("TAIL"))
    private void lavaIgnition(CallbackInfo ci) {
        EntityFire.authorize((Entity) (Object) this);
    }

    @Inject(method = "thunderHit", at = @At("TAIL"))
    private void lightningIgnition(ServerLevel level, LightningBolt bolt, CallbackInfo ci) {
        EntityFire.authorize((Entity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void spreadFire(CallbackInfo ci) {
        EntityFire.tick((Entity) (Object) this);
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void saveIgnition(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        Entity entity = (Entity) (Object) this;
        if (!entity.level().isClientSide) immersiveWeathering$fire.save(cir.getReturnValue(), entity.getRemainingFireTicks());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadIgnition(CompoundTag tag, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (!entity.level().isClientSide) immersiveWeathering$fire.load(tag, entity.getRemainingFireTicks());
    }
}
