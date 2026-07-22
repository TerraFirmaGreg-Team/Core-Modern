package su.terrafirmagreg.core.client;

import net.minecraft.resources.ResourceLocation;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class TFGBlockPartials {
    public static final PartialModel TITANIUM_FLYWHEEL = get("steampowered", "titanium_flywheel/wheel");
    public static final PartialModel TITANIUM_FLYWHEEL_UPPER_ROTATING = get("steampowered", "titanium_flywheel/upper_rotating_connector");
    public static final PartialModel TITANIUM_FLYWHEEL_LOWER_ROTATING = get("steampowered", "titanium_flywheel/lower_rotating_connector");
    public static final PartialModel TITANIUM_FLYWHEEL_UPPER_SLIDING = get("steampowered", "titanium_flywheel/upper_sliding_connector");
    public static final PartialModel TITANIUM_FLYWHEEL_LOWER_SLIDING = get("steampowered", "titanium_flywheel/lower_sliding_connector");

    private static PartialModel get(String namespace, String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(namespace, "block/" + path));
    }

    public static void clientInit() {
    }
}
