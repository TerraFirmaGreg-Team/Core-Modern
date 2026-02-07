package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.world.biome.BiomeExtension;

/**
 * Shore noise samplers are implemented as modifiers on the original results produced by {@link net.dries007.tfc.world.BiomeNoiseSampler}s.
 * Thus, they take in the {@code height} and {@code noise} values, and generally do their own interpolation / blending, based on the relative
 * weights of land, ocean, and shore biomes.
 */
public interface ShoreNoiseSampler {
    ShoreNoiseSampler NONE = new ShoreNoiseSampler() {
    };

    default double setColumnAndSampleHeight(double heightIn, int x, int z, double oceanWeight, double landWeight, double shoreWeight, double thisWeight, BiomeExtension biome, double shoreHeight,
            double normalHeight) {
        return heightIn;
    }

    default double noise(int y, double noiseIn) {
        return noiseIn;
    }
}
