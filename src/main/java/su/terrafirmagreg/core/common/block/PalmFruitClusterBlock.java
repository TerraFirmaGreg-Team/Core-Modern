package su.terrafirmagreg.core.common.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("deprecation")
public class PalmFruitClusterBlock extends HorizontalDirectionalBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final BooleanProperty ATTACHED = BooleanProperty.create("attached");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    public PalmFruitClusterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(AGE, 0)
                .setValue(ATTACHED, false)
                .setValue(NATURAL, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, AGE, ATTACHED, NATURAL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state != null) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            if (isValidAttachment(context.getLevel(), context.getClickedPos(), facing)) {
                return state.setValue(FACING, facing)
                        .setValue(ATTACHED, true)
                        .setValue(NATURAL, false);
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        return isValidAttachment(level, pos, state.getValue(FACING));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction == state.getValue(FACING)) {
            return state.setValue(ATTACHED, isValidAttachment(level, pos, direction));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ATTACHED)) {
            if (random.nextFloat() < 0.25f) {
                int age = state.getValue(AGE);
                if (age < 6) {
                    level.setBlock(pos, state.setValue(AGE, age + 1), 2);
                } else {
                    level.removeBlock(pos, false);
                }
            }
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(ATTACHED);
    }

    private boolean isValidAttachment(LevelReader level, BlockPos pos, Direction facing) {
        BlockState neighbor = level.getBlockState(pos.relative(facing));
        if (neighbor.getBlock() instanceof PalmHeadBlock) {
            return neighbor.hasProperty(PalmHeadBlock.NATURAL) && neighbor.getValue(PalmHeadBlock.NATURAL);
        }
        if (neighbor.getBlock() instanceof PalmFruitClusterBlock) {
            return neighbor.hasProperty(NATURAL) && neighbor.getValue(NATURAL);
        }
        return false;
    }
}
