package su.terrafirmagreg.core.world.new_ow_wg;

/**
 * Accessor interface to work with BiomeExtensionMixin
 */

public interface IBiomeExtension {
    void tfg$init(ShoreBlendType shoreBlendType, boolean hasTuffCones, boolean hasTuyas, int tuffRingRarity, int tuyaRarity, int shoreBaseHeight);

    boolean tfg$hasTuffRings();

    boolean tfg$hasTuyas();

    int tfg$getTuffRingRarity();

    int tfg$getTuyaRarity();

    int tfg$getShoreBaseHeight();

    ShoreBlendType tfg$getShoreBlendType();
}
