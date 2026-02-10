package su.terrafirmagreg.core.world.new_ow_wg.noise;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.noise.FastNoiseLite;

import it.unimi.dsi.fastutil.HashCommon;

public class TFGCellular2D extends Cellular2D {

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
        super(seed);
        this.seed = HashCommon.long2int(seed);
        this.jitter = jitter;
        this.sample = sample;
        this.frequency = 1;
    }

    @Override
    public @NotNull TFGCellular2D spread(double scaleFactor) {
        frequency *= scaleFactor;
        return this;
    }

    @Override
    public @NotNull Cell cell(double x, double y) {
        x *= frequency;
        y *= frequency;

        final int primeX = 501125321;
        final int primeY = 1136930381;

        int xr = FastNoiseLite.FastFloor(x);
        int yr = FastNoiseLite.FastFloor(y);

        double distance0 = Double.MAX_VALUE;
        double distance1 = Double.MAX_VALUE;
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

                double newDistance = (vecX - x) * (vecX - x) + (vecY - y) * (vecY - y);

                distance1 = FastNoiseLite.FastMax(FastNoiseLite.FastMin(distance1, newDistance), distance0);
                if (newDistance < distance0) {
                    distance0 = newDistance;
                    closestHash = hash;

                    // Store the last computed centers
                    closestCenterX = vecX;
                    closestCenterY = vecY;
                    closestCellX = xi;
                    closestCellY = yi;
                }
                yPrimed += primeY;
            }
            xPrimed += primeX;
        }

        return new Cell(closestCenterX / frequency, closestCenterY / frequency, closestCellX, closestCellY, distance0, distance1, closestHash * (1 / 2147483648.0f));

    }
}
