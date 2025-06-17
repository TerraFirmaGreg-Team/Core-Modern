package su.terrafirmagreg.core.world;

import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class DeadCoralHelpers
{
	/**
	 * A copy of TFC's coral features to place dead ones, which itself is a copy of vanilla's
	 */
	public static boolean placeCoralBlock(LevelAccessor level, RandomSource rand, BlockPos pos, BlockState coralBlockState)
	{
		BlockPos abovePos = pos.above();
		BlockState blockstate = level.getBlockState(pos);
		if ((level.isEmptyBlock(pos) || Helpers.isBlock(blockstate, BlockTags.CORALS)) && level.isEmptyBlock(abovePos))
		{
			if (coralBlockState.is(Blocks.FIRE_CORAL_BLOCK))
			{
				level.setBlock(pos, Blocks.DEAD_FIRE_CORAL_BLOCK.defaultBlockState(), 3);
			}
			else if (coralBlockState.is(Blocks.BUBBLE_CORAL_BLOCK))
			{
				level.setBlock(pos, Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState(), 3);
			}
			else if (coralBlockState.is(Blocks.HORN_CORAL_BLOCK))
			{
				level.setBlock(pos, Blocks.DEAD_HORN_CORAL_BLOCK.defaultBlockState(), 3);
			}
			else if (coralBlockState.is(Blocks.BRAIN_CORAL_BLOCK))
			{
				level.setBlock(pos, Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState(), 3);
			}
			else if (coralBlockState.is(Blocks.TUBE_CORAL_BLOCK))
			{
				level.setBlock(pos, Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState(), 3);
			}

			if (rand.nextFloat() < 0.25F)
			{
				BlockState coralState = switch (rand.nextInt(5)) {
					case 0 -> Blocks.DEAD_FIRE_CORAL_BLOCK.defaultBlockState();
					case 1 -> Blocks.DEAD_BUBBLE_CORAL_BLOCK.defaultBlockState();
					case 2 -> Blocks.DEAD_HORN_CORAL_BLOCK.defaultBlockState();
					case 3 -> Blocks.DEAD_BRAIN_CORAL_BLOCK.defaultBlockState();
					default -> Blocks.DEAD_TUBE_CORAL_BLOCK.defaultBlockState();
				};

				level.setBlock(abovePos, coralState, 2);
			}

			for (Direction direction : Direction.Plane.HORIZONTAL)
			{
				if (rand.nextFloat() < 0.2F)
				{
					BlockPos relativePos = pos.relative(direction);
					if (level.isEmptyBlock(relativePos))
					{
						BlockState coralState = (switch (rand.nextInt(5)) {
							case 0 -> Blocks.DEAD_FIRE_CORAL_WALL_FAN.defaultBlockState();
							case 1 -> Blocks.DEAD_BUBBLE_CORAL_WALL_FAN.defaultBlockState();
							case 2 -> Blocks.DEAD_HORN_CORAL_WALL_FAN.defaultBlockState();
							case 3 -> Blocks.DEAD_BRAIN_CORAL_WALL_FAN.defaultBlockState();
							default -> Blocks.DEAD_TUBE_CORAL_WALL_FAN.defaultBlockState();
						}).setValue(CoralWallFanBlock.WATERLOGGED, false);

						if (coralState.hasProperty(CoralWallFanBlock.FACING))
						{
							coralState = coralState.setValue(CoralWallFanBlock.FACING, direction);
						}

						level.setBlock(relativePos, coralState, 2);
					}
				}
			}
			return true;
		}
		return false;
	}
}
