package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class FluidGasVentFeature extends Feature<FluidGasVentConfig> {

    public FluidGasVentFeature(Codec<FluidGasVentConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FluidGasVentConfig> context) {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final var random = context.random();
        final FluidGasVentConfig config = context.config();

        final int x = pos.getX();
        final int z = pos.getZ();

        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final RockData rockData = provider.get(context.level(), pos).getRockData();

        final BlockState cobbleState = rockData.getRock(pos).cobble().defaultBlockState();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        final int baseRadius = config.baseRadius();
        for (int dx = -baseRadius - 1; dx <= baseRadius + 1; dx++) {
            for (int dz = -baseRadius - 1; dz <= baseRadius + 1; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                double effectiveRadius = baseRadius + (random.nextDouble() - 0.5) * 1.5;

                if (dist > effectiveRadius)
                    continue;

                int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz);

                mutablePos = mutablePos.set(x + dx, surfaceY - 1, z + dz);
                if (level.isOutsideBuildHeight(mutablePos))
                    continue;

                var existingState = level.getBlockState(mutablePos);
                if (!existingState.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES))
                    continue;

                if (dist < 1.5) {
                    setBlock(level, mutablePos, cobbleState);
                    if (random.nextFloat() < 0.8) {
                        setBlock(level, mutablePos.offset(0, 1, 0), config.ventState());
                    }
                    placed = true;
                } else if (random.nextFloat() < config.chance()) {
                    setBlock(level, mutablePos, cobbleState);
                    if (random.nextFloat() < 0.2) {
                        setBlock(level, mutablePos.offset(0, 1, 0), config.ventState());
                    }
                    placed = true;
                }
            }
        }

        return placed;
    }
}
