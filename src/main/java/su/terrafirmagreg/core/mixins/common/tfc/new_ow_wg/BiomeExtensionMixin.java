package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.dries007.tfc.world.biome.BiomeExtension;

import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.rivers.TFGRiverBlendType;
import su.terrafirmagreg.core.world.new_ow_wg.shores.ShoreBlendType;

/**
 * Adds additional data to biome extensions that are new in 1.21
 */

@Mixin(value = BiomeExtension.class, remap = false)
public class BiomeExtensionMixin implements IBiomeExtension {

    @Unique
    private ShoreBlendType tfg$shoreBlendType;
    @Unique
    private TFGRiverBlendType tfg$riverBlendType;
    @Unique
    private int tfg$shoreBaseHeight;
    @Unique
    private boolean tfg$hasTuffCones;
    @Unique
    private boolean tfg$hasTuyas;
    @Unique
    private boolean tfg$centeredFeatureIce;
    @Unique
    private int tfg$centeredFeatureRockHeight;
    @Unique
    private int tfg$centeredFeatureBaseHeight;
    @Unique
    private int tfg$centeredFeatureScaleHeight;

    @Unique
    public void tfg$init(ShoreBlendType shoreBlendType, TFGRiverBlendType riverBlendType, int shoreBaseHeight, boolean hasTuffCones, boolean hasTuyas,
            int centeredFeatureRockHeight, int centeredFeatureBaseHeight, int centeredFeatureScaleHeight, boolean centeredFeatureIce) {
        this.tfg$shoreBlendType = shoreBlendType;
        this.tfg$riverBlendType = riverBlendType;
        this.tfg$shoreBaseHeight = shoreBaseHeight;
        this.tfg$hasTuffCones = hasTuffCones;
        this.tfg$hasTuyas = hasTuyas;
        this.tfg$centeredFeatureRockHeight = centeredFeatureRockHeight;
        this.tfg$centeredFeatureBaseHeight = centeredFeatureBaseHeight;
        this.tfg$centeredFeatureScaleHeight = centeredFeatureScaleHeight;
        this.tfg$centeredFeatureIce = centeredFeatureIce;
    }

    @Override
    public boolean tfg$hasTuffRings() {
        return this.tfg$hasTuffCones;
    }

    @Override
    public boolean tfg$hasTuyas() {
        return this.tfg$hasTuyas;
    }

    @Override
    public int tfg$getCenteredFeatureRockHeight() {
        return this.tfg$centeredFeatureRockHeight;
    }

    @Override
    public int tfg$getCenteredFeatureBaseHeight() {
        return this.tfg$centeredFeatureBaseHeight;
    }

    @Override
    public int tfg$getCenteredFeatureScaleHeight() {
        return this.tfg$centeredFeatureScaleHeight;
    }

    @Override
    public boolean tfg$getCenteredFeatureIce() {
        return this.tfg$centeredFeatureIce;
    }

    @Override
    public int tfg$getShoreBaseHeight() {
        return this.tfg$shoreBaseHeight;
    }

    @Override
    public ShoreBlendType tfg$getShoreBlendType() {
        return this.tfg$shoreBlendType;
    }

    @Override
    public TFGRiverBlendType tfg$getRiverBlendType() {
        return this.tfg$riverBlendType;
    }
}
