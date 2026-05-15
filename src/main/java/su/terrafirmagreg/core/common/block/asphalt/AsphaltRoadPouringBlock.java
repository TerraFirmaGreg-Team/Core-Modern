package su.terrafirmagreg.core.common.block.asphalt;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.block.asphalt.blockentity.AsphaltPouringSpreadBlockEntity;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;

public class AsphaltRoadPouringBlock extends Block implements EntityBlock {

    public static final int MAX_VISUAL_LEVEL = 7;
    public static final IntegerProperty ASPHALT_LEVEL = IntegerProperty.create("asphalt_level", 0, MAX_VISUAL_LEVEL);
    protected static final VoxelShape[] SHAPES = createShapes();

    public AsphaltRoadPouringBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(ASPHALT_LEVEL, MAX_VISUAL_LEVEL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ASPHALT_LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(ASPHALT_LEVEL)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, AsphaltRoadHelper.POURING_TICKS_UNTIL_HOT);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        AsphaltRoadHelper.spawnHotAsphaltAmbient(level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        AsphaltRoadHelper.tickBurn(level, pos, entity);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AsphaltPouringSpreadBlockEntity spreadEntity) {
            boolean done = spreadEntity.spreadStep(level, pos);
            int visualLevel = spreadEntity.currentVisualLevel();
            if (state.getValue(ASPHALT_LEVEL) != visualLevel) {
                level.setBlock(pos, state.setValue(ASPHALT_LEVEL, visualLevel), Block.UPDATE_CLIENTS);
            }
            if (!done) {
                level.scheduleTick(pos, this, AsphaltRoadHelper.POURING_TICK_DELAY);
                return;
            }
        } else {
            level.removeBlock(pos, false);
            return;
        }
        level.removeBlock(pos, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AsphaltPouringSpreadBlockEntity(TFGBlockEntities.ASPHALT_POURING_SPREAD.get(), pos, state);
    }

    private static VoxelShape[] createShapes() {
        VoxelShape[] shapes = new VoxelShape[MAX_VISUAL_LEVEL + 1];
        for (int level = 0; level <= MAX_VISUAL_LEVEL; level++) {
            shapes[level] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D + 14.0D * level / MAX_VISUAL_LEVEL, 16.0D);
        }
        return shapes;
    }
}
