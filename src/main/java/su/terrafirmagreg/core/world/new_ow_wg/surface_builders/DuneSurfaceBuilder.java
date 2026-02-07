package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

import su.terrafirmagreg.core.world.new_ow_wg.TFGSimpleSurfaceStates;

public class DuneSurfaceBuilder implements SurfaceBuilderFactory.Invariant {
    private final TFGSimpleSurfaceStates states;

    public DuneSurfaceBuilder(long seed) {
        this.states = TFGSimpleSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        context.setSlope(context.getSlope() * (1 - context.weight()));
        NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, states.SNOWY_SAND, states.SAND, states.SAND, states.SAND, states.SAND);
    }
}
