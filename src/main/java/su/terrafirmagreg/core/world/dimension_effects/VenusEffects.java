package su.terrafirmagreg.core.world.dimension_effects;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

// No sun/moon, skybox is fog color, but less close than the nether, and with clouds.
// Fog color depends on the time of day.

public class VenusEffects extends DimensionSpecialEffects {

    public VenusEffects() {
        super(150f, true, SkyType.NORMAL, false, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        // Same as the overworld
        return color.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return true;
    }
}
