package su.terrafirmagreg.core.common.block.appliances;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.DeviceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CondenserBlock extends DeviceBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, -2.0D, 2.0D, 14.0D, 4.0D, 14.0D),
            Block.box(4.0D, 4.0D, 4.0D, 12.0D, 7.0D, 12.0D),
            Block.box(6.0D, 7.0D, 6.0D, 10.0D, 11.0D, 10.0D)
    );

    public CondenserBlock(ExtendedProperties properties) {
        super(properties, InventoryRemoveBehavior.SAVE);
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Block below = level.getBlockState(pos.relative(Direction.DOWN)).getBlock();
        return below instanceof CopperPotBlock;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (!canSurvive(state, level, pos)) {
            Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            return state.setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return null;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

//    @Override
//    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
//        CondenserBlockEntity condenser = level.getBlockEntity(pos, TFGBlockEntities.CONDENSER.get()).orElse(null);
//        if (condenser != null) {
//            return condenser.getFuelLevel();
//        }
//        return 0;
//    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

//    @Override
//    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
//        var heldItem = player.getItemInHand(hand);
//        if (!heldItem.isEmpty() && heldItem.getItem() instanceof ArmItem) {
//            return InteractionResult.FAIL;
//        }
//
//        CondenserBlockEntity condenser = level.getBlockEntity(pos, TFGBlockEntities.CONDENSER.get()).orElse(null);
//        if (condenser != null) {
//            if (FluidHelpers.transferBetweenBlockEntityAndItem(heldItem, condenser, player, hand)) {
//                return InteractionResult.SUCCESS;
//            }
//            return InteractionResult.sidedSuccess(level.isClientSide());
//        }
//        return InteractionResult.PASS;
//    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

}
