package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class TFGLowlandsSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory INSTANCE = TFGLowlandsSurfaceBuilder::new;

    private final Noise2D surfaceMaterialNoise;
    private final TFGSimpleSurfaceStates simpleStates;
    private final TFGComplexSurfaceStates complexStates;

    public TFGLowlandsSurfaceBuilder(long seed) {
        surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.04f);
        simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final float noise = (float) surfaceMaterialNoise.noise(context.pos().getX(), context.pos().getZ()) * 0.9f + context.random().nextFloat() * 0.1f;
        final SurfaceState mud = context.rainfall() < 130f ? simpleStates.DRY_MUD : SurfaceStates.MUD;
        TFGNormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, noise < 0f ? complexStates.TOP_GRASS_TO_GRAVEL : mud, mud, complexStates.MID_DIRT_TO_GRAVEL,
                noise > 0 ? complexStates.MID_DIRT_TO_GRAVEL : mud, mud);
    }
}
