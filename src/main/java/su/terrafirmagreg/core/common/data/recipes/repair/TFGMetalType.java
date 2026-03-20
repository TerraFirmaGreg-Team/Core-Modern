package su.terrafirmagreg.core.common.data.recipes.repair;

import java.util.Locale;

/**
 * Metal ids used with forge ingot tags, TFC ingot item ids, and similar paths. Each constant maps to a lowercase name via
 * getSerializedName(), same style as datapacks and scripts. Actual repair recipes are defined elsewhere; this enum is only a
 * shared list of materials.
 */

public enum TFGMetalType {

    COPPER,
    BISMUTH_BRONZE,
    BLACK_BRONZE,
    BRONZE,
    WROUGHT_IRON,
    STEEL,
    BLACK_STEEL,
    BLUE_STEEL,
    RED_STEEL,

    IRON,
    TIN,
    LEAD,
    ZINC,
    SILVER,
    GOLD,
    NICKEL,
    PLATINUM,
    ALUMINIUM,
    TITANIUM,
    TUNGSTEN,
    COBALT,
    ROSE_GOLD,
    INVAR,
    STAINLESS_STEEL,
    ULTIMET,
    TUNGSTEN_CARBIDE,
    DAMASCUS_STEEL,
    TUNGSTEN_STEEL,
    COBALT_BRASS,
    VANADIUM_STEEL,
    NAQUADAH,
    NAQUADAH_ALLOY,
    TRINIUM,
    NEUTRONIUM,
    DURANIUM,
    DARMSTADTIUM,
    HSSG,
    HSSS,
    HSSE;

    private final String serializedName;

    TFGMetalType() {
        this.serializedName = name().toLowerCase(Locale.ROOT);
    }

    public String getSerializedName() {
        return serializedName;
    }
}
