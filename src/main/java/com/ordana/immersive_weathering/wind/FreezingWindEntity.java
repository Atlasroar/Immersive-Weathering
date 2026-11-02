/*
 * Adapted from Frostiful WindEntity/FreezingWindEntity by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see freezing-wind-NOTICE.txt.
 */
package com.ordana.immersive_weathering.wind;

import com.ordana.immersive_weathering.ImmersiveWeathering;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.reg.ModParticles;
import com.ordana.immersive_weathering.reg.ModSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FreezingWindEntity extends Entity {
    private static final EntityDataAccessor<Integer> LIFE = SynchedEntityData.defineId(FreezingWindEntity.class, EntityDataSerializers.INT);
    public static final TagKey<EntityType<?>> HEAVY = TagKey.create(Registries.ENTITY_TYPE, ImmersiveWeathering.res("wind_heavy"));

    public FreezingWindEntity(EntityType<? extends FreezingWindEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        if (!level.isClientSide) entityData.set(LIFE, 80 + random.nextInt(61));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(LIFE, 110);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (entityData.get(LIFE) > 0) {
                for (int i = 0; i < 2; i++) {
                    level().addParticle(random.nextBoolean() ? ModParticles.FREEZING_WIND.get() : ModParticles.FREEZING_WIND_FLIPPED.get(),
                            getRandomX(0.5), getRandomY(), getRandomZ(0.5), -0.15, 0, 0);
                    level().addParticle(ParticleTypes.SNOWFLAKE, getRandomX(1), getRandomY(), getRandomZ(1), -0.1, 0.1, 0);
                }
            }
            return;
        }
        ServerLevel level = (ServerLevel) level();
        if (!CommonConfigs.WIND_ENABLED.get() || !FreezingWind.loaded(level, getBoundingBox().inflate(2))
                || !level.getWorldBorder().isWithinBounds(getBoundingBox()) || entityData.get(LIFE) <= 0) {
            discard();
            return;
        }
        entityData.set(LIFE, entityData.get(LIFE) - 1);
        if (entityData.get(LIFE) == 0) {
            discard();
            return;
        }
        if (tickCount % 10 == 0) {
            Vec3 step = horizontalCollision ? new Vec3(0, 0.5, 0) : new Vec3(-1, 0, 0);
            move(MoverType.SELF, step);
        }
        if (tickCount % 30 == 0) playSound(ModSoundEvents.WEATHER_SANDSTORM.get(), 0.1F, 0.9F + random.nextFloat() / 3);
        if (tickCount % 5 == 0) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox(),
                    entity -> entity.isAlive() && !entity.isSpectator() && !entity.getType().is(HEAVY))) {
                if (!target.hasLineOfSight(this)) continue;
                target.push(target.isFallFlying() ? -1.75 : -0.16, 0, 0);
                target.hurtMarked = true;
                if (CommonConfigs.WIND_SLOW_TICKS.get() > 0) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        CommonConfigs.WIND_SLOW_TICKS.get(), CommonConfigs.WIND_SLOW_AMPLIFIER.get()));
            }
            WindBlocks.interact(level, this);
        }
    }

    @Override
    public boolean fireImmune() { return true; }

    @Override
    public net.minecraft.sounds.SoundSource getSoundSource() { return net.minecraft.sounds.SoundSource.WEATHER; }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Life", 3)) entityData.set(LIFE, Mth.clamp(tag.getInt("Life"), 0, 140));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", entityData.get(LIFE));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }
}
