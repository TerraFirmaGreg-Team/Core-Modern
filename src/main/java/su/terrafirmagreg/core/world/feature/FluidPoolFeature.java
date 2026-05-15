package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class FluidPoolFeature extends Feature<FluidPoolConfig> {

    public FluidPoolFeature(Codec<FluidPoolConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FluidPoolConfig> context) {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final var random = context.random();
        final FluidPoolConfig config = context.config();

        if (random.nextFloat() > config.spawnChance())
            return false;

        final int x = pos.getX();
        final int z = pos.getZ();
        final int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final ChunkData data = provider.get(level, pos);
        final RockSettings rock = data.getRockData().getRock(x, 0, z);
        final BlockState groundBlock = rock.hardened().defaultBlockState();

        final int poolRadius = Math.min(4, config.minRadius() + random.nextInt(Math.max(1, config.maxRadius() - config.minRadius() + 1)));
        final int poolDepth = config.minDepth() + random.nextInt(config.maxDepth() - config.minDepth() + 1);
        final int radiusSq = poolRadius * poolRadius;
        final int innerRadiusSq = (poolRadius - 1) * (poolRadius - 1);

        final BlockState fluidState = config.fluidState();
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        boolean placed = false;

        // Ground and Walls
        for (int dx = -poolRadius; dx <= poolRadius; dx++) {
            for (int dz = -poolRadius; dz <= poolRadius; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > radiusSq)
                    continue;

                mutablePos.set(x + dx, surfaceY - poolDepth, z + dz);
                if (!level.isOutsideBuildHeight(mutablePos)) {
                    level.getChunk(mutablePos).setBlockState(mutablePos, groundBlock, false);
                    placed = true;
                }

                if (distSq > innerRadiusSq) {
                    for (int dy = 0; dy < poolDepth; dy++) {
                        mutablePos.set(x + dx, surfaceY - dy, z + dz);
                        if (!level.isOutsideBuildHeight(mutablePos))
                            level.getChunk(mutablePos).setBlockState(mutablePos, groundBlock, false);
                    }
                }
            }
        }

        // Fluid
        for (int dx = -poolRadius; dx <= poolRadius; dx++) {
            for (int dz = -poolRadius; dz <= poolRadius; dz++) {
                if (dx * dx + dz * dz > innerRadiusSq)
                    continue;

                for (int dy = 0; dy < poolDepth; dy++) {
                    int currentY = surfaceY - dy;
                    if (level.isOutsideBuildHeight(currentY))
                        continue;

                    mutablePos.set(x + dx, currentY, z + dz);
                    level.getChunk(mutablePos).setBlockState(mutablePos, fluidState, false);
                    placed = true;
                }
            }
        }

        return placed;
    }
}
