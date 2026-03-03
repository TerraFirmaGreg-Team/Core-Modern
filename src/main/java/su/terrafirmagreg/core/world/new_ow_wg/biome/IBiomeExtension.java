package su.terrafirmagreg.core.world.new_ow_wg.biome;

import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;

/**
 * Accessor interface to work with BiomeExtensionMixin
 */

public interface IBiomeExtension {
    void tfg$init(ShoreBlendType shoreBlendType, TFGRiverBlendType riverBlendType, int shoreBaseHeight, boolean hasTuffCones, boolean hasTuyas,
            int centeredFeatureRockHeight, int centeredFeatureBaseHeight, int centeredFeatureScaleHeight, boolean centeredFeatureIce);

    boolean tfg$hasTuffRings();

    boolean tfg$hasTuyas();

    int tfg$getShoreBaseHeight();

    ShoreBlendType tfg$getShoreBlendType();

    TFGRiverBlendType tfg$getRiverBlendType();

    int tfg$getCenteredFeatureRockHeight();

    int tfg$getCenteredFeatureBaseHeight();

    int tfg$getCenteredFeatureScaleHeight();

    boolean tfg$getCenteredFeatureIce();
}
