package net.mehvahdjukaar.moonlight.api.platform;

import net.minecraft.world.level.block.Block;

import java.util.IdentityHashMap;
import java.util.Map;

/** Capture the platform registration boundary; the standalone harness has no Fabric fire mixins. */
public final class RegHelper {
    public record FireEntry(int spread, int flammability) {}
    private static final Map<Block, FireEntry> FIRE = new IdentityHashMap<>();

    public static void registerBlockFlammability(Block block, int spread, int flammability) {
        FIRE.put(block, new FireEntry(spread, flammability));
    }

    public static FireEntry registeredFire(Block block) {
        return java.util.Objects.requireNonNull(FIRE.get(block), "Missing fire registration");
    }
}
