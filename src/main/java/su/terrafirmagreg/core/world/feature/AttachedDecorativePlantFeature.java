package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import net.dries007.tfc.common.blocks.plant.EpiphytePlantBlock;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.DecorativeAttachedPlantBlock;

// Mostly just a copy of TFC's EpiphytePlantFeature

public class AttachedDecorativePlantFeature extends Feature<AttachedDecorativePlantConfig> {

	public AttachedDecorativePlantFeature(Codec<AttachedDecorativePlantConfig> pCodec) {
		super(pCodec);
	}

	@Override
	public boolean place(FeaturePlaceContext<AttachedDecorativePlantConfig> context) {
		final RandomSource random = context.random();
		final WorldGenLevel level = context.level();
		final Block block = context.config().block();

		if (!(block instanceof DecorativeAttachedPlantBlock)) {
			TFGCore.LOGGER.error("AttachedDecorativePlantFeature was passed a block that isn't a DecorativeAttachedPlantBlock! Was: {}", block);
			return false;
		}

		final BlockPos pos = context.origin().offset(0, random.nextInt(context.config().heightRange()), 0);
		if (!EnvironmentHelpers.isWorldgenReplaceable(level, pos))
			return false;

		BlockState state = block.defaultBlockState();
		for (Direction direction : Direction.Plane.HORIZONTAL)
		{
			state = state.setValue(DecorativeAttachedPlantBlock.FACING, direction);
			if (state.canSurvive(level, pos))
			{
				setBlock(level, pos, state);
				return true;
			}
		}
		return false;
	}
}
