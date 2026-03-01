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

    /**
     * Minimum of two noises.
     */
    public static Noise2D min(Noise2D noise, Noise2D other) {
        return (x, y) -> Math.min(noise.noise(x, y), other.noise(x, y));
    }

    /**
     * Used to generate varying-height cliffs starting at various noise values
     *
     * @param compareNoise value above which cliffs should be added
     * @param addendNoise  cliff height noise
     * @param slopeNoise multiplier between the slope of the base noise and the slope of the added cliff
     */
    public static Noise2D slopedCliffMap(Noise2D thisNoise, Noise2D compareNoise, Noise2D addendNoise, Noise2D slopeNoise) {
        return (x, z) -> {
            final double noise = thisNoise.noise(x, z);
            final double compare = compareNoise.noise(x, z);
            final double addend = addendNoise.noise(x, z);
            final double slope = slopeNoise.noise(x, z);
            // Well above the cliff, add the full cliff height amount
            if (noise > compare + addend) {
                return noise + addend;
            } else if (noise > compare) {
                return noise + Math.min((noise - compare) * slope, addend);
            } else {
                return noise;
            }
        };
    }

    public static Noise2D stretchZ(Noise2D noise, double stretch) {
        return (x, z) -> noise.noise(x, z / stretch);
    }

    public static Noise2D stretchX(Noise2D noise, double stretch) {
        return (x, z) -> noise.noise(x / stretch, z);
    }
}
