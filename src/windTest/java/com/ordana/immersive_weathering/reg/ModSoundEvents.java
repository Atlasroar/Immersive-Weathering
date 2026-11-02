package com.ordana.immersive_weathering.reg;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import java.util.function.Supplier;

public final class ModSoundEvents {
    public static final Supplier<SoundEvent> WEATHER_SANDSTORM = () -> SoundEvents.WOOL_HIT;
    public static final Supplier<SoundEvent> ICICLE_CRACK = () -> SoundEvents.GLASS_BREAK;
}
