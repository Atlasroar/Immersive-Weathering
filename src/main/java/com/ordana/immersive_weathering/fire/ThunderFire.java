package com.ordana.immersive_weathering.fire;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class ThunderFire {
    public static boolean affected(Level level, BlockPos pos) {
        return CommonConfigs.FIRE_THUNDER_DAMPENING.get() && level.isThundering()
                && level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK) && level.canSeeSky(pos);
    }

    public static double multiplier(Level level, BlockPos pos) {
        return affected(level, pos) ? CommonConfigs.FIRE_THUNDER_SPREAD_MULTIPLIER.get() : 1;
    }

    public static boolean allowSpread(Level level, BlockPos pos, RandomSource random) {
        double multiplier = multiplier(level, pos);
        return multiplier >= 1 || multiplier > 0 && random.nextDouble() < multiplier;
    }

    public static boolean extinguish(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!affected(level, pos) || !level.getBlockState(pos).is(Blocks.FIRE)) return false;
        double chance = CommonConfigs.FIRE_THUNDER_EXTINGUISH_CHANCE.get();
        return chance > 0 && random.nextDouble() < chance && level.removeBlock(pos, false);
    }

    private ThunderFire() {}
}
