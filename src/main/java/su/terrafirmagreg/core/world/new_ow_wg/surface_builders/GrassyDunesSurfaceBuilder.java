package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.TFGSimpleSurfaceStates;

public class GrassyDunesSurfaceBuilder implements SurfaceBuilder {

    private final Noise2D grassHeightVariationNoise;
    private final TFGSimpleSurfaceStates simpleStates;
    private final TFGComplexSurfaceStates complexStates;

    public GrassyDunesSurfaceBuilder(long seed) {
        grassHeightVariationNoise = new OpenSimplex2D(Seed.of(seed).next()).octaves(2).scaled(SEA_LEVEL_Y + 8, SEA_LEVEL_Y + 14).spread(0.08f);
        simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final double heightVariation = grassHeightVariationNoise.noise(context.pos().getX(), context.pos().getZ());
        final double trueSlope = context.getSlope();

        context.setSlope(trueSlope * (1 - context.weight()));

        if (startY > heightVariation && trueSlope < 5) {
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, complexStates.TOP_GRASS_TO_SAND, simpleStates.SAND, simpleStates.SAND, simpleStates.SAND, simpleStates.SAND);
        } else {
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, simpleStates.SNOWY_SAND, simpleStates.SAND, simpleStates.SAND, simpleStates.SAND, simpleStates.SAND);
        }
    }
}
