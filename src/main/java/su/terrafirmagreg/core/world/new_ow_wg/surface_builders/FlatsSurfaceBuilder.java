package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;

import su.terrafirmagreg.core.world.new_ow_wg.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.TFGSimpleSurfaceStates;

public class FlatsSurfaceBuilder implements SurfaceBuilder {
    private final TFGComplexSurfaceStates complexStates;
    private final SurfaceState top;
    private final SurfaceState mid;
    private final SurfaceState water;

    public FlatsSurfaceBuilder(boolean isMuddy) {
        TFGSimpleSurfaceStates simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        this.complexStates = TFGComplexSurfaceStates.INSTANCE();
        this.top = isMuddy ? simpleStates.DRY_MUD : simpleStates.SALTED_EARTH;
        this.mid = simpleStates.DRY_MUD;
        this.water = SurfaceStates.MUD;
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        if (startY < 66 && context.rainfall() < 25) {
        NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, top, mid, mid, water, water);
        } else {
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, complexStates.TOP_GRASS_TO_SAND, complexStates.MID_DIRT_TO_SAND, complexStates.UNDER_GRAVEL);
        }
    }
}
