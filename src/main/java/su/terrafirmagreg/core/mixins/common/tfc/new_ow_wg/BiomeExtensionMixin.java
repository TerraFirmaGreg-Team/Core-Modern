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
    private boolean tfg$hasTuffCones;
    @Unique
    private boolean tfg$hasTuyas;
    @Unique
    private int tfg$tuffRingRarity;
    @Unique
    private int tfg$tuyaRarity;
    @Unique
    private int tfg$shoreBaseHeight;

    @Unique
    public void tfg$init(ShoreBlendType shoreBlendType, TFGRiverBlendType riverBlendType, boolean hasTuffCones, boolean hasTuyas, int tuffRingRarity, int tuyaRarity, int shoreBaseHeight) {
        this.tfg$shoreBlendType = shoreBlendType;
        this.tfg$riverBlendType = riverBlendType;
        this.tfg$hasTuffCones = hasTuffCones;
        this.tfg$hasTuyas = hasTuyas;
        this.tfg$tuffRingRarity = tuffRingRarity;
        this.tfg$tuyaRarity = tuyaRarity;
        this.tfg$shoreBaseHeight = shoreBaseHeight;
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
    public int tfg$getTuffRingRarity() {
        return this.tfg$tuffRingRarity;
    }

    @Override
    public int tfg$getTuyaRarity() {
        return this.tfg$tuyaRarity;
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
