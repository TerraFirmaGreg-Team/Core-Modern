package su.terrafirmagreg.core.world.dimension_effects;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

// Similar to the normal nether but without the close fog. Fog color is the same no matter the time of day

public class BeneathEffects extends DimensionSpecialEffects {

    public BeneathEffects() {
        super(Float.NaN, true, SkyType.NONE, false, true);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor, float brightness) {
        return fogColor;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return true;
    }

    @Nullable
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }
}
