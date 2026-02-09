package su.terrafirmagreg.core.world.new_ow_wg;

import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;

/**
 * Accessor interface to work with BiomeExtensionMixin
 */

public interface IBiomeExtension {
    void tfg$init(ShoreBlendType shoreBlendType, TFGRiverBlendType riverBlendType, boolean hasTuffCones, boolean hasTuyas, int tuffRingRarity, int tuyaRarity, int shoreBaseHeight);

    boolean tfg$hasTuffRings();

    boolean tfg$hasTuyas();

    int tfg$getTuffRingRarity();

    int tfg$getTuyaRarity();

    int tfg$getShoreBaseHeight();

    ShoreBlendType tfg$getShoreBlendType();

    TFGRiverBlendType tfg$getRiverBlendType();
}
