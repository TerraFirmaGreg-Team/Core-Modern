package su.terrafirmagreg.core.utils;

import net.minecraftforge.fml.ModList;

public enum TFGModsResolver {
    AE2("ae2"),
    ASTIKOR_CARTS("astikorcarts"),
    STEAM_AND_RAILS("railways"),
    FIRMA_LIFE("firmalife"),
    FIRMA_CIV("firmaciv"),
    CREATE("create"),
    TFC_AMBIENTAL("tfcambiental"),
    TFC("tfc"),
    GRAPPLEMOD("grapplemod"),
    RNR("rnr"),
    SPACE_GRAVITY("space_gravity");

    private final String name;
    private Boolean modLoaded;

    TFGModsResolver(String name) {
        this.name = name;
    }

    public boolean isLoaded() {
        if (this.modLoaded == null) {
            this.modLoaded = ModList.get().isLoaded(this.name);
        }
        return this.modLoaded;
    }
}
