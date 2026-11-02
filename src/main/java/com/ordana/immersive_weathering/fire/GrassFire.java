package com.ordana.immersive_weathering.fire;

import com.ordana.immersive_weathering.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class GrassFire {
    private GrassFire() {}

    public static int odds(BlockState state, int original, int grassOdds) {
        return CommonConfigs.FIRE_FLAMMABLE_GRASS.get() && state.is(Blocks.GRASS_BLOCK)
                ? Math.max(original, grassOdds) : original;
    }

    public static boolean consume(Level level, BlockPos pos) {
        if (!CommonConfigs.FIRE_FLAMMABLE_GRASS.get() || !level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) return false;
        if (!level.isClientSide && level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
        return true;
    }
}
