package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

import su.terrafirmagreg.core.world.new_ow_wg.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.TFGSimpleSurfaceStates;

public class SimpleSurfaceBuilder implements SurfaceBuilder {
    private static TFGSimpleSurfaceStates simpleStates = TFGSimpleSurfaceStates.INSTANCE();
    private static TFGComplexSurfaceStates complexStates = TFGComplexSurfaceStates.INSTANCE();

    public static final SurfaceBuilderFactory ROCKY_SHORE = seed -> new SimpleSurfaceBuilder(
            SurfaceStates.RAW, SurfaceStates.RAW, SurfaceStates.GRAVEL, true);

    public static final SurfaceBuilderFactory ROCKY_VOLCANIC_SOIL = seed -> new SimpleSurfaceBuilder(
            complexStates.VOLCANIC_TOP_GRASS_TO_LOCAL_GRAVEL, complexStates.VOLCANIC_MID_DIRT_TO_LOCAL_GRAVEL, SurfaceStates.GRAVEL, true);

    public static final SurfaceBuilderFactory VOLCANIC_SOIL = seed -> new SimpleSurfaceBuilder(
            complexStates.VOLCANIC_TOP_GRASS_TO_LOCAL_GRAVEL, complexStates.VOLCANIC_MID_DIRT_TO_LOCAL_GRAVEL, SurfaceStates.GRAVEL, true);

    public static final SurfaceBuilderFactory OCEAN_MUD = seed -> new SimpleSurfaceBuilder(
            simpleStates.OCEAN_MUD, simpleStates.OCEAN_MUD, simpleStates.OCEAN_MUD, false);

    private final SurfaceState top;
    private final SurfaceState mid;
    private final SurfaceState water;
    private final boolean rockySurfaceBuilder;

    public SimpleSurfaceBuilder(SurfaceState top, SurfaceState mid, SurfaceState water, boolean rockySurfaceBuilder) {
        this.top = top;
        this.mid = mid;
        this.water = water;
        this.rockySurfaceBuilder = rockySurfaceBuilder;
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        if (rockySurfaceBuilder) {
            NormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY, top, mid, mid, water, water);
        } else {
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, top, mid, mid, water, water);
        }
    }
}
