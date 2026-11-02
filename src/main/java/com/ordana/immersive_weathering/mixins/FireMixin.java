package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.reg.ModTags;
import com.ordana.immersive_weathering.fire.GrassFire;
import com.ordana.immersive_weathering.fire.ThunderFire;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.ordana.immersive_weathering.util.WeatheringHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(value = FireBlock.class, priority = 900)
public abstract class FireMixin extends BaseFireBlock{

    protected FireMixin(Properties settings, float damage) {
        super(settings, damage);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void thunderExtinguish(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (ThunderFire.extinguish(level, pos, random)) ci.cancel();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/FireBlock;checkBurnOut(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/util/RandomSource;I)V"))
    private void thunderBurn(FireBlock fire, Level level, BlockPos target, int chance, RandomSource random, int age,
                             Operation<Void> original, @Local(argsOnly = true) BlockPos source) {
        if (ThunderFire.allowSpread(level, source, random)) original.call(fire, level, target, chance, random, age);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean thunderSpread(ServerLevel level, BlockPos target, BlockState state, int flags,
                                  Operation<Boolean> original, @Local(argsOnly = true) BlockPos source,
                                  @Local(argsOnly = true) RandomSource random) {
        return (target.equals(source) || !state.is(net.minecraft.world.level.block.Blocks.FIRE)
                || ThunderFire.allowSpread(level, source, random)) && original.call(level, target, state, flags);
    }

    //expired fire turns into soot
    @Inject(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z",
                    ordinal = 3,
                    shift = At.Shift.AFTER))
    private void removeBlock(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random, CallbackInfo ci) {
        WeatheringHelper.onFireExpired(serverLevel, pos, state);
    }

    //fire can replace soot
    @Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I",
            at = @At(value = "HEAD"), cancellable = true)
    private void canFireReplace(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (reader.getBlockState(pos).is(ModTags.FIRE_REPLACEABLE)) cir.setReturnValue(0);
    }

    @Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I",
            at = @At("RETURN"), cancellable = true)
    private void grassIgnition(BlockState state, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GrassFire.odds(state, cir.getReturnValue(), 5));
    }

    @Inject(method = "getBurnOdds", at = @At("RETURN"), cancellable = true)
    private void grassBurning(BlockState state, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GrassFire.odds(state, cir.getReturnValue(), 20));
    }

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            ordinal = 1), cancellable = true)
    private void preserveGrassGround(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        if (GrassFire.consume(level, pos)) ci.cancel();
    }
}