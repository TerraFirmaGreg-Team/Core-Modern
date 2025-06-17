package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Basically a copy of the vanilla chorus flower block except it can grow on any rock/cobble/gravel/sand
 * instead of being limited to end stone
 */

public class LunarChorusFlowerBlock extends Block {

	public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
	private final Supplier<? extends Block> plant;

	public LunarChorusFlowerBlock(BlockBehaviour.Properties pProperties, Supplier<? extends Block> plantBlock)
	{
		super(pProperties);
		this.plant = plantBlock;

		registerDefaultState(this.defaultBlockState().setValue(AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AGE);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter view, BlockPos pos) {
		return false;
	}

	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
	{
		if (!pState.canSurvive(pLevel, pPos))
		{
			pLevel.destroyBlock(pPos, true);
		}
	}

	private void setBodyBlock(LevelAccessor level, BlockPos pos)
	{
		LunarChorusPlantBlock plantBlock = (LunarChorusPlantBlock) plant.get();
		level.setBlock(pos, plantBlock.getStateForPlacement(level, pos), 2);
	}

	public static boolean isGroundBlock(BlockState state)
	{
		return state.is(Tags.Blocks.STONE)
		   || state.is(Tags.Blocks.COBBLESTONE)
		   || state.is(Tags.Blocks.GRAVEL)
		   || state.is(Tags.Blocks.SAND);
	}

	public static boolean isGroundBlock(Block block)
	{
		return Helpers.isBlock(block, Tags.Blocks.STONE)
			|| Helpers.isBlock(block, Tags.Blocks.COBBLESTONE)
			|| Helpers.isBlock(block, Tags.Blocks.GRAVEL)
			|| Helpers.isBlock(block, Tags.Blocks.SAND);
	}

	public boolean isRandomlyTicking(BlockState pState) {
		return pState.getValue(AGE) < 5;
	}

	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
	{
		BlockPos abovePos = pPos.above();
		if (pLevel.isEmptyBlock(abovePos) && abovePos.getY() < pLevel.getMaxBuildHeight())
		{
			int i = pState.getValue(AGE);
			if (i < 5 && ForgeHooks.onCropsGrowPre(pLevel, abovePos, pState, true))
			{
				boolean shouldPlaceNewBody = false;
				boolean foundGroundFurtherDown = false;
				BlockState belowState = pLevel.getBlockState(pPos.below());
				Block belowBlock = belowState.getBlock();

				if (isGroundBlock(belowState))
				{
					shouldPlaceNewBody = true;
				}
				else if (belowBlock == plant)
				{
					int j = 1;

					for (int k = 0; k < 4; ++k)
					{
						Block belowBlockOffset = pLevel.getBlockState(pPos.below(j + 1)).getBlock();
						if (belowBlockOffset != plant)
						{
							if (isGroundBlock(belowBlockOffset))
							{
								foundGroundFurtherDown = true;
							}
							break;
						}

						++j;
					}

					if (j < 2 || j <= pRandom.nextInt(foundGroundFurtherDown ? 5 : 4))
					{
						shouldPlaceNewBody = true;
					}
				}
				else if (belowState.isAir())
				{
					shouldPlaceNewBody = true;
				}

				if (shouldPlaceNewBody && allNeighborsEmpty(pLevel, abovePos, null) && pLevel.isEmptyBlock(pPos.above(2)))
				{
					setBodyBlock(pLevel, pPos);
					this.placeGrownFlower(pLevel, abovePos, i);
				}
				else if (i < 4)
				{
					int l = pRandom.nextInt(4);
					if (foundGroundFurtherDown)
					{
						++l;
					}

					boolean foundValidGrowthSpace = false;

					for(int j = 0; j < l; ++j)
					{
						Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
						BlockPos blockpos1 = pPos.relative(direction);

						if (pLevel.isEmptyBlock(blockpos1)
							&& pLevel.isEmptyBlock(blockpos1.below())
							&& allNeighborsEmpty(pLevel, blockpos1, direction.getOpposite()))
						{
							this.placeGrownFlower(pLevel, blockpos1, i + 1);
							foundValidGrowthSpace = true;
						}
					}

					if (foundValidGrowthSpace)
					{
						setBodyBlock(pLevel, pPos);
					}
					else
					{
						this.placeDeadFlower(pLevel, pPos);
					}
				}
				else
				{
					placeDeadFlower(pLevel, pPos);
				}

				ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
			}
		}
	}

	private void placeGrownFlower(Level pLevel, BlockPos pPos, int pAge)
	{
		pLevel.setBlock(pPos, this.defaultBlockState().setValue(AGE, pAge), 2);
		pLevel.levelEvent(1033, pPos, 0);
	}

	private void placeDeadFlower(Level pLevel, BlockPos pPos)
	{
		pLevel.setBlock(pPos, this.defaultBlockState().setValue(AGE, 5), 2);
		pLevel.levelEvent(1034, pPos, 0);
	}

	private static boolean allNeighborsEmpty(LevelReader pLevel, BlockPos pPos, @Nullable Direction pExcludingSide)
	{
		for (Direction direction : Direction.Plane.HORIZONTAL)
		{
			if (direction != pExcludingSide && !pLevel.isEmptyBlock(pPos.relative(direction)))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
	{
		LunarChorusPlantBlock body = (LunarChorusPlantBlock) plant.get();

		BlockState blockstate = pLevel.getBlockState(pPos.below());
		if (blockstate.getBlock() != body && !isGroundBlock(blockstate))
		{
			if (!blockstate.isAir())
			{
				return false;
			}
			else
			{
				boolean isValid = false;

				for (Direction direction : Direction.Plane.HORIZONTAL)
				{
					BlockState relativeState = pLevel.getBlockState(pPos.relative(direction));
					if (Helpers.isBlock(relativeState, body))
					{
						if (isValid)
						{
							return false;
						}

						isValid = true;
					}
					else if (!relativeState.isAir())
					{
						return false;
					}
				}

				return isValid;
			}
		}
		else
		{
			return true;
		}
	}


	public boolean generatePlant(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, int pMaxHorizontalDistance)
	{
		final BlockState originalState = pLevel.getBlockState(pPos);

		setBodyBlock(pLevel, pPos);

		if (pLevel.getBlockState(pPos).canSurvive(pLevel, pPos) && growTreeRecursive(pLevel, pPos, pRandom, pPos, pMaxHorizontalDistance, 0))
		{
			return true;
		}
		else
		{
			// Revert the original state
			pLevel.setBlock(pPos, originalState, 3);
			return false;
		}
	}

	public boolean growTreeRecursive(LevelAccessor level, BlockPos branchPos, RandomSource rand, BlockPos originalBranchPos, int maxHorizontalDistance, int iterations)
	{
		boolean any = false;
		int i = rand.nextInt(5) + 1;
		if (iterations == 0)
		{
			++i;
		}
		for (int j = 0; j < i; ++j)
		{
			BlockPos blockpos = branchPos.above(j + 1);
			if (!allNeighborsEmpty(level, blockpos, null))
			{
				return any;
			}
			else
			{
				setBodyBlock(level, blockpos);
			}
			setBodyBlock(level, blockpos.below());
		}

		boolean willContinue = false;
		if (iterations < 4)
		{
			int branchAttempts = rand.nextInt(4);
			if (iterations == 0)
			{
				++branchAttempts;
			}

			for (int k = 0; k < branchAttempts; ++k)
			{
				Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
				BlockPos aboveRelativePos = branchPos.above(i).relative(direction);

				if (Math.abs(aboveRelativePos.getX() - originalBranchPos.getX()) < maxHorizontalDistance
					&& Math.abs(aboveRelativePos.getZ() - originalBranchPos.getZ()) < maxHorizontalDistance
					&& level.isEmptyBlock(aboveRelativePos)
					&& level.isEmptyBlock(aboveRelativePos.below())
					&& allNeighborsEmpty(level, aboveRelativePos, direction.getOpposite()))
				{
					willContinue = true;
					setBodyBlock(level, aboveRelativePos);
					setBodyBlock(level, aboveRelativePos.relative(direction.getOpposite()));
					growTreeRecursive(level, aboveRelativePos, rand, originalBranchPos, maxHorizontalDistance, iterations + 1);
				}
			}
		}
		if (!willContinue)
		{
			level.setBlock(branchPos.above(i), defaultBlockState().setValue(AGE, rand.nextInt(10) == 1 ? 3 : 5), 2);
		}
		return true;
	}
}
