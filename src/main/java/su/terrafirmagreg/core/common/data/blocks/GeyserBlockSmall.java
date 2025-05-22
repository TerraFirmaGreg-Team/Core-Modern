package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GeyserBlockSmall extends Block {

	protected static final VoxelShape SHAPE = Block.box(2.0F, 0.0F, 2.0F, 14.0F, 12.0F, 14.0F);


	public GeyserBlockSmall(Properties pProperties) {
		super(pProperties);
	}


	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
	{
		return SHAPE;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
		var attachedBlock = pos.relative(Direction.DOWN);
		return level.getBlockState(attachedBlock).isFaceSturdy(level, attachedBlock, Direction.UP);
	}

	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		RandomSource randomsource = pLevel.getRandom();
		SimpleParticleType simpleparticletype =  ParticleTypes.CAMPFIRE_COSY_SMOKE;
		pLevel.addAlwaysVisibleParticle(simpleparticletype, true, (double)pPos.getX() + (double)0.5F + randomsource.nextDouble() / (double)3.0F * (double)(randomsource.nextBoolean() ? 1 : -1), (double)pPos.getY() + randomsource.nextDouble() + randomsource.nextDouble(), (double)pPos.getZ() + (double)0.5F + randomsource.nextDouble() / (double)3.0F * (double)(randomsource.nextBoolean() ? 1 : -1), (double)0.0F, 0.07, (double)0.0F);
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
