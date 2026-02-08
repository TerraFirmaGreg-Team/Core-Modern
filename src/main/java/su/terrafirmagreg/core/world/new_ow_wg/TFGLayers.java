package su.terrafirmagreg.core.world.new_ow_wg;

import net.dries007.tfc.world.layer.TFCLayers;

public class TFGLayers {

    public static void init() {
    }

    public static final int MUD_FLATS;
    public static final int SALT_FLATS;
    public static final int DUNE_SEA;
    public static final int GRASSY_DUNES;

    public static final int DEEP_OCEAN_TRENCH;
    public static final int DEEP_OCEAN;
    public static final int OCEAN;
    public static final int OCEAN_REEF;

    static {
        MUD_FLATS = TFCLayers.register(() -> TFGBiomes.MUD_FLATS);
        SALT_FLATS = TFCLayers.register(() -> TFGBiomes.SALT_FLATS);
        DUNE_SEA = TFCLayers.register(() -> TFGBiomes.DUNE_SEA);
        GRASSY_DUNES = TFCLayers.register(() -> TFGBiomes.GRASSY_DUNES);

        DEEP_OCEAN_TRENCH = TFCLayers.register(() -> TFGBiomes.DEEP_OCEAN_TRENCH);
        DEEP_OCEAN = TFCLayers.register(() -> TFGBiomes.DEEP_OCEAN);
        OCEAN = TFCLayers.register(() -> TFGBiomes.OCEAN);
        OCEAN_REEF = TFCLayers.register(() -> TFGBiomes.OCEAN_REEF);
    }

    public static boolean isOcean(int value) {
        return value == OCEAN || value == DEEP_OCEAN || value == DEEP_OCEAN_TRENCH || value == OCEAN_REEF;
    }

    public static boolean isFlats(int value) {
        return value == MUD_FLATS || value == SALT_FLATS;
    }

}
