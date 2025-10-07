package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.blocks.SnowPileBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGBlockProperties;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.SandLayerBlock;

// Most of this code is taken from TFC's OverworldClimateModel::onChunkLoad(),
// since that's where it does its initial snow placement
public class MartianPolesFeature extends Feature<MartianPolesConfig> {

    private static final int MARS_SEA_LEVEL = 88;

    private static final Noise2D snowPatchNoise;
    private static final Noise2D icePatchNoise;

    static {
        // Numbers taken from TFC
        snowPatchNoise = new OpenSimplex2D(372463264).octaves(2).spread(0.3f).scaled(-1, 1);
        icePatchNoise = new OpenSimplex2D(728648234).octaves(3).spread(0.6f);
    }

    public MartianPolesFeature(Codec<MartianPolesConfig> pCodec) {
        super(pCodec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MartianPolesConfig> context) {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final int startX = pos.getX();
        final int startZ = pos.getZ();
        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final ChunkData chunkData = provider.get(level, new ChunkPos(pos));

        // Early exit
        if (chunkData.getAverageTemp(startX, startZ) > context.config().maximumTemp()) {
            return false;
        }

        final int snowStartTemp = context.config().snowStartTemp();
        final int snowFinishTemp = context.config().snowFinishTemp();

        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final BlockState snowState = Blocks.SNOW.defaultBlockState();
        final BlockState piledSnowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2);
        final BlockState iceState = TFGBlocks.MARS_ICE.get().defaultBlockState();

        for (int x = startX; x <= startX + 15; x++) {
            for (int z = startZ; z <= startZ + 15; z++) {
                // Ocean floor heightmap so it goes through the big trees
                mutablePos.set(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z);

                float noise = (float) snowPatchNoise.noise(x, z);
                float temperature = chunkData.getAverageTemp(x, z);
                float snowTemperatureModifier = Mth.clampedMap(temperature, snowFinishTemp, snowStartTemp, -1, 1);

                // Handle snow
                BlockState stateAt = level.getBlockState(mutablePos);
                float snowTempNoise = snowTemperatureModifier + noise;
                if (snowTempNoise < 0) {
                    if ((stateAt.isAir() || stateAt.getBlock() instanceof SandLayerBlock) && snowState.canSurvive(level, mutablePos)) {
                        // Place snow
                        level.setBlock(mutablePos, snowTempNoise < -1 ? piledSnowState : snowState, 2);
                        mutablePos.move(Direction.DOWN);
                        level.setBlock(mutablePos, Helpers.setProperty(level.getBlockState(mutablePos), SnowyDirtBlock.SNOWY, true), 2);
                        mutablePos.move(Direction.UP);
                    } else if (SnowPileBlock.canPlaceSnowPile(level, mutablePos, stateAt)) {
                        // If it's waterlogged, don't snow pile it
                        if (!stateAt.hasProperty(TFGBlockProperties.SPACE_WATER) || stateAt.getValue(TFGBlockProperties.SPACE_WATER).getFluid() == Fluids.EMPTY) {
                            // Place snow pile
                            SnowPileBlock.placeSnowPile(level, mutablePos, stateAt, false);
                            level.setBlock(mutablePos, Helpers.setProperty(level.getBlockState(mutablePos), SnowyDirtBlock.SNOWY, true), 2);
                        }
                    }
                    // TODO: check if the below block is spice, and place a different layer instead?
                }

                // Handle ice
                mutablePos.set(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
                mutablePos.move(Direction.DOWN);
                stateAt = level.getBlockState(mutablePos);

                if (stateAt.is(TFGBlocks.MARS_WATER.get())) {
                    float threshold = (float) icePatchNoise.noise(x * 0.2f, z * 0.2f) + Mth.clamp(temperature * 0.1f, -0.2f, 0.2f);

                    int waterDepth = MARS_SEA_LEVEL - level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                    float waterDepthModifier = Mth.clampedMap(waterDepth, 0, 5, 0, 1);
                    float tempModifier = Mth.clampedMap(temperature, snowFinishTemp, snowStartTemp, -0.2f, 1);

                    if (waterDepthModifier + tempModifier < threshold && tempModifier < 1) {
                        level.setBlock(mutablePos, iceState, 3);
                    }
                }
            }
        }

        return true;
    }
}
