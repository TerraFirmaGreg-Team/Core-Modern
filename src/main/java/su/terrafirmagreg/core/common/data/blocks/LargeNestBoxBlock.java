package su.terrafirmagreg.core.common.data.blocks;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.BottomSupportedDeviceBlock;
import net.dries007.tfc.common.entities.misc.Seat;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;

public class LargeNestBoxBlock extends BottomSupportedDeviceBlock {
    public static final IntegerProperty NEST_PART = IntegerProperty.create("nest_part", 0, 3);
    public static final IntegerProperty HAS_EGG_TYPE = IntegerProperty.create("has_egg_type", 0, 2);

    public LargeNestBoxBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any()
                .setValue(NEST_PART, 0)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(HAS_EGG_TYPE, 0));
    }

    private static boolean canPlaceLargeNest(Level level, BlockPos pos, BlockState state, Direction facing) {
        for (BlockPos testPos : BlockPos.betweenClosed(pos, pos.relative(facing).relative(facing.getClockWise()))) {
            if (!level.getBlockState(testPos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    public static BlockPos findZeroPos(Level level, BlockPos pos, BlockState state) {
        final Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        int part = state.getValue(NEST_PART);
        return switch (part) {
            case 1 -> pos.relative(facing.getOpposite());
            case 2 -> pos.relative(facing.getCounterClockWise());
            case 3 -> pos.relative(facing.getCounterClockWise()).relative(facing.getOpposite());
            default -> pos;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        final BlockPos zero = findZeroPos(level, pos, state);
        return useZeroBlock(level.getBlockState(zero), level, zero, player, hand, hit.withPosition(zero));
    }

    public InteractionResult useZeroBlock(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof LargeNestBoxBlockEntity) {
            if (player instanceof ServerPlayer serverPlayer) {
                level.getBlockEntity(pos, TFGBlockEntities.LARGE_NEST_BOX.get())
                        .ifPresent(nest -> Helpers.openScreen(serverPlayer, nest, pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder
                .add(NEST_PART)
                .add(BlockStateProperties.HORIZONTAL_FACING)
                .add(HAS_EGG_TYPE));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return canPlaceLargeNest(
                ctx.getLevel(),
                ctx.getClickedPos(),
                defaultBlockState(),
                ctx.getHorizontalDirection())
                        ? defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                                ctx.getHorizontalDirection())
                        : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos pos, BlockPos facingPos) {
        return canPartSurvive(level, pos, state) ? state : Blocks.AIR.defaultBlockState();
    }

    private static final VoxelShape[] SHAPE_0 = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 1, 0, 0, 16, 1, 15),
            Helpers.rotateShape(dir, 1, 0, 12, 16, 4, 15),
            Helpers.rotateShape(dir, 1, 0, 0, 4, 4, 15)));
    private static final VoxelShape[] SHAPE_1 = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 1, 0, 1, 16, 1, 16),
            Helpers.rotateShape(dir, 1, 0, 1, 16, 4, 4),
            Helpers.rotateShape(dir, 1, 0, 1, 4, 4, 16)));
    private static final VoxelShape[] SHAPE_2 = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 0, 0, 0, 15, 1, 15),
            Helpers.rotateShape(dir, 0, 0, 12, 15, 4, 15),
            Helpers.rotateShape(dir, 12, 0, 0, 15, 4, 15)));
    private static final VoxelShape[] SHAPE_3 = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 0, 0, 1, 15, 1, 16),
            Helpers.rotateShape(dir, 0, 0, 1, 15, 4, 4),
            Helpers.rotateShape(dir, 12, 0, 1, 15, 4, 16)));

    private static final VoxelShape[] SHAPE_0_S = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 1, 0, 0, 16, 1, 15),
            Helpers.rotateShape(dir, 1, 0, 12, 16, 4, 15),
            Helpers.rotateShape(dir, 1, 0, 0, 4, 4, 15),
            Helpers.rotateShape(dir, 6, 1, 1, 15, 14, 11)));
    private static final VoxelShape[] SHAPE_1_S = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 1, 0, 1, 16, 1, 16),
            Helpers.rotateShape(dir, 1, 0, 1, 16, 4, 4),
            Helpers.rotateShape(dir, 1, 0, 1, 4, 4, 16),
            Helpers.rotateShape(dir, 5, 1, 9, 14, 12, 16)));
    private static final VoxelShape[] SHAPE_2_S = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 0, 0, 0, 15, 1, 15),
            Helpers.rotateShape(dir, 0, 0, 12, 15, 4, 15),
            Helpers.rotateShape(dir, 12, 0, 0, 15, 4, 15),
            Helpers.rotateShape(dir, 0, 1, 0, 8, 13, 9)));
    private static final VoxelShape[] SHAPE_3_S = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 0, 0, 1, 15, 1, 16),
            Helpers.rotateShape(dir, 0, 0, 1, 15, 4, 4),
            Helpers.rotateShape(dir, 12, 0, 1, 15, 4, 16),
            Helpers.rotateShape(dir, 0, 1, 6, 10, 14, 15)));

    private static final VoxelShape[] SHAPE_0_W = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 1, 0, 0, 16, 1, 15),
            Helpers.rotateShape(dir, 1, 0, 12, 16, 4, 15),
            Helpers.rotateShape(dir, 1, 0, 0, 4, 4, 15),
            Helpers.rotateShape(dir, 6, 1, 1, 14, 11, 9)));
    private static final VoxelShape[] SHAPE_1_W = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 1, 0, 1, 16, 1, 16),
            Helpers.rotateShape(dir, 1, 0, 1, 16, 4, 4),
            Helpers.rotateShape(dir, 1, 0, 1, 4, 4, 16),
            Helpers.rotateShape(dir, 7, 1, 8, 14, 10, 15)));
    private static final VoxelShape[] SHAPE_2_W = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 0, 0, 0, 15, 1, 15),
            Helpers.rotateShape(dir, 0, 0, 12, 15, 4, 15),
            Helpers.rotateShape(dir, 12, 0, 0, 15, 4, 15),
            Helpers.rotateShape(dir, 0, 1, 1, 6, 9, 7)));
    private static final VoxelShape[] SHAPE_3_W = Helpers.computeHorizontalShapes(dir -> Shapes.or(
            Helpers.rotateShape(dir, 0, 0, 1, 15, 1, 16),
            Helpers.rotateShape(dir, 0, 0, 1, 15, 4, 4),
            Helpers.rotateShape(dir, 12, 0, 1, 15, 4, 16),
            Helpers.rotateShape(dir, 2, 1, 8, 9, 11, 15)));

    private static final VoxelShape[][] SHAPES_EMPTY = { SHAPE_0, SHAPE_1, SHAPE_2, SHAPE_3 };
    private static final VoxelShape[][] SHAPES_SNIFFER = { SHAPE_0_S, SHAPE_1_S, SHAPE_2_S, SHAPE_3_S };
    private static final VoxelShape[][] SHAPES_WRAPTOR = { SHAPE_0_W, SHAPE_1_W, SHAPE_2_W, SHAPE_3_W };

    // SHAPES[EGG_TYPE][NEST_PART][DIRECTION]
    public static final VoxelShape[][][] SHAPES = { SHAPES_EMPTY, SHAPES_SNIFFER, SHAPES_WRAPTOR };

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(HAS_EGG_TYPE)][state.getValue(NEST_PART)][state
                .getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue()];
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        final Direction forward = placer != null ? placer.getDirection() : Direction.NORTH;
        final Direction back = forward.getOpposite();
        final Direction right = forward.getClockWise();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(pos);

        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, forward);
        cursor.move(forward);
        level.setBlockAndUpdate(cursor, state.setValue(NEST_PART, 1));
        cursor.move(right).move(back);
        level.setBlockAndUpdate(cursor, state.setValue(NEST_PART, 2));
        cursor.move(forward);
        level.setBlockAndUpdate(cursor, state.setValue(NEST_PART, 3));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        final Entity sitter = Seat.getSittingEntity(level, pos);
        if (sitter != null) {
            sitter.stopRiding();
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // return state.getValue(NEST_PART) == 0 ? super.newBlockEntity(pos, state) : null;
        return super.newBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(NEST_PART) == 0 || canPartSurvive(level, pos, state);
    }

    private static boolean canPartSurvive(LevelReader level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof LargeNestBoxBlock)) {
            return false;
        }
        final Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        final Direction cw = facing.getClockWise();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int dx = facing.getStepX();
        final int cdx = cw.getStepX();
        final int dz = facing.getStepZ();
        final int cdz = cw.getStepZ();

        return switch (state.getValue(NEST_PART)) {
            case 0 -> stageAtPos(dx, 0, dz, level, pos, cursor, 1) &&
                    stageAtPos(cdx, 0, cdz, level, pos, cursor, 2);
            case 1 -> stageAtPos(-dx, 0, -dz, level, pos, cursor, 0) &&
                    stageAtPos(cdx, 0, cdz, level, pos, cursor, 3);
            case 2 -> stageAtPos(dx, 0, dz, level, pos, cursor, 3) &&
                    stageAtPos(-cdx, 0, -cdz, level, pos, cursor, 0);
            case 3 -> stageAtPos(-dx, 0, -dz, level, pos, cursor, 2) &&
                    stageAtPos(-cdx, 0, -cdz, level, pos, cursor, 1);

            default -> false;
        };
    }

    private static boolean stageAtPos(int dx, int dy, int dz, LevelReader level, BlockPos origin,
            BlockPos.MutableBlockPos cursor, int stageWanted) {
        cursor.set(origin).move(dx, dy, dz);
        final BlockState state = level.getBlockState(cursor);
        return state.getBlock() instanceof LargeNestBoxBlock && state.getValue(NEST_PART) == stageWanted;
    }
}
