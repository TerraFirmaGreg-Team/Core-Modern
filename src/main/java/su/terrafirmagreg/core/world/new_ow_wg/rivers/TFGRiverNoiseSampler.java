/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.rivers;

import net.dries007.tfc.world.river.RiverInfo;

/**
 * River noise samplers are implemented as modifiers on the original results produced by {@link net.dries007.tfc.world.BiomeNoiseSampler}s.
 * Thus, they take in the {@code height} and {@code noise} values, and generally do their own interpolation / blending, based on the distance to the river in question.
 */
public interface TFGRiverNoiseSampler {
    TFGRiverNoiseSampler NONE = new TFGRiverNoiseSampler() {
    };

    default double setColumnAndSampleHeight(RiverInfo info, int x, int z, double heightIn, double caveWeight, double thisWeight) {
        return heightIn;
    }

    default double noise(int y, double noiseIn) {
        return noiseIn;
    }
}
