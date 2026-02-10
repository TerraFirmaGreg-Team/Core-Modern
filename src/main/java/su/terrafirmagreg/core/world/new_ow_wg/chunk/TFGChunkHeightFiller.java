package su.terrafirmagreg.core.world.new_ow_wg.chunk;

import java.util.Arrays;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.BiomeNoiseSampler;
import net.dries007.tfc.world.ChunkBiomeSampler;
import net.dries007.tfc.world.ChunkHeightFiller;
import net.dries007.tfc.world.ChunkNoiseFiller;
import net.dries007.tfc.world.biome.BiomeBlendType;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.region.RegionPartition;
import net.dries007.tfc.world.region.RiverEdge;
import net.dries007.tfc.world.region.Units;
import net.dries007.tfc.world.river.Flow;
import net.dries007.tfc.world.river.MidpointFractal;
import net.dries007.tfc.world.river.RiverInfo;
import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverNoiseSampler;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreNoiseSampler;

public class TFGChunkHeightFiller {
    protected static final int RIVER_TYPE_NONE = TFGRiverBlendType.NONE.ordinal();
    protected static final int RIVER_TYPE_CAVE = TFGRiverBlendType.CAVE.ordinal();

    protected final Map<BiomeExtension, BiomeNoiseSampler> biomeNoiseSamplers; // Biome -> Noise Samplers
    protected final Object2DoubleMap<BiomeNoiseSampler> columnBiomeNoiseSamplers; // Per column weighted map of biome noises samplers

    protected final Object2DoubleMap<BiomeExtension>[] sampledBiomeWeights; // 7x7 array of biome weights, at quart pos resolution
    protected final Object2DoubleMap<BiomeExtension> biomeWeights1; // Local biome weights, for individual column adjustment

    // Rivers
    protected final BiomeSourceExtension biomeSource;
    protected final Map<TFGRiverBlendType, TFGRiverNoiseSampler> riverNoiseSamplers;
    protected final double[] riverBlendWeights; // Indexed by RiverBlendType.ordinal

    // Shores
    protected final Map<ShoreBlendType, ShoreNoiseSampler> shoreNoiseSamplers;
    protected final double[] shoreBlendWeights; // Indexed by ShoreBlendType.ordinal
    protected final int seaLevel;
    protected final Noise2D tideHeightNoise;

    protected int blockX, blockZ; // Absolute x/z positions
    protected int localX, localZ; // Chunk-local x/z

    public TFGChunkHeightFiller(Object2DoubleMap<BiomeExtension>[] sampledBiomeWeights, BiomeSourceExtension biomeSource, Map<BiomeExtension, BiomeNoiseSampler> biomeNoiseSamplers,
            Map<TFGRiverBlendType, TFGRiverNoiseSampler> riverNoiseSamplers, Map<ShoreBlendType, ShoreNoiseSampler> shoreNoiseSamplers, int seaLevel, Noise2D tideHeightNoise) {
        this.biomeNoiseSamplers = biomeNoiseSamplers;
        this.columnBiomeNoiseSamplers = new Object2DoubleOpenHashMap<>();
        this.sampledBiomeWeights = sampledBiomeWeights;
        this.biomeWeights1 = new Object2DoubleOpenHashMap<>();

        this.biomeSource = biomeSource;
        this.riverNoiseSamplers = riverNoiseSamplers;
        this.riverBlendWeights = new double[TFGRiverBlendType.SIZE];

        this.shoreNoiseSamplers = shoreNoiseSamplers;
        this.shoreBlendWeights = new double[ShoreBlendType.SIZE];
        this.seaLevel = seaLevel;
        this.tideHeightNoise = tideHeightNoise;
    }

    /**
     * Samples the height at a specific location. This is the only exposed public method available
     * from {@link ChunkHeightFiller}, any other manipulations require a complete {@link ChunkNoiseFiller}
     *
     * @param blockX The block X.
     * @param blockZ The block Z.
     * @return The approximate height of the world at that location.
     */
    public double sampleHeight(int blockX, int blockZ) {
        setupColumn(blockX, blockZ);
        prepareColumnBiomeWeights();
        return sampleColumnHeightAndBiome(biomeWeights1, false);
    }

    /**
     * Initializes {@link #biomeWeights1} from the sampled biome weights
     */
    protected final void prepareColumnBiomeWeights() {
        ChunkBiomeSampler.sampleBiomesColumn(biomeWeights1, sampledBiomeWeights, localX, localZ);
    }

    /**
     * For a given (x, z) position, samples the provided biome weight map to calculate the height at that location, and the biome
     *
     * @param useCache If, in the stateful implementation, arrays corresponding to position within the chunk should be updated.
     * @return The maximum height at this location
     */
    protected final double sampleColumnHeightAndBiome(Object2DoubleMap<BiomeExtension> biomeWeights, boolean useCache) {
        columnBiomeNoiseSamplers.clear();

        double height = 0, normalHeight = 0, shoreHeight = 0, shoreBaseHeight = 0, oceanHeight = 0;
        double shoreWeight = 0, oceanWeight = 0;

        BiomeExtension biomeAt = null, normalBiomeAt = null, shoreBiomeAt = null, oceanBiomeAt = null;
        double maxNormalWeight = 0, maxShoreWeight = 0, maxOceanWeight = 0; // Partition on biome type

        boolean anySaltyBiomesNearby = false;
        for (Object2DoubleMap.Entry<BiomeExtension> entry : biomeWeights.object2DoubleEntrySet()) {
            final double biomeWeight = entry.getDoubleValue();
            final BiomeExtension biome = entry.getKey();
            final BiomeNoiseSampler sampler = biomeNoiseSamplers.get(biome);

            if (biome.isSalty()) {
                anySaltyBiomesNearby = true;
            }

            assert sampler != null : "Non-existent sampler for biome: " + biome.key();

            if (columnBiomeNoiseSamplers.containsKey(sampler)) {
                columnBiomeNoiseSamplers.mergeDouble(sampler, biomeWeight, Double::sum);
            } else {
                sampler.setColumn(blockX, blockZ);
                columnBiomeNoiseSamplers.put(sampler, biomeWeight);
            }

            final double biomeHeight = biomeWeight * sampler.height();
            height += biomeHeight;

            if (biome.isShore()) {
                var extension = (IBiomeExtension) biome;

                shoreHeight += biomeHeight;
                shoreWeight += biomeWeight;
                shoreBaseHeight += biomeWeight * extension.tfg$getShoreBaseHeight();
                if (maxShoreWeight < biomeWeight) {
                    shoreBiomeAt = biome;
                    maxShoreWeight = biomeWeight;
                }
            } else if (biome.biomeBlendType() == BiomeBlendType.OCEAN) {
                oceanHeight += biomeHeight;
                oceanWeight += biomeWeight;
                if (maxOceanWeight < biomeWeight) {
                    oceanBiomeAt = biome;
                    maxOceanWeight = biomeWeight;
                }
            } else {
                normalHeight += biomeHeight;
                if (maxNormalWeight < biomeWeight) {
                    normalBiomeAt = biome;
                    maxNormalWeight = biomeWeight;
                }
            }
        }

        biomeAt = normalBiomeAt;
        computeInitialShoreWeights(biomeWeights);

        final double landWeight = 1 - oceanWeight - shoreWeight;
        if (shoreWeight > 0 && shoreBiomeAt != null) {
            height = adjustHeightForShoreContributions(height, oceanWeight, landWeight, shoreWeight, maxShoreWeight, shoreBiomeAt, shoreHeight, normalHeight);
            if (shoreWeight > 0.5)
                biomeAt = shoreBiomeAt;
        }

        if (biomeAt == null) {
            biomeAt = oceanBiomeAt;
        }

        if (oceanWeight >= 0.25) {
            final double tideAdjustedSeaEdgeHeight = tideHeightNoise.noise(blockX, blockZ) - 4;
            height = Mth.clampedMap(landWeight, 0.32, 0.36, Math.min(height, tideAdjustedSeaEdgeHeight), height);
        }

        assert biomeAt != null;

        computeInitialRiverWeights(biomeWeights);

        final double initialCaveWeight = adjustWeightsForRiverCaves();
        final RiverInfo info = sampleRiverInfo(useCache);

        height = adjustHeightForRiverContributions(height, info, initialCaveWeight);

        if (useCache) {
            updateLocalCaches(biomeWeights, biomeAt, info, height, anySaltyBiomesNearby);
        }

        return height;
    }

    protected void setupColumn(int x, int z) {
        this.blockX = x;
        this.blockZ = z;
        this.localX = x & 15;
        this.localZ = z & 15;
    }

    /**
     * Initializes {@link #shoreBlendWeights} from the biome weights, using the shore type of each biome.
     */
    private void computeInitialShoreWeights(Object2DoubleMap<BiomeExtension> biomeWeights) {
        // Sum weights by biome extension -> river blend type first
        Arrays.fill(shoreBlendWeights, 0d);
        for (Object2DoubleMap.Entry<BiomeExtension> entry : biomeWeights.object2DoubleEntrySet()) {
            var extension = (IBiomeExtension) entry.getKey();
            shoreBlendWeights[extension.tfg$getShoreBlendType().ordinal()] += entry.getDoubleValue();
        }
    }

    private double adjustHeightForShoreContributions(final double height, final double oceanWeight, final double landWeight, final double shoreWeight, final double thisWeight,
            final BiomeExtension biome, final double shoreHeight, final double normalHeight) {

        // Iterate through blend types, and sample once
        // Each sampler gets the original terrain height, modifies it, and is interpolated together
        double shoreBlendHeight = 0d;
        for (ShoreBlendType type : ShoreBlendType.ALL) {
            final double weight = shoreBlendWeights[type.ordinal()];
            final ShoreNoiseSampler sampler = shoreNoiseSamplers.get(type);
            if (type == ShoreBlendType.NONE) {
                shoreBlendHeight += weight * height;
            } else if (weight > 0) {
                final double newShoreHeight = sampler.setColumnAndSampleHeight(height, blockX, blockZ, oceanWeight, landWeight, shoreWeight, thisWeight, biome, shoreHeight, normalHeight);
                shoreBlendHeight += weight * newShoreHeight;
            }
        }
        return shoreBlendHeight;
    }

    /**
     * Initializes {@link #riverBlendWeights} from the biome weights, using the river type of each biome.
     */
    private void computeInitialRiverWeights(Object2DoubleMap<BiomeExtension> biomeWeights) {
        // Sum weights by biome extension -> river blend type first
        Arrays.fill(riverBlendWeights, 0d);
        for (Object2DoubleMap.Entry<BiomeExtension> entry : biomeWeights.object2DoubleEntrySet()) {
            riverBlendWeights[entry.getKey().riverBlendType().ordinal()] += entry.getDoubleValue();
        }
    }

    /**
     * Adjusts {@link #riverBlendWeights} to bias towards river caves, creating sharper cutoffs and preventing caves
     * from pinching off rivers.
     *
     * @return The initial weight of the river cave type.
     */
    private double adjustWeightsForRiverCaves() {
        // Adjust bias for river cave to create sharp cutoffs at borders, helps prevent caves from breaking up rivers
        final double initialCaveWeight = riverBlendWeights[RIVER_TYPE_CAVE];
        if (initialCaveWeight > 0) {
            final double totalWeight = 1.0 - riverBlendWeights[RIVER_TYPE_NONE];
            // Delegate weight entirely to the cave carver after a point, and let it handle interpolation into the mouth of the cave
            // This needs to be very carefully managed not to pinch off the edge, and interpolating a canyon and cave together leads to subpar results.
            // So, we supply the river carve weight (initial value) to the cave carver, and run it at 1.0 weight instead, which will create a smooth transition.
            final double adjustedCaveWeight = initialCaveWeight < 0.25 ? Mth.map(initialCaveWeight, 0.0, 0.25, 0, 0.1 * totalWeight) : totalWeight;

            for (TFGRiverBlendType type : TFGRiverBlendType.ALL) {
                final double weight = riverBlendWeights[type.ordinal()];
                riverBlendWeights[type.ordinal()] = weight * (1.0 - adjustedCaveWeight) / (1.0 - initialCaveWeight);
            }

            riverBlendWeights[RIVER_TYPE_CAVE] = adjustedCaveWeight;
        }

        return initialCaveWeight;
    }

    private double adjustHeightForRiverContributions(final double height, @Nullable RiverInfo info, double initialCaveWeight) {
        // Only perform river modifications if there's a river anywhere in sight
        if (info != null) {
            // Iterate through blend types, and sample once
            // Each sampler gets the original terrain height, modifies it, and is interpolated together
            double riverBlendHeight = 0d;
            for (TFGRiverBlendType type : TFGRiverBlendType.ALL) {
                final double weight = riverBlendWeights[type.ordinal()];
                final TFGRiverNoiseSampler sampler = riverNoiseSamplers.get(type);
                if (type == TFGRiverBlendType.NONE) {
                    riverBlendHeight += weight * height;
                } else if (weight > 0) {
                    final double riverHeight = sampler.setColumnAndSampleHeight(info, blockX, blockZ, height, initialCaveWeight, weight);
                    riverBlendHeight += weight * riverHeight;
                }
            }
            return riverBlendHeight;
        } else {
            // Otherwise, we do a hack here - we set the weights to 1.0 at 'NONE' instead.
            // So later when we sample noise, we don't call `noise()` on any samplers that didn't initialize, because the river info was null.
            Arrays.fill(riverBlendWeights, 0);
            riverBlendWeights[RIVER_TYPE_NONE] = 1.0;
            return height;
        }
    }

    protected void updateLocalCaches(Object2DoubleMap<BiomeExtension> biomeWeights, BiomeExtension biomeAt, @Nullable RiverInfo info, double height, boolean couldBeSalty) {
    }

    @Nullable
    protected RiverInfo sampleRiverInfo(boolean useCache) {
        return sampleRiverEdge(biomeSource.getPartition(blockX, blockZ));
    }

    @Nullable
    protected final RiverInfo sampleRiverEdge(RegionPartition.Point point) {
        final float limitDistInGridSq = 50f * 50f / (Units.GRID_WIDTH_IN_BLOCK * Units.GRID_WIDTH_IN_BLOCK);
        double minDist = limitDistInGridSq; // Only concern ourselves with rivers within a range of 50 ^2 blocks. This helps `maybeIntersect` fail more often.
        double minDistAdjusted = Float.MAX_VALUE;
        RiverEdge minEdge = null;

        double exactGridX = Units.blockToGridExact(blockX);
        double exactGridZ = Units.blockToGridExact(blockZ);

        for (RiverEdge edge : point.rivers()) {
            final MidpointFractal fractal = edge.fractal();
            if (fractal.maybeIntersect(exactGridX, exactGridZ, minDist)) {
                // Minimum by square distance would get us the closest edge, but would fail in the case some edges are wider than others
                // Since in most situations, we're actually concerned about distance / width, we want to have the one with the highest weight in that respect.
                final double dist = fractal.intersectDistance(exactGridX, exactGridZ);
                if (dist < limitDistInGridSq) // Extra check that we intersect at a shorter distance than can possibly affect this location
                {
                    final double distAdjusted = dist / edge.widthSq();
                    if (distAdjusted < minDistAdjusted) {
                        minDist = dist;
                        minDistAdjusted = distAdjusted;
                        minEdge = edge;
                    }
                }
            }
        }

        if (minEdge != null) {
            final double realWidth = minEdge.widthSq(exactGridX, exactGridZ);
            final Flow flow = minEdge.fractal().calculateFlow(exactGridX, exactGridZ);

            // minDist is in grid^2
            // convert it to block^2
            minDist *= Units.GRID_WIDTH_IN_BLOCK * Units.GRID_WIDTH_IN_BLOCK;

            return new RiverInfo(minEdge, flow, minDist, realWidth);
        }
        return null;
    }
}
