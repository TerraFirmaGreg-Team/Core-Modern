package su.terrafirmagreg.core.world.new_ow_wg.layers;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;

import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;

public enum TFGRegionEdgeBiomeLayer implements AdjacentTransformLayer {
    INSTANCE;

    @Override
    public int apply(AreaContext context, int north, int east, int south, int west, int center) {
        final Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);

        // >= 2 Adjacent border conditions
        if (TFGLayers.isLow(center)) {
            if (matcher.test(TFGLayers::isOcean) && matcher.test(TFGLayers::isMountains)) {
                return TFGLayers.OCEANIC_MOUNTAINS;
            } else if (matcher.test(TFGLayers::isOcean) && matcher.test(i -> i == TFGLayers.LOWLANDS)) {
                return TFGLayers.SALT_MARSH;
            }
        }

        // No mud/salt flats near oceans
        if (TFGLayers.isFlats(center)) {
            if (matcher.test(TFGLayers::isOcean) && matcher.test(TFGLayers::isFlats)) {
                return TFGLayers.CANYONS;
            }
        }

        if (center == TFGLayers.PLATEAU || center == TFGLayers.BADLANDS) {
            if (matcher.test(i -> i == TFGLayers.LOW_CANYONS || i == TFGLayers.LOWLANDS)) {
                return TFGLayers.HILLS;
            } else if (matcher.test(i -> i == TFGLayers.PLAINS || i == TFGLayers.HILLS)) {
                return TFGLayers.ROLLING_HILLS;
            }
        } else if (TFGLayers.isMountains(center)) {
            if (matcher.test(TFGLayers::isLow)) {
                return TFGLayers.ROLLING_HILLS;
            }
        }
        // Inverses of above conditions
        else if (center == TFGLayers.LOWLANDS || center == TFGLayers.LOW_CANYONS) {
            if (matcher.test(i -> i == TFGLayers.PLATEAU || i == TFGLayers.BADLANDS)) {
                return TFGLayers.HILLS;
            } else if (matcher.test(TFGLayers::isMountains)) {
                return TFGLayers.ROLLING_HILLS;
            }
        } else if (center == TFGLayers.PLAINS || center == TFGLayers.HILLS) {
            if (matcher.test(i -> i == TFGLayers.PLATEAU || i == TFGLayers.BADLANDS)) {
                return TFGLayers.HILLS;
            } else if (matcher.test(TFGLayers::isMountains)) {
                return TFGLayers.ROLLING_HILLS;
            }
        } else if (center == TFGLayers.DEEP_OCEAN_TRENCH) {
            if (matcher.test(i -> !TFGLayers.isOcean(i))) {
                return TFGLayers.OCEAN;
            }
        }
        return center;
    }
}
