package su.terrafirmagreg.core.common.block;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RopeLadderBlock extends Block {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final int MAX_LENGTH = 64;

    private static final VoxelShape EAST_AABB = Block.box(0, 0, 0, 4, 16, 16);
    private static final VoxelShape WEST_AABB = Block.box(12, 0, 0, 16, 16, 16);
    private static final VoxelShape SOUTH_AABB = Block.box(0, 0, 0, 16, 16, 4);
    private static final VoxelShape NORTH_AABB = Block.box(0, 0, 12, 16, 16, 16);

    public RopeLadderBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.getBlock() instanceof RopeLadderBlock) {
            return defaultBlockState().setValue(FACING, belowState.getValue(FACING));
        }

        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal() && canAttachTo(level, pos.relative(clickedFace.getOpposite()), clickedFace)) {
            return defaultBlockState().setValue(FACING, clickedFace);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (canAttachTo(level, pos.relative(direction.getOpposite()), direction)) {
                return defaultBlockState().setValue(FACING, direction);
            }
        }

        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(placer instanceof Player player)) {
            return;
        }

        Direction facing = state.getValue(FACING);
        BlockPos nextPos = pos.below();
        int placed = 0;
        while (placed < MAX_LENGTH && nextPos.getY() >= level.getMinBuildHeight()) {
            BlockState nextState = level.getBlockState(nextPos);
            if (!nextState.canBeReplaced() || !nextState.getFluidState().isEmpty()) {
                break;
            }

            ItemStack ladderStack = findMatchingLadderInInventory(player);
            if (ladderStack.isEmpty()) {
                break;
            }

            level.setBlock(nextPos, defaultBlockState().setValue(FACING, facing), Block.UPDATE_ALL);
            if (!player.getAbilities().instabuild) {
                ladderStack.shrink(1);
            }

            placed++;
            nextPos = nextPos.below();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (!canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (level.isClientSide() || movedByPiston || newState.is(this)) {
            return;
        }

        BlockPos below = pos.below();
        while (level.getBlockState(below).is(this)) {
            popResource(level, below, new ItemStack(this));
            level.removeBlock(below, false);
            below = below.below();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.getBlockState(pos.above()).is(this)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            BlockPos next = pos;
            List<BlockPos> chain = new ArrayList<>();
            while (level.getBlockState(next).is(this)) {
                chain.add(next);
                next = next.below();
            }

            for (int i = chain.size() - 1; i >= 0; i--) {
                giveLadderToPlayer(player);
                level.removeBlock(chain.get(i), false);
            }

            if (!chain.isEmpty()) {
                level.playSound(null, player.blockPosition(), SoundEvents.LADDER_STEP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (level.getBlockState(pos.above()).is(this)) {
            return true;
        }
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            default -> NORTH_AABB;
        };
    }

    private static boolean canAttachTo(LevelReader level, BlockPos pos, Direction direction) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, direction);
    }

    private ItemStack findMatchingLadderInInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(asItem())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void giveLadderToPlayer(Player player) {
        ItemStack ladderStack = new ItemStack(this);
        if (!player.getInventory().add(ladderStack)) {
            player.drop(ladderStack, false);
        }
    }
}
