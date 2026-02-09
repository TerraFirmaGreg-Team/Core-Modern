package su.terrafirmagreg.core.world.new_ow_wg.layers;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;

import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;

public enum TFGShoreLayer implements AdjacentTransformLayer {
    INSTANCE;

    @Override
    public int apply(AreaContext context, int north, int east, int south, int west, int center) {
        Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
        if (!TFGLayers.isOcean(center) && TFGLayers.hasShore(center)) {
            if (matcher.test(TFGLayers::isOcean)) {
                return TFGLayers.shoreFor(center);
            }
        }
        return center;
    }
}
