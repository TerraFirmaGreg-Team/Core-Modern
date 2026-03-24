package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.world.feature.plant.CreepingPlantConfig;
import net.dries007.tfc.world.noise.Noise2D;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluid;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.blocks.CreepingWaterPlantBlock;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;

public class CreepingOceanPlantFeature extends Feature<CreepingPlantConfig> {
    public CreepingOceanPlantFeature(Codec<CreepingPlantConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CreepingPlantConfig> context) {
        final WorldGenLevel level = context.level();
        final Seed seed = Seed.of(level.getSeed());
        final Noise2D maxTideHeight = TFGBiomeNoise.shoreTideLevelNoise(seed);
        final BlockPos pos = context.origin();
        final BlockState state = context.config().block().defaultBlockState();
        final int radius = context.config().radius();
        final int height = context.config().height();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        boolean isSuccessful = false;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y < height; y++) {
                    if (x * x + z * z < radius * radius && context.random().nextFloat() < context.config().integrity()) {
                        cursor.setWithOffset(pos, x, y, z);
                        int heightAboveTide = state.is(TFGTags.Blocks.IsAnemone) ? -1 : 2;
                        if (EnvironmentHelpers.isWorldgenReplaceable(level, cursor) && cursor.getY() <= heightAboveTide + maxTideHeight.noise(cursor.getX(), cursor.getZ())) {
                            final BlockState newState = CreepingWaterPlantBlock.updateStateFromSides(level, cursor, state);
                            if (!newState.isAir()) {
                                final Fluid fluidAt = level.getFluidState(cursor).getType();
                                final boolean isOpen = fluidAt.isSame(TFCFluids.SALT_WATER.getSource());
                                final BlockState waterloggedState = FluidHelpers.fillWithFluid(newState, fluidAt);
                                if (waterloggedState != null) {
                                    setBlock(level, cursor, waterloggedState.setValue(TFGBlockProperties.OPEN, isOpen));
                                    isSuccessful = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return isSuccessful;
    }
}
