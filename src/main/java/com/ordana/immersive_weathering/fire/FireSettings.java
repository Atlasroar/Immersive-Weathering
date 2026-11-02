package com.ordana.immersive_weathering.fire;

import com.ordana.immersive_weathering.configs.CommonConfigs;

public record FireSettings(int interval, int spreadRadius, int heatRadius, int sourceCap,
                           double baseChance, double projectileMultiplier, double heatBonus,
                           double temperatureBonus, double chanceCap) {
    public static FireSettings current() {
        return new FireSettings(CommonConfigs.FIRE_SPREAD_INTERVAL.get(), CommonConfigs.FIRE_SPREAD_RADIUS.get(),
                CommonConfigs.FIRE_HEAT_RADIUS.get(), CommonConfigs.FIRE_HEAT_SOURCE_CAP.get(),
                CommonConfigs.FIRE_SPREAD_CHANCE.get(), CommonConfigs.FIRE_PROJECTILE_MULTIPLIER.get(),
                CommonConfigs.FIRE_HEAT_BONUS.get(), CommonConfigs.FIRE_TEMPERATURE_BONUS.get(),
                CommonConfigs.FIRE_SPREAD_CHANCE_CAP.get());
    }

    public static boolean isDue(int ticks, int entityId, int interval) {
        return Math.floorMod(ticks, interval) == Math.floorMod(entityId, interval);
    }

    public double chance(int sources, float temperature, boolean projectile) {
        double heat = 1 + Math.max(0, Math.min(sourceCap, sources)) * heatBonus;
        double hot = 1 + Math.max(0, Math.min(2, temperature) - 0.8F) * temperatureBonus;
        double chance = Math.min(chanceCap, baseChance * heat * hot);
        return chance * (projectile ? projectileMultiplier : 1);
    }
}
