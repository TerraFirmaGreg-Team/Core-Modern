/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

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

    /**
     * Sum of a noise and a constant.
     */
    public static Noise2D addConstant(Noise2D noise, double constant) {
        return (x, y) -> noise.noise(x, y) + constant;
    }

    /**
     * Used to generate varying-height cliffs starting at various noise values
     *
     * @param compare value above which cliffs should be added
     * @param addend  cliff height noise
     */
    public static Noise2D cliffMap(Noise2D thisNoise, Noise2D compare, Noise2D addend) {
        return (x, z) -> {
            final double noise = thisNoise.noise(x, z);
            if (noise > compare.noise(x, z)) {
                return noise + addend.noise(x, z);
            } else {
                return noise;
            }
        };
    }

    public static double triangle(double amplitude, double midpoint, double frequency, double value) {
        return midpoint + amplitude * (Math.abs(4.0 * frequency * value + 1.0 - 4.0 * Mth.floor(frequency * value + 0.75)) - 1.0);
    }

    /**
     * Returns an approximate angle in the range [0, 4] where 4 is the equivalent of 360 degrees from a vector in the form x, y
     */
    public static double diamondAngle(double x, double y) {
        if (y >= 0)
            return (x >= 0 ? y / (x + y) : 1 - x / (-x + y));
        else
            return (x < 0 ? 2 - y / (-x - y) : 3 + x / (x - y));
    }

    /**
     * @return The average annual temperature adjusted for elevation above sea level
     */
    public static float adjustAverageTemperatureByElevation(int y, float averageTemperature, float seaLevel) {
        return averageTemperature - Mth.clamp((y - seaLevel) * 0.16225f, 0, 17.822f);
    }
}
