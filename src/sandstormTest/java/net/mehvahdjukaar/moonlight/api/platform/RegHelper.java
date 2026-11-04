package net.mehvahdjukaar.moonlight.api.platform;

import net.minecraft.world.level.block.Block;

import java.util.IdentityHashMap;
import java.util.Map;

/** Capture registration; this native-state fixture does not load Fabric's fire mixins. */
public final class RegHelper {
    private static final Map<Block, int[]> FIRE = new IdentityHashMap<>();

    public static void registerBlockFlammability(Block block, int spread, int flammability) {
        FIRE.put(block, new int[]{spread, flammability});
    }
}
