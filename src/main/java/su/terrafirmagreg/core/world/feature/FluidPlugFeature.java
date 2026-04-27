package su.terrafirmagreg.core.world.feature;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Earth;

public class FluidPlugFeature extends Feature<NoneFeatureConfiguration> {

    public FluidPlugFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {

        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();

        final ChunkAccess chunk = level.getChunk(pos);
        final ChunkPos chunkPos = new ChunkPos(pos);
        final int chunkX = chunkPos.getMinBlockX(), chunkZ = chunkPos.getMinBlockZ();
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final RockData rockData = ChunkDataProvider.get(context.chunkGenerator()).get(chunk).getRockData();

        final int minY = context.chunkGenerator().getMinY();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                // Top down iteration, attempt to either fix unstable locations, or remove the offending blocks.
                final int baseHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkX + x, chunkZ + z);

                mutablePos.set(chunkX + x, baseHeight, chunkZ + z);

                for (int y = baseHeight; y >= minY; y--) {

                    mutablePos.setY(y);
                    BlockState stateAt = chunk.getBlockState(mutablePos);

                    if (stateAt.isAir()) {
                        if (solidify(level, chunk, rockData, mutablePos, mutablePos.north()))
                            continue;
                        if (solidify(level, chunk, rockData, mutablePos, mutablePos.east()))
                            continue;
                        if (solidify(level, chunk, rockData, mutablePos, mutablePos.south()))
                            continue;
                        if (solidify(level, chunk, rockData, mutablePos, mutablePos.west()))
                            continue;
                        solidify(level, chunk, rockData, mutablePos, mutablePos.above());
                    }
                }
            }
        }
        return true;
    }

    private boolean solidify(WorldGenLevel level, ChunkAccess chunk, RockData rockData, BlockPos thisPos, BlockPos otherPos) {
        FluidState fluidState = level.getBlockState(otherPos).getFluidState();
        if (!fluidState.isEmpty()) {
            BlockState state = getReplacementBlock(fluidState, rockData, thisPos);
            if (state != null) {
                chunk.setBlockState(thisPos, state, false);
                return true;
            }
        }
        return false;
    }

    private @Nullable BlockState getReplacementBlock(FluidState fluid, RockData rockData, BlockPos pos) {
        if (fluid.is(TFCFluids.SALT_WATER.getSource())) {
            return TFGBlocks_Earth.HALITE.getDefaultState();
        } else if (fluid.is(TFCFluids.SPRING_WATER.getSource())) {
            return Blocks.CALCITE.defaultBlockState();
        } else if (fluid.is(TFGFluids.MUDDY_WATER.getSource())) {
            return TFGBlocks_Earth.HARDENED_CLAY.getDefaultState();
        } else if (fluid.is(Fluids.WATER)) {
            return rockData.getRock(pos).hardened().defaultBlockState();
        }
        // Do nothing for lava
        return null;
    }
}
