package su.terrafirmagreg.core.world.new_ow_wg.noise;

import net.dries007.tfc.world.noise.Noise2D;
import net.minecraft.util.Mth;

public class TFGNoiseHelpers {

    /**
     * Re-scales the output of the noise to a new range, clamped between the minimum and maximum values
     *
     * @param oldMin the old minimum value (typically -1)
     * @param oldMax the old maximum value (typically 1)
     * @param min    the new minimum value
     * @param max    the new maximum value
     * @return a new noise function
     */
    public static Noise2D clampedScaled(Noise2D noise, double oldMin, double oldMax, double min, double max) {
        final double scale = (max - min) / (oldMax - oldMin);
        final double shift = min - oldMin * scale;
        return (x, y) -> Mth.clamp(noise.noise(x, y) * scale + shift, min, max);
    }

    /**
     * Maximum of two noises.
     */
    public static Noise2D max(Noise2D noise, Noise2D other) {
        return (x, y) -> Math.max(noise.noise(x, y), other.noise(x, y));
    }
}
