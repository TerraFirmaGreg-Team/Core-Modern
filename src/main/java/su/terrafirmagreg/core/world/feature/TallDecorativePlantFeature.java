package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.blocks.TallDecorativePlantBlock;

public class TallDecorativePlantFeature extends Feature<TallDecorativePlantConfig> {

	public TallDecorativePlantFeature(Codec<TallDecorativePlantConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<TallDecorativePlantConfig> context) {
		final WorldGenLevel level = context.level();
		final BlockPos pos = context.origin();

		if (!(context.config().block() instanceof TallDecorativePlantBlock)) {
			TFGCore.LOGGER.error("TallDecorativePlantFeature was passed a block that isn't a TallDecorativePlantBlock! Was: {}", context.config().block());
			return false;
		}

		final int plantHeight = context.config().plantHeight();
		final int middle = context.config().middle();

		final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		// First generate a size
		int height = context.random().nextIntBetweenInclusive(context.config().minHeight(), context.config().maxHeight());

		// Then check that there's enough room
		for (int i = 0; i < height; i++) {
			cursor.setWithOffset(pos, 0, i, 0);
			if (!EnvironmentHelpers.isWorldgenReplaceable(level.getBlockState(cursor))) {
				return false;
			}
		}

		// Make an array of heights
		int[] bsHeights = new int[height];
		int index = 0;
		if (middle != 0) {
			for (int i = 0; i < middle; i++) {
				bsHeights[index++] = i;
			}
		}
		for (int i = 0; i <= height - plantHeight; i++) {
			bsHeights[index++] = middle;
		}
		if (middle < plantHeight - 1) {
			for (int i = middle + 1; i < plantHeight; i++) {
				bsHeights[index++] = i;
			}
		}

		// And then place it
		for (int i = 0; i < Math.max(plantHeight, height); i++) {

			cursor.setWithOffset(pos, 0, i, 0);

			// If we're under the normal height, replace the top with air
			if (height < plantHeight && i == height) {
				level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
				break;
			}

			BlockState state = context.config().block().defaultBlockState();

			// Waterlog it
			FluidState currentFluid = level.getFluidState(cursor);
			if (!currentFluid.isEmpty()) {
				state = state.setValue(TallDecorativePlantBlock.FLUID, TallDecorativePlantBlock.FLUID.keyForOrEmpty(currentFluid.getType()));
			}

			setBlock(level, cursor, state.setValue(TallDecorativePlantBlock.HEIGHT, bsHeights[i]));
		}

		return true;
	}
}
