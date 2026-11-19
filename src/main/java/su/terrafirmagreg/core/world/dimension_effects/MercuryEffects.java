package su.terrafirmagreg.core.world.dimension_effects;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class MercuryEffects extends DimensionSpecialEffects {

    public MercuryEffects() {
        super(Float.NaN, false, SkyType.NORMAL, true, false);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 vec3, float v) {
        return vec3;
    }

    @Override
    public boolean isFoggyAt(int i, int i1) {
        return false;
    }

    @Nullable
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }
}
