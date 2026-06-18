package su.terrafirmagreg.core.common.block.asphalt;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Asphalt;

@SuppressWarnings("deprecation")
public class AsphaltRoadHotBlock extends Block {

    /** Same geometry as {@link AsphaltRoadBlock} / RNR path_block. */
    protected static final VoxelShape PATH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public AsphaltRoadHotBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PATH_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PATH_SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return PATH_SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        TickCounterBlockEntity.reset(level, pos);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, AsphaltRoadHelper.HOT_TICKS_UNTIL_SET);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        level.setBlock(pos, TFGBlocks_Asphalt.ASPHALT_ROAD.getDefaultState(), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos, TFGBlocks_Asphalt.ASPHALT_ROAD.get());
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
}
