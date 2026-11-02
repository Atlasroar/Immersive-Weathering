package com.ordana.immersive_weathering.sand;

import com.ordana.immersive_weathering.blocks.LayerBlock;
import com.ordana.immersive_weathering.blocks.SandLayerBlock;
import com.ordana.immersive_weathering.configs.CommonConfigs;
import com.ordana.immersive_weathering.snow.SnowHosts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SandCoveredBlock extends Block {
    private final Block host;
    private final SandLayerBlock pile;

    private SandCoveredBlock(Block host, SandLayerBlock pile) {
        super(Properties.copy(host).noOcclusion().randomTicks().offsetType(OffsetType.NONE));
        this.host = host;
        this.pile = pile;
        registerDefaultState(SnowHosts.copy(host.defaultBlockState(), stateDefinition.any()).setValue(LayerBlock.LAYERS_8, 1));
    }

    public static SandCoveredBlock create(Block host, SandLayerBlock pile) {
        if (host instanceof FenceBlock) return new Fence(host, pile);
        if (host == Blocks.DEAD_BUSH) return new SandCoveredBlock(host, pile);
        throw new IllegalArgumentException("No sand state schema for " + host);
    }

    public Block host() { return host; }
    public SandLayerBlock pile() { return pile; }
    private BlockState raw(BlockState state) { return SandHosts.raw(state); }
    private BlockState sand(BlockState state) {
        return pile.defaultBlockState().setValue(LayerBlock.LAYERS_8, SandHosts.layers(state));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LayerBlock.LAYERS_8);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(raw(state).getShape(level, pos, context), sand(state).getShape(level, pos, context));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(raw(state).getCollisionShape(level, pos, context), sand(state).getCollisionShape(level, pos, context));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.or(raw(state).getBlockSupportShape(level, pos), sand(state).getBlockSupportShape(level, pos));
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) { return true; }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return raw(state).isPathfindable(level, pos, type) && sand(state).isPathfindable(level, pos, type);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return raw(state).canSurvive(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                  LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        BlockState updated = host.updateShape(raw(state), direction, neighbor, level, pos, otherPos);
        level.scheduleTick(pos, this, 2);
        return updated.is(host) ? SnowHosts.copy(updated, state) : state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!raw(state).canSurvive(level, pos)) {
            if (level.setBlockAndUpdate(pos, sand(state))) {
                Block.dropResources(raw(state), level, pos);
                SandPhysics.checkSupport(level, pos, sand(state));
            }
        } else SandPhysics.checkSupport(level, pos, state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SandWeather.erode(level, pos, state, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        host.animateTick(raw(state), level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        host.entityInside(raw(state), level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        pile.stepOn(level, pos, sand(state), entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float height) {
        pile.fallOn(level, sand(state), pos, entity, height);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (SandHosts.canAdd(context, state)) return true;
        return CommonConfigs.REPLACE_SAND_PILES.get()
                && context.getItemInHand().getItem() instanceof BlockItem item
                && !(item.getBlock() instanceof SandLayerBlock) && raw(state).canBeReplaced(context);
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) { return false; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof ShovelItem && player.mayBuild()) {
            if (!level.isClientSide) {
                if (!level.setBlockAndUpdate(pos, raw(state))) return InteractionResult.FAIL;
                if (!player.isCreative()) {
                    Block.dropResources(sand(state), level, pos, null, player, player.getItemInHand(hand));
                    player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
                level.levelEvent(player, 2001, pos, Block.getId(sand(state)));
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return host.use(raw(state), level, pos, player, hand, hit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>(host.getDrops(raw(state),
                builder.withParameter(LootContextParams.BLOCK_STATE, raw(state))));
        drops.addAll(pile.getDrops(sand(state), builder.withParameter(LootContextParams.BLOCK_STATE, sand(state))));
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return host.getCloneItemStack(level, pos, raw(state));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return SnowHosts.copy(raw(state).rotate(rotation), state);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return SnowHosts.copy(raw(state).mirror(mirror), state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (!stack.is(pile.asItem())) host.setPlacedBy(level, pos, raw(state), entity, stack);
    }

    private static final class Fence extends SandCoveredBlock {
        private Fence(Block host, SandLayerBlock pile) { super(host, pile); }
        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(FenceBlock.NORTH, FenceBlock.EAST, FenceBlock.SOUTH, FenceBlock.WEST);
        }
    }
}
