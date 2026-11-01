package com.ordana.immersive_weathering.configs;

import java.util.List;
import java.util.function.Supplier;

/** Test-only substitute for loader-owned configuration initialization. */
public final class CommonConfigs {
    public static boolean enabled = true;
    public static final Supplier<Boolean> BLOCK_GROWTHS = () -> enabled;
    public static final Supplier<List<String>> DISABLED_GROWTHS = List::of;
}
