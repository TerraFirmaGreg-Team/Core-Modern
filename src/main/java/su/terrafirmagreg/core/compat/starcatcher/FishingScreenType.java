package su.terrafirmagreg.core.compat.starcatcher;

import net.minecraft.resources.ResourceLocation;

import lombok.Getter;

/**
 * Enum representing different screen types for the fishing minigame.
 * Each screen type has its own tank texture and condition.
 */
@Getter
public enum FishingScreenType {
    SURFACE("surface"),
    SURFACE_WARM("surface_warm"),
    SURFACE_COLD("surface_cold"),
    CAVE("cave"),
    NETHER("nether"),
    MARS("mars"),
    VENUS("venus"),
    END("end");

    private final String name;
    private final ResourceLocation texture;

    FishingScreenType(String name) {
        this.name = name;
        this.texture = ResourceLocation.fromNamespaceAndPath("starcatcher", "textures/gui/minigame/" + name + ".png");
    }

}
