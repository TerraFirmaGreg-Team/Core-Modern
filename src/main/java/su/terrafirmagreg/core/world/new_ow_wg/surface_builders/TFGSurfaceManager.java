/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;

public class TFGSurfaceManager {
    private static Map<BiomeExtension, SurfaceBuilder> collectSurfaceBuilders(long worldSeed) {
        final ImmutableMap.Builder<BiomeExtension, SurfaceBuilder> builder = ImmutableMap.builder();
        for (BiomeExtension variant : TFGBiomes.getExtensions()) {
            builder.put(variant, variant.createSurfaceBuilder(worldSeed));
        }
        return builder.build();
    }

    private final Map<BiomeExtension, SurfaceBuilder> builders;

    public TFGSurfaceManager(long worldSeed) {
        this.builders = collectSurfaceBuilders(worldSeed);
    }

    public void buildSurface(LevelAccessor world, ChunkAccess chunk, RockLayerSettings rockLayerSettings, ChunkData chunkData, BiomeExtension[] accurateChunkBiomes,
            BiomeExtension[] accurateChunkBiomesNoRivers, double[] accurateChunkBiomeWeights, double[] slopeMap, RandomSource random, int seaLevel, int minY, BiomeExtension cinderConeBiome,
            BiomeExtension tuffRingBiome, BiomeExtension tuyaBiome) {
        final ChunkPos chunkPos = chunk.getPos();
        final int blockX = chunkPos.getMinBlockX(), blockZ = chunkPos.getMinBlockZ();

        SurfaceBuilderContext context = new SurfaceBuilderContext(world, chunk, chunkData, random, Seed.worldSeed, rockLayerSettings, seaLevel, minY);
        ISurfaceBuilderContext ctx = (ISurfaceBuilderContext) context;
        ctx.tfg$init(cinderConeBiome, tuffRingBiome, tuyaBiome);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                final int y = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + 1;
                final double slope = sampleSlope(slopeMap, x, z);

                final BiomeExtension biome = accurateChunkBiomes[x + 16 * z];
                final BiomeExtension originalBiome = accurateChunkBiomesNoRivers[x + 16 * z];
                final double weight = accurateChunkBiomeWeights[x + 16 * z];
                final SurfaceBuilder builder = builders.get(biome);

                context.buildSurface(biome, originalBiome, weight, biome.isSalty(), builder, blockX + x, y, blockZ + z, slope);
            }
        }
    }

    /**
     * Samples the 'slope' value for a given coordinate within the chunk
     * Expected values are in [0, 13] but are practically unbounded above
     */
    @SuppressWarnings("PointlessArithmeticExpression")
    private double sampleSlope(double[] slopeMap, int x, int z) {
        // compute slope contribution from lerp of corners
        final int offsetX = x + 2, offsetZ = z + 2;
        final int cellX = offsetX >> 2, cellZ = offsetZ >> 2;
        final double deltaX = ((double) offsetX - (cellX << 2)) * 0.25, deltaZ = ((double) offsetZ - (cellZ << 2)) * 0.25;

        double slope = 0;
        slope += slopeMap[(cellX + 0) + 6 * (cellZ + 0)] * (1 - deltaX) * (1 - deltaZ);
        slope += slopeMap[(cellX + 1) + 6 * (cellZ + 0)] * (deltaX) * (1 - deltaZ);
        slope += slopeMap[(cellX + 0) + 6 * (cellZ + 1)] * (1 - deltaX) * (deltaZ);
        slope += slopeMap[(cellX + 1) + 6 * (cellZ + 1)] * (deltaX) * (deltaZ);

        slope *= 0.8f;
        return slope;
    }
}
