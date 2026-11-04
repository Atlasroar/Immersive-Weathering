package com.ordana.immersive_weathering.piles;

import com.ordana.immersive_weathering.snow.SnowHosts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
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
public abstract class PileCoveredBlock extends Block implements BonemealableBlock {
    private static final ThreadLocal<Block> SCHEMA = new ThreadLocal<>();
    private final Block host;

    protected PileCoveredBlock(Block host) {
        super(Properties.copy(host).noOcclusion().randomTicks().offsetType(OffsetType.NONE));
        this.host = host;
        registerDefaultState(SnowHosts.copy(host.defaultBlockState(), stateDefinition.any()).setValue(SnowLayerBlock.LAYERS, 1));
    }

    protected static <T extends PileCoveredBlock> T create(Block host, java.util.function.Supplier<T> factory) {
        if (host.defaultBlockState().hasBlockEntity() || host.defaultBlockState().hasProperty(SnowLayerBlock.LAYERS)) {
            throw new IllegalArgumentException("Invalid pile host schema: " + host);
        }
        // Block constructs its state definition before subclass fields are initialized.
        Block previous = SCHEMA.get();
        SCHEMA.set(host);
        try {
            return factory.get();
        } finally {
            if (previous == null) SCHEMA.remove();
            else SCHEMA.set(previous);
        }
    }

    public Block host() { return host; }

    protected BlockState raw(BlockState state) { return PilePlants.raw(state); }
    protected abstract BlockState pileState(BlockState state);
    protected abstract BlockState cover(BlockState state, int layers);
    protected abstract boolean canAdd(BlockPlaceContext context, BlockState state);
    protected abstract boolean replacement(BlockState state);
    protected abstract boolean isPileItem(ItemStack stack);
    protected abstract boolean hasPileSupport(LevelReader level, BlockPos pos);
    protected abstract void checkSupport(ServerLevel level, BlockPos pos, BlockState state);
    protected abstract boolean weatherTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SnowLayerBlock.LAYERS);
        Block schema = SCHEMA.get();
        if (schema == null) throw new IllegalStateException("Pile host schema was not supplied");
        schema.defaultBlockState().getProperties().forEach(property -> builder.add(property));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(raw(state).getShape(level, pos, context), pileState(state).getShape(level, pos, context));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(raw(state).getCollisionShape(level, pos, context), pileState(state).getCollisionShape(level, pos, context));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.or(raw(state).getBlockSupportShape(level, pos), pileState(state).getBlockSupportShape(level, pos));
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.or(raw(state).getOcclusionShape(level, pos), pileState(state).getOcclusionShape(level, pos));
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) { return true; }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return raw(state).isPathfindable(level, pos, type) && pileState(state).isPathfindable(level, pos, type);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return SnowHosts.hostSurvives(raw(state), level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                   LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        BlockState hostState = raw(state);
        BlockState updated = host instanceof DoublePlantBlock && !(host instanceof PitcherCropBlock)
                ? SnowHosts.doubleShape(hostState, direction, neighbor, level, pos)
                : host.updateShape(hostState, direction, neighbor, level, pos, otherPos);
        if (updated.isAir() || !hasPileSupport(level, pos)) level.scheduleTick(pos, this, 2);
        if (!updated.is(host)) {
            BlockState replacement = cover(updated, state.getValue(SnowLayerBlock.LAYERS));
            return replacement == null ? state : replacement;
        }
        return SnowHosts.copy(updated, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moving) {
        PileInteractions.run(level, () -> host.onPlace(raw(state), level, pos, raw(old), moving));
        if (!canSurvive(state, level, pos) || !hasPileSupport(level, pos)) level.scheduleTick(pos, this, 2);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        PileInteractions.run(level, () -> host.neighborChanged(raw(state), level, pos, block, otherPos, moving));
        retainCover(level, pos, state);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        PileInteractions.run(level, () -> host.stepOn(level, pos, raw(state), entity));
        retainCover(level, pos, state);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return host.getDestroyProgress(raw(state), player, level, pos);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit,
                                 net.minecraft.world.entity.projectile.Projectile projectile) {
        PileInteractions.run(level, () -> host.onProjectileHit(level, raw(state), hit, projectile));
        retainCover(level, hit.getBlockPos(), state);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState hostState = raw(state);
        boolean valid = SnowHosts.hostSurvives(hostState, level, pos);
        if (host instanceof DoublePlantBlock && !(host instanceof PitcherCropBlock)) {
            Direction partner = hostState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                    ? Direction.UP : Direction.DOWN;
            valid = SnowHosts.doubleShape(hostState, partner, level.getBlockState(pos.relative(partner)),
                    level, pos).is(host);
        }
        if (!valid) {
            if (level.setBlockAndUpdate(pos, pileState(state))) {
                Block.dropResources(hostState, level, pos);
                checkSupport(level, pos, pileState(state));
            }
            return;
        }
        PileInteractions.run(level, () -> host.tick(hostState, level, pos, random));
        retainCover(level, pos, state);
        BlockState current = level.getBlockState(pos);
        if (current.getBlock() == this) checkSupport(level, pos, current);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (weatherTick(state, level, pos, random)) return;
        BlockState hostState = raw(state);
        if (hostState.isRandomlyTicking()) {
            List<SaplingCover> neighbours = saplingCovers(hostState, level, pos);
            PileInteractions.run(level, () -> host.randomTick(hostState, level, pos, random));
            retainGrowthCover(level, pos, state, neighbours);
        }
    }

    private record SaplingCover(BlockPos pos, BlockState state) {}

    private List<SaplingCover> saplingCovers(BlockState hostState, LevelReader level, BlockPos pos) {
        if (!(host instanceof SaplingBlock) || hostState.getValue(SaplingBlock.STAGE) == 0) return List.of();
        List<SaplingCover> result = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos other = pos.offset(x, 0, z);
                BlockState state = level.getBlockState(other);
                if (state.getBlock() instanceof PileCoveredBlock && raw(state).is(host)) {
                    result.add(new SaplingCover(other, state));
                }
            }
        }
        return result;
    }

    private void retainGrowthCover(Level level, BlockPos pos, BlockState state, List<SaplingCover> neighbours) {
        retainCover(level, pos, state);
        // Failed 2x2 tree growth restores raw saplings in all four cells, not only the tick owner.
        for (SaplingCover cover : neighbours) {
            if (raw(level.getBlockState(cover.pos())).is(host)) {
                ((PileCoveredBlock) cover.state().getBlock()).retainCover(level, cover.pos(), cover.state());
            }
        }
    }

    private void retainCover(Level level, BlockPos pos, BlockState previous) {
        BlockState current = level.getBlockState(pos);
        if (current == previous) return;
        BlockState covered = cover(current, previous.getValue(SnowLayerBlock.LAYERS));
        if (covered != null && covered != current) level.setBlockAndUpdate(pos, covered);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        host.animateTick(raw(state), level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        PileInteractions.run(level, () -> host.entityInside(raw(state), level, pos, entity));
        retainCover(level, pos, state);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (canAdd(context, state)) return true;
        return replacement(state)
                && context.getItemInHand().getItem() instanceof net.minecraft.world.item.BlockItem
                && !isPileItem(context.getItemInHand())
                && raw(state).canBeReplaced(context);
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
                    Block.dropResources(pileState(state), level, pos, null, player, player.getItemInHand(hand));
                    player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                }
                level.levelEvent(player, 2001, pos, Block.getId(pileState(state)));
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        InteractionResult result = PileInteractions.call(level, () -> host.use(raw(state), level, pos, player, hand, hit));
        retainCover(level, pos, state);
        return result;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> result = new ArrayList<>(host.getDrops(raw(state), builder.withParameter(LootContextParams.BLOCK_STATE, raw(state))));
        result.addAll(pileState(state).getDrops(builder.withParameter(LootContextParams.BLOCK_STATE, pileState(state))));
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
        if (!isPileItem(stack)) {
            PileInteractions.run(level, () -> host.setPlacedBy(level, pos, raw(state), entity, stack));
            retainCover(level, pos, state);
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
            List<SaplingCover> neighbours = saplingCovers(raw(state), level, pos);
            PileInteractions.run(level, () -> plant.performBonemeal(level, random, pos, raw(state)));
            retainGrowthCover(level, pos, state, neighbours);
        }
    }

}
