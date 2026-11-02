package com.ordana.immersive_weathering.mixins;

import com.ordana.immersive_weathering.fire.EntityFire;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireMixin {
    @Inject(method = "entityInside", at = @At("TAIL"))
    private void fireContact(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        EntityFire.authorize(entity);
    }

    @Redirect(method = "onPlace", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/portal/PortalShape;findEmptyPortalShape(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction$Axis;)Ljava/util/Optional;"))
    private Optional<PortalShape> noEntityPortals(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        return EntityFire.placingFire() ? Optional.empty() : PortalShape.findEmptyPortalShape(level, pos, axis);
    }
}
