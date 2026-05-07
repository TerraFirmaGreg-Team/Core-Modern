package su.terrafirmagreg.core.common.block.asphalt;

import com.therighthon.rnr.common.RNRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

@SuppressWarnings("deprecation")
public class PouringAsphaltRoadBlock extends Block {

    public static final int TICKS_UNTIL_HOT = 10;
    public static final int DEFAULT_ASPHALT_LEVEL = 4;
    public static final IntegerProperty ASPHALT_LEVEL = IntegerProperty.create("asphalt_level", 0, DEFAULT_ASPHALT_LEVEL);

    public PouringAsphaltRoadBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ASPHALT_LEVEL, DEFAULT_ASPHALT_LEVEL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ASPHALT_LEVEL);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, TICKS_UNTIL_HOT);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        int asphaltLevel = state.getValue(ASPHALT_LEVEL);
        if (asphaltLevel > 0) {
            spreadAsphalt(level, pos.north(), asphaltLevel);
            spreadAsphalt(level, pos.south(), asphaltLevel);
            spreadAsphalt(level, pos.west(), asphaltLevel);
            spreadAsphalt(level, pos.east(), asphaltLevel);
        }
        level.setBlock(pos, TFGBlocks.HOT_ASPHALT_ROAD.getDefaultState(), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos, TFGBlocks.HOT_ASPHALT_ROAD.get());
    }

    private static void spreadAsphalt(Level level, BlockPos spreadPos, int sourceLevel) {
        if (level.getBlockState(spreadPos).is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            level.setBlock(spreadPos,
                    TFGBlocks.POURING_ASPHALT_ROAD.getDefaultState().setValue(ASPHALT_LEVEL, sourceLevel - 1),
                    Block.UPDATE_ALL);
        }
    }
}
