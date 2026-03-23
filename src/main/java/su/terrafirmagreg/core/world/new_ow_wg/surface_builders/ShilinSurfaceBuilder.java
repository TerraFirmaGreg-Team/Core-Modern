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
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class ShilinSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory INSTANCE = ShilinSurfaceBuilder::new;

    private final Noise2D ridges;
    private final TFGSimpleSurfaceStates simpleStates;

    public ShilinSurfaceBuilder(long seed) {
        this.ridges = TFGBiomeNoise.shilinRidges(seed);
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final double val = ridges.noise(context.pos().getX(), context.pos().getZ());
        if (val > 0.18) {
            TFGNormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY, simpleStates.SNOWY_RAW, SurfaceStates.RAW, SurfaceStates.RAW);
        } else if (val > 0.09) {
            TFGNormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY, simpleStates.SNOWY_GRAVEL, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL);
        } else {
            TFGNormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY);
        }
    }
}
