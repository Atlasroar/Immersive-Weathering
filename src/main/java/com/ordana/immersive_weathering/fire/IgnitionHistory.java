package com.ordana.immersive_weathering.fire;

import net.minecraft.nbt.CompoundTag;

public final class IgnitionHistory {
    private static final String KEY = "immersive_weathering:non_sun_fire";
    private boolean authorized;

    public void authorize(int fireTicks) {
        if (fireTicks > 0) authorized = true;
    }

    public void update(int fireTicks) {
        if (fireTicks <= 0) authorized = false;
    }

    public boolean canSpread(int fireTicks) {
        return authorized && fireTicks > 0;
    }

    public void save(CompoundTag tag, int fireTicks) {
        tag.putBoolean(KEY, canSpread(fireTicks));
    }

    public void load(CompoundTag tag, int fireTicks) {
        authorized = fireTicks > 0 && tag.getBoolean(KEY);
    }
}
