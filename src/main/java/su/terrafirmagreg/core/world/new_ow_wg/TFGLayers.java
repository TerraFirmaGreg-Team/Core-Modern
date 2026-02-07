package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.world.layer.TFCLayers;

public class TFGLayers {

    public static void init() {
    }

    public static final int MUD_FLATS;
    public static final int SALT_FLATS;
    public static final int DUNE_SEA;
    public static final int GRASSY_DUNES;

    static {
        MUD_FLATS = TFCLayers.register(() -> TFGBiomes.MUD_FLATS);
        SALT_FLATS = TFCLayers.register(() -> TFGBiomes.SALT_FLATS);
        DUNE_SEA = TFCLayers.register(() -> TFGBiomes.DUNE_SEA);
        GRASSY_DUNES = TFCLayers.register(() -> TFGBiomes.GRASSY_DUNES);
    }
}
