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

public class PatternedGroundSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory INSTANCE = PatternedGroundSurfaceBuilder::new;

    private final TFGNormalSurfaceBuilder surfaceBuilder;
    private final Noise2D edgeNoise;

    public PatternedGroundSurfaceBuilder(long seed) {
        this.surfaceBuilder = TFGNormalSurfaceBuilder.INSTANCE;
        this.edgeNoise = TFGBiomeNoise.patternedGround(seed);
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        if (edgeNoise.noise(context.pos().getX(), context.pos().getZ()) * context.weight() >= -0.60) {
            surfaceBuilder.buildSurface(context, startY, endY);
        } else {
            surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.MUD, SurfaceStates.MUD, SurfaceStates.GRAVEL, SurfaceStates.MUD, SurfaceStates.MUD);
        }
    }
}
