/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class RockyPlateauSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory INSTANCE = RockyPlateauSurfaceBuilder::new;

    private final long seed;
    private final TFGSimpleSurfaceStates simpleStates;
    private final TFGComplexSurfaceStates complexStates;

    public RockyPlateauSurfaceBuilder(long seed) {
        this.seed = seed;
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        this.complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final double weight = context.weight();
        final TFGNormalSurfaceBuilder surfaceBuilder = TFGNormalSurfaceBuilder.ROCKY;
        if (weight > 0.9 && startY < 86 && context.rainfall() == 0) {
            surfaceBuilder.buildSurface(context, startY, endY, simpleStates.SALTED_EARTH, simpleStates.DRY_MUD, SurfaceStates.RAW);
        } else if (startY - 2 > BiomeNoise.hills(seed, 22, 32).noise(context.pos().getX(), context.pos().getZ())) {
            surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.RAW, SurfaceStates.RAW, SurfaceStates.RAW);
        } else {
            surfaceBuilder.buildSurface(context, startY, endY, complexStates.TOP_GRASS_TO_SAND, complexStates.MID_DIRT_TO_SAND, SurfaceStates.RAW);
        }
    }
}
