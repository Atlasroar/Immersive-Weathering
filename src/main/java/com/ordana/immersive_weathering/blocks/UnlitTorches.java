package com.ordana.immersive_weathering.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

final class UnlitTorches {
    private UnlitTorches() {}

    static InteractionResult relight(BlockState lit, Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (!player.mayUseItemAt(pos, net.minecraft.core.Direction.UP, item)
                || !(item.is(Items.FLINT_AND_STEEL) || item.is(Items.FIRE_CHARGE)
                || item.is(Items.TORCH) || item.is(Items.SOUL_TORCH))) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (!level.setBlockAndUpdate(pos, lit)) return InteractionResult.FAIL;
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, 1);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (!player.getAbilities().instabuild) {
                if (item.is(Items.FLINT_AND_STEEL)) item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                else if (item.is(Items.FIRE_CHARGE)) item.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
