package su.terrafirmagreg.core.common.block.asphalt;

import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocksAsphalt;

@SuppressWarnings("deprecation")
public class AsphaltRoadHotBlock extends Block {

    /** Same geometry as {@link AsphaltRoadBlock} / RNR path_block. */
    protected static final VoxelShape PATH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    /** Keep hot asphalt for about one minute before setting. */
    public static final int TICKS_UNTIL_SET = 200;

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
            level.scheduleTick(pos, this, TICKS_UNTIL_SET);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        level.setBlock(pos, TFGBlocksAsphalt.ASPHALT_ROAD.getDefaultState(), Block.UPDATE_ALL);
        level.updateNeighborsAt(pos, TFGBlocksAsphalt.ASPHALT_ROAD.get());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        AsphaltRoadHeatVisuals.spawnHotAsphaltAmbient(level, pos, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide() || !(entity instanceof LivingEntity living) || living.isSteppingCarefully()) {
            return;
        }
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        DamageSource src = level.damageSources().hotFloor();
        if (!living.isInvulnerableTo(src)) {
            living.hurt(src, 0.5F);
        }
    }
}
