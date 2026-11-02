/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt.
 */
package com.ordana.immersive_weathering.client;

/** No sound handles survive a world change; wind uses ordinary one-shot resource events. */
public final class WindTimer {
    private Object world;
    private int timer;

    public boolean tick(Object currentWorld, boolean enabled, int randomThree) {
        if (world != currentWorld) {
            reset();
            world = currentWorld;
        }
        if (!enabled) {
            timer = 0;
            return false;
        }
        if (randomThree >= timer) {
            timer++;
            return false;
        }
        timer = 0;
        return true;
    }

    public void reset() {
        world = null;
        timer = 0;
    }
}
