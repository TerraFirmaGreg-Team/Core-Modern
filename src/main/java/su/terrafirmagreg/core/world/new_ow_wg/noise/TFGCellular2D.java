/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.noise;

import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.world.noise.FastNoiseLite;
import net.dries007.tfc.world.noise.Noise2D;

import it.unimi.dsi.fastutil.HashCommon;

public class TFGCellular2D implements Noise2D {

    private final int seed;
    private final double jitter;
    private final int sample;
    private double frequency;

    public TFGCellular2D(long seed) {
        this(seed, 0.43701595f, 1);
    }

    public TFGCellular2D(long seed, int sample) {
        this(seed, 0.43701595f, sample);
    }

    public TFGCellular2D(long seed, float jitter, int sample) {
        this.seed = HashCommon.long2int(seed);
        this.jitter = jitter;
        this.sample = sample;
        this.frequency = 1;
    }

    @Override
    public double noise(double x, double y) {
        return cell(x, y).noise();
    }

    @Override
    public @NotNull TFGCellular2D spread(double scaleFactor) {
        frequency *= scaleFactor;
        return this;
    }

    public Noise2D then(ToDoubleFunction<TFGCell> f) {
        return (x, y) -> f.applyAsDouble(cell(x, y));
    }

    public @NotNull TFGCell cell(double x, double y) {
        x *= frequency;
        y *= frequency;

        final int primeX = 501125321;
        final int primeY = 1136930381;

        int xr = FastNoiseLite.FastFloor(x);
        int yr = FastNoiseLite.FastFloor(y);

        double distance0 = Double.MAX_VALUE;
        double distance1 = Double.MAX_VALUE;
        double angle0 = -1;
        double closestCenterX = 0;
        double closestCenterY = 0;
        int closestHash = 0;
        int closestCellX = 0;
        int closestCellY = 0;

        int xPrimed = (xr - 1) * primeX;
        int yPrimedBase = (yr - 1) * primeY;

        for (int xi = xr - sample; xi <= xr + sample; xi++) {
            int yPrimed = yPrimedBase;

            for (int yi = yr - sample; yi <= yr + sample; yi++) {
                int hash = FastNoiseLite.Hash(seed, xPrimed, yPrimed);
                int idx = hash & (255 << 1);

                double vecX = xi + FastNoiseLite.RandVecs2D[idx] * jitter;
                double vecY = yi + FastNoiseLite.RandVecs2D[idx | 1] * jitter;

                double newDistanceX = vecX - x;
                double newDistanceY = vecY - y;
                double newAngle = TFGNoiseHelpers.diamondAngle(newDistanceX, newDistanceY);
                double newDistance = newDistanceX * newDistanceX + newDistanceY * newDistanceY;

                distance1 = FastNoiseLite.FastMax(FastNoiseLite.FastMin(distance1, newDistance), distance0);
                if (newDistance < distance0) {
                    distance0 = newDistance;
                    angle0 = newAngle;
                    closestHash = hash;

                    // Store the last computed centers
                    closestCenterX = vecX; // Cell 1 X
                    closestCenterY = vecY; // Cell 1 Y
                    closestCellX = xi; // Cell 2 X
                    closestCellY = yi; // Cell 2 Y
                }
                yPrimed += primeY;
            }
            xPrimed += primeX;
        }

        return new TFGCell(closestCenterX / frequency, closestCenterY / frequency, closestCellX, closestCellY, distance0, distance1, closestHash * (1 / 2147483648.0f), angle0);
    }

    /**
     * @param x     "X"-coordinate of cell center. X/Y coordinates of cells are not tied to in game coordinates
     * @param y     "Y"-coordinate of cell center
     * @param cx    "X"-coordinate of the nearest cell (C2) center
     * @param cy    "Y"-coordinate of the nearest cell (C2) center
     * @param f1    Distance to x, y
     * @param f2    Distance to cx, cy
     * @param noise Hash value of the cell, range 0-1
     * @param angle Diamond angle to the center
     */
    public record TFGCell(double x, double y, int cx, int cy, double f1, double f2, double noise, double angle) {
    }
}
