package com.ordana.immersive_weathering.fire;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.mixins.accessors.FireBlockInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class EntityFire {
    private static final ThreadLocal<Boolean> PLACING_FIRE = new ThreadLocal<>();

    private EntityFire() {}

    public static void authorize(Entity entity) {
        if (!entity.level().isClientSide && !entity.fireImmune()) {
            ((FireCarrier) entity).immersiveWeathering$ignitionHistory().authorize(entity.getRemainingFireTicks());
        }
    }

    public static boolean authorized(Entity entity) {
        return ((FireCarrier) entity).immersiveWeathering$ignitionHistory().canSpread(entity.getRemainingFireTicks());
    }

    public static void afterMeleeHit(LivingEntity attacker, Entity target, boolean success) {
        if (!success || attacker.level().isClientSide) return;
        if (net.minecraft.world.item.enchantment.EnchantmentHelper.getFireAspect(attacker) > 0) authorize(target);
        if (attacker instanceof Player player) torchHit(player, target);
    }

    public static void afterProjectileHit(Projectile projectile, Entity target, boolean success) {
        if (success && (projectile instanceof SmallFireball
                || projectile instanceof AbstractArrow && projectile.isOnFire() && target.getType() != EntityType.ENDERMAN)) {
            authorize(target);
        }
    }

    public static boolean torchHit(Player player, Entity target) {
        if (player.level().isClientSide || !CommonConfigs.FIRE_TORCH_ATTACKS.get()
                || !(target instanceof LivingEntity living) || !living.isAlive() || living.fireImmune()
                || living.hasEffect(MobEffects.FIRE_RESISTANCE) || living.isInWaterRainOrBubble()
                || living.isInPowderSnow || living.wasInPowderSnow) return false;
        var stack = player.getMainHandItem();
        if (!stack.is(Items.TORCH) && !stack.is(Items.SOUL_TORCH)) return false;
        living.setSecondsOnFire(4);
        if (living.getRemainingFireTicks() <= 0) return false;
        authorize(living);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return true;
    }

    public static void tick(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !CommonConfigs.FIRE_ENTITY_SPREAD.get()
                || !entity.isAlive() || entity.fireImmune() || !authorized(entity)
                || entity.isInWaterRainOrBubble() || entity.isInPowderSnow || entity.wasInPowderSnow
                || !level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)
                || !mayGrief(entity, level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING))) return;
        int interval = CommonConfigs.FIRE_SPREAD_INTERVAL.get();
        if (!FireSettings.isDue(entity.tickCount, entity.getId(), interval)) return;
        spread(level, entity.blockPosition(), entity instanceof Projectile, FireSettings.current());
    }

    public static boolean mayGrief(Entity entity, boolean mobGriefing) {
        return mobGriefing || entity instanceof Player
                || entity instanceof Projectile projectile && projectile.getOwner() instanceof Player;
    }

    public static boolean isHeatSource(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.LAVA);
    }

    public static boolean accessible(Level level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(pos) && level.isLoaded(pos);
    }

    public static int heatSources(Level level, BlockPos origin, FireSettings settings) {
        int sources = 0;
        int radius = settings.heatRadius();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            if (accessible(level, pos) && isHeatSource(level.getBlockState(pos)) && ++sources >= settings.sourceCap()) break;
        }
        return sources;
    }

    public static boolean canIgnite(Level level, BlockPos pos) {
        if (!accessible(level, pos)) return false;
        for (Direction direction : Direction.values()) {
            if (!accessible(level, pos.relative(direction))) return false;
        }
        if (!level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) return false;
        if (level.isRainingAt(pos)) return false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.isRainingAt(pos.relative(direction))) return false;
        }
        return ((FireBlockInvoker) Blocks.FIRE).immersiveWeathering$igniteOdds(level, pos) > 0
                && BaseFireBlock.getState(level, pos).canSurvive(level, pos);
    }

    public static boolean spread(ServerLevel level, BlockPos origin, boolean projectile, FireSettings settings) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)
                || settings.baseChance() == 0 || settings.chanceCap() == 0
                || projectile && settings.projectileMultiplier() == 0 || !accessible(level, origin)) return false;
        double chance = settings.chance(heatSources(level, origin, settings),
                level.getBiome(origin).value().getBaseTemperature(), projectile) * ThunderFire.multiplier(level, origin);
        if (level.random.nextDouble() >= chance) return false;
        BlockPos chosen = null;
        int candidates = 0;
        int radius = settings.spreadRadius();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            if (canIgnite(level, pos) && level.random.nextInt(++candidates) == 0) chosen = pos.immutable();
        }
        return chosen != null && placeFire(level, chosen);
    }

    public static boolean placingFire() {
        return Boolean.TRUE.equals(PLACING_FIRE.get());
    }

    public static boolean placeFire(Level level, BlockPos pos) {
        boolean previous = placingFire();
        PLACING_FIRE.set(true);
        try {
            return level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
        } finally {
            if (previous) PLACING_FIRE.set(true);
            else PLACING_FIRE.remove();
        }
    }
}
