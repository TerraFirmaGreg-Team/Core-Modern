package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class SmokeEmitterDecorationBlock extends Block {

	public static final VoxelShape DEFAULT_SHAPE = Block.box(2.0F, 0.0F, 2.0F, 14.0F, 16.0F, 14.0F);
	private final VoxelShape shape;

	public SmokeEmitterDecorationBlock(Properties pProperties, VoxelShape shape, Supplier<Item> itemSupplier) {
		super(pProperties);
        this.shape = shape;
    }

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape != null ? shape : super.getShape(state, level, pos, context);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
		var attachedBlock = pos.relative(Direction.DOWN);
		return level.getBlockState(attachedBlock).isFaceSturdy(level, attachedBlock, Direction.UP);
	}

	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		RandomSource randomsource = pLevel.getRandom();
		SimpleParticleType simpleparticletype =  ParticleTypes.CAMPFIRE_SIGNAL_SMOKE;
		pLevel.addAlwaysVisibleParticle(simpleparticletype, true, (double)pPos.getX() + (double)0.5F + randomsource.nextDouble() / (double)4.0F * (double)(randomsource.nextBoolean() ? 1 : -1), (double)pPos.getY() + randomsource.nextDouble() + randomsource.nextDouble(), (double)pPos.getZ() + (double)0.5F + randomsource.nextDouble() / (double)4.0F * (double)(randomsource.nextBoolean() ? 1 : -1), (double)0.0F, 0.07, (double)0.0F);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

		if (!canSurvive(state, level, pos)) {
			Block.updateOrDestroy(state, Blocks.AIR.defaultBlockState(), level, pos, Block.UPDATE_ALL);
		}
	}
}
