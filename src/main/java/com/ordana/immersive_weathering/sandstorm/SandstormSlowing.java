/*
 * Adapted from Scorchful by TheDeathlyCow, LGPL-3.0.
 * Modified for IW on 2026-11-02; see sandstorm-NOTICE.txt.
 */
package com.ordana.immersive_weathering.sandstorm;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class SandstormSlowing {
    public static final UUID ID = UUID.fromString("ddcbef92-e913-4994-9524-72e97d4eb92c");

    public static void tick(LivingEntity entity) {
        if (entity.level().isClientSide) return;
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        boolean active = Sandstorms.ownsStorms() && CommonConfigs.SANDSTORM_SLOWING.get()
                && !entity.getType().is(Sandstorms.IMMUNE)
                && Sandstorms.exposedStorm(entity.level(), entity.blockPosition()) != Sandstorms.Type.NONE;
        reconcile(speed, active, CommonConfigs.SANDSTORM_SLOW_AMOUNT.get());
    }

    public static void reconcile(AttributeInstance speed, boolean active, double amount) {
        AttributeModifier current = speed.getModifier(ID);
        double desired = active ? -amount : 0;
        if (current != null && (desired == 0 || current.getAmount() != desired
                || current.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)) {
            speed.removeModifier(ID);
            current = null;
        }
        if (desired != 0 && current == null) {
            speed.addTransientModifier(new AttributeModifier(ID, "IW sandstorm slow", desired,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    private SandstormSlowing() {}
}
