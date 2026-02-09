package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGComplexSurfaceStates;

public class TFGRiverSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory INSTANCE = TFGRiverSurfaceBuilder::new;
    private final long seed;
    private final TFGComplexSurfaceStates complexStates;

    protected TFGRiverSurfaceBuilder(long seed) {
        this.seed = seed;
        complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final BiomeExtension biome = context.originalBiome();
        if (biome.isShore()) {
            biome.createSurfaceBuilder(seed).buildSurface(context, startY, endY);
        } else if (!biome.hasSandyRiverShores()) {
            TFGNormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY);
        } else {
            SurfaceState state = SurfaceStates.GRAVEL;
            if (context.getSlope() < 2) {
                state = complexStates.TOP_GRASS_TO_GRAVEL;
            } else if (context.getSlope() < 5) {
                state = SurfaceStates.RIVER_SAND;
            }
            TFGNormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, state, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL);
        }
    }
}
