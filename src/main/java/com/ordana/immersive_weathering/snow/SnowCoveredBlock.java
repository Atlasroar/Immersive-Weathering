package com.ordana.immersive_weathering.snow;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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

/**
 * One registered block per explicitly supported host, with the host's property objects plus LAYERS.
 * Host methods always receive a raw state: vanilla wall shape caches cannot index a composite state.
 */
public class SnowCoveredBlock extends Block implements BonemealableBlock {
    private final Block host;

    protected SnowCoveredBlock(Block host) {
        super(Properties.copy(host).noOcclusion().randomTicks().offsetType(OffsetType.NONE));
        this.host = host;
        registerDefaultState(SnowHosts.copy(host.defaultBlockState(), stateDefinition.any()).setValue(SnowLayerBlock.LAYERS, 1));
    }

    public static SnowCoveredBlock create(Block host) {
        if (host instanceof FenceBlock) return new Fence(host);
        if (host instanceof WallBlock) return new Wall(host);
        if (host instanceof DoublePlantBlock) return new DoublePlant(host);
        if (host == Blocks.GRASS || host == Blocks.FERN) return new SnowCoveredBlock(host);
        throw new IllegalArgumentException("No snow state schema for " + host);
    }

    public Block host() { return host; }

    private BlockState raw(BlockState state) { return SnowHosts.raw(state); }
    private BlockState snow(BlockState state) {
        return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, state.getValue(SnowLayerBlock.LAYERS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SnowLayerBlock.LAYERS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(raw(state).getShape(level, pos, context), snow(state).getShape(level, pos, context));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(raw(state).getCollisionShape(level, pos, context), snow(state).getCollisionShape(level, pos, context));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.or(raw(state).getBlockSupportShape(level, pos), snow(state).getBlockSupportShape(level, pos));
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.or(raw(state).getOcclusionShape(level, pos), snow(state).getOcclusionShape(level, pos));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) { return true; }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return raw(state).isPathfindable(level, pos, type) && snow(state).isPathfindable(level, pos, type);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return SnowHosts.hostSurvives(raw(state), level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                   LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        BlockState hostState = raw(state);
        BlockState updated = host instanceof DoublePlantBlock
                ? SnowHosts.doubleShape(hostState, direction, neighbor, level, pos)
                : host.updateShape(hostState, direction, neighbor, level, pos, otherPos);
        level.scheduleTick(pos, this, 2);
        if (!updated.is(host)) return state;
        return SnowHosts.copy(updated, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState hostState = raw(state);
        boolean valid = SnowHosts.hostSurvives(hostState, level, pos);
        if (host instanceof DoublePlantBlock) {
            Direction partner = hostState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                    ? Direction.UP : Direction.DOWN;
            valid = SnowHosts.doubleShape(hostState, partner, level.getBlockState(pos.relative(partner)),
                    level, pos).is(host);
        }
        if (!valid) {
            Block.dropResources(hostState, level, pos);
            level.setBlockAndUpdate(pos, snow(state));
            SnowPhysics.checkSupport(level, pos, snow(state));
            return;
        }
        SnowPhysics.checkSupport(level, pos, state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBrightness(LightLayer.BLOCK, pos) > 11 || level.isRainingAt(pos.above())) {
            level.setBlockAndUpdate(pos, raw(state));
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (SnowHosts.canAddSnow(context, state)) return true;
        return SnowSettings.replacement() && SnowHosts.layers(state) < 8
                && context.getItemInHand().getItem() instanceof net.minecraft.world.item.BlockItem
                && !context.getItemInHand().is(net.minecraft.world.item.Items.SNOW)
                && raw(state).canBeReplaced(context);
    }

    @Override
    public boolean canBeReplaced(BlockState state, Fluid fluid) { return false; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof ShovelItem && player.mayBuild()) {
            if (!level.isClientSide) {
                if (!player.isCreative()) {
                    Block.dropResources(snow(state), level, pos, null, player, player.getItemInHand(hand));
                    player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
                level.setBlockAndUpdate(pos, raw(state));
                level.levelEvent(player, 2001, pos, Block.getId(snow(state)));
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return host.use(raw(state), level, pos, player, hand, hit);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> result = new ArrayList<>(host.getDrops(raw(state), builder.withParameter(LootContextParams.BLOCK_STATE, raw(state))));
        result.addAll(Blocks.SNOW.getDrops(snow(state), builder.withParameter(LootContextParams.BLOCK_STATE, snow(state))));
        return result;
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
        if (!stack.is(net.minecraft.world.item.Items.SNOW)) {
            host.setPlacedBy(level, pos, raw(state), entity, stack);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (host instanceof DoublePlantBlock && !level.isClientSide) {
            if (player.isCreative()) {
                if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                        && SnowHosts.raw(level.getBlockState(pos.below())).is(host)) {
                    level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 35);
                }
            } else {
                Block.dropResources(state, level, pos, null, player, player.getMainHandItem());
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                               @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, host instanceof DoublePlantBlock
                ? Blocks.AIR.defaultBlockState() : state, blockEntity, tool);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean client) {
        return host instanceof BonemealableBlock plant && plant.isValidBonemealTarget(level, pos, raw(state), client);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return host instanceof BonemealableBlock plant && plant.isBonemealSuccess(level, random, pos, raw(state));
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (host instanceof BonemealableBlock plant) {
            plant.performBonemeal(level, random, pos, raw(state));
            BlockState grown = SnowHosts.cover(level.getBlockState(pos), SnowHosts.layers(state));
            if (grown != null) level.setBlockAndUpdate(pos, grown);
        }
    }

    public static final class Fence extends SnowCoveredBlock {
        private Fence(Block host) { super(host); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(FenceBlock.NORTH, FenceBlock.EAST, FenceBlock.SOUTH, FenceBlock.WEST);
        }
    }

    public static final class Wall extends SnowCoveredBlock {
        private Wall(Block host) { super(host); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(WallBlock.UP, WallBlock.NORTH_WALL, WallBlock.EAST_WALL, WallBlock.SOUTH_WALL,
                    WallBlock.WEST_WALL);
        }
    }

    public static final class DoublePlant extends SnowCoveredBlock {
        private DoublePlant(Block host) { super(host); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(DoublePlantBlock.HALF);
        }
    }
}
