/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.noise;

import java.util.function.Function;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;

public enum CenteredFeatureBlendType {
    CINDER_CONE(CenteredFeatureNoise::cinder), // The original, small-cone volcanoes
    TUYA(CenteredFeatureNoise::tuya), // Flat-topped mounds formed around ice sheets
    TUFF_RING(CenteredFeatureNoise::tuffRing); // Rings of tuff, similar to Diamond Head, Molokini, etc.

    public static final CenteredFeatureBlendType[] ALL = values();
    public static final int SIZE = ALL.length;

    private final Function<Seed, CenteredFeatureNoiseSampler> factory;

    CenteredFeatureBlendType(Function<Seed, CenteredFeatureNoiseSampler> factory) {
        this.factory = factory;
    }

    public CenteredFeatureNoiseSampler createNoiseSampler(Seed seed) {
        return factory.apply(seed);
    }
}
