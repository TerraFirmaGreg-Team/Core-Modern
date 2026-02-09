package su.terrafirmagreg.core.world.new_ow_wg.layers;

import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;

import su.terrafirmagreg.core.world.new_ow_wg.TFGLayers;

public enum TFGMoreShoresLayer implements AdjacentTransformLayer {
    INSTANCE;

    @Override
    public int apply(AreaContext context, int north, int east, int south, int west, int center) {
        if (center != TFGLayers.OCEAN) {
            //			Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
            //			if (matcher.test(layer -> layer == TFGLayers.TERRACE_LOWER))
            //			{
            //				return TFGLayers.TERRACE_UPPER;
            //			}
            //			if (matcher.test(layer -> layer == TFGLayers.SEA_STACKS))
            //			{
            //				return TFGLayers.SEA_STACKS;
            //			}
            //			if (matcher.test(layer -> layer == TFGLayers.TIDAL_FLATS && layer == TFGLayers.SHORE))
            //			{
            //				return TFGLayers.SHORE;
            //			}
            //			if (matcher.test(layer -> layer == TFGLayers.COASTAL_DUNES))
            //			{
            //				return TFGLayers.COASTAL_DUNES;
            //			}
            //			if (matcher.test(layer -> layer == TFGLayers.SETBACK_CLIFFS))
            //			{
            //				return TFGLayers.SETBACK_CLIFFS;
            //			}
            //			if (matcher.test(layer -> layer == TFGLayers.ROCKY_SHORES))
            //			{
            //				return TFGLayers.ROCKY_SHORES;
            //			}
            //			if (matcher.test(layer -> layer == TFGLayers.EMBAYMENTS))
            //			{
            //				return TFGLayers.EMBAYMENTS;
            //			}
        }
        return center;
    }
}
