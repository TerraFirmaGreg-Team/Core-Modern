package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecorativeFloatingPlantBlock extends DecorativePlantBlock {

	public DecorativeFloatingPlantBlock(ExtendedProperties properties, VoxelShape shape) {
		super(properties, shape);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		FluidState fluid = level.getFluidState(pos.below());
		return fluid.is(TFCTags.Fluids.WATER_LIKE) || fluid.is(TFCTags.Fluids.LAVA_LIKE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState();
	}
}
