/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class BurrenSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory INSTANCE = BurrenSurfaceBuilder::new;

    private final Noise2D crevices;
    private final TFGSimpleSurfaceStates simpleStates;
    private final TFGComplexSurfaceStates complexStates;

    public BurrenSurfaceBuilder(long seed) {
        this.crevices = TFGBiomeNoise.burrenCrevices(seed);
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        this.complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        if (crevices.noise(context.pos().getX(), context.pos().getZ()) + 0.3 * context.weight() <= 0.40) {
            TFGNormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY, complexStates.TOP_GRASS_TO_GRAVEL, SurfaceStates.RAW, SurfaceStates.RAW);
        } else {
            TFGNormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY, simpleStates.SNOWY_RAW, SurfaceStates.RAW, SurfaceStates.RAW);
        }
    }
}
