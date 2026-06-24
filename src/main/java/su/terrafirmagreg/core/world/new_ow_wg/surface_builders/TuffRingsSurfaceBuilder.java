/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoiseSampler;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class TuffRingsSurfaceBuilder implements SurfaceBuilder {
    public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
        return seed -> new TuffRingsSurfaceBuilder(parent.apply(seed), Seed.of(seed));
    }

    private final SurfaceBuilder parent;
    private final Seed seed;
    private final TFGSimpleSurfaceStates simpleStates;
    private final TFGComplexSurfaceStates complexStates;

    public TuffRingsSurfaceBuilder(SurfaceBuilder parent, Seed seed) {
        this.parent = parent;
        this.seed = seed;
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        this.complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        var ctx = (ISurfaceBuilderContext) context;
        var tuffRingBiome = ctx.tfg$getTuffRingBiome();
        // Null when the surface context wasn't tfg$init-ed (eg classic-TFC pipeline)
        if (tuffRingBiome != null && ((IBiomeExtension) tuffRingBiome).tfg$hasTuffRings()) {
            final CenteredFeatureNoiseSampler sampler = CenteredFeatureNoise.tuffRing(seed);
            final float easing = sampler.calculateEasing(context.pos(), tuffRingBiome);
            if (easing > 0.6f) {
                if (startY < context.getSeaLevel() + 3) {
                    buildTuffSurface(context, startY, endY, complexStates.VOLCANIC_SHORE_SAND, complexStates.VOLCANIC_SHORE_SAND,
                            simpleStates.TUFF, simpleStates.TUFF_GRAVEL);
                } else {
                    buildTuffSurface(context, startY, endY, complexStates.VOLCANIC_TOP_GRASS_TO_TUFF_GRAVEL, complexStates.VOLCANIC_MID_DIRT_TO_TUFF_GRAVEL,
                            simpleStates.TUFF, simpleStates.TUFF_GRAVEL);
                }
                return;
            }
        }
        parent.buildSurface(context, startY, endY);
    }

    private void buildTuffSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topState, SurfaceState midState, SurfaceState underState, SurfaceState underWaterState) {
        int surfaceDepth = -1;
        int surfaceY = 0;
        boolean underwaterLayer = false, firstLayer = false;
        SurfaceState surfaceState = underState;

        int tuffDepth = (int) (20 * context.weight());

        for (int y = startY; y >= endY; --y) {
            final BlockState stateAt = context.getBlockState(y);
            if (stateAt.isAir()) {
                surfaceDepth = -1; // Reached air, reset surface depth
            } else if (context.isDefaultBlock(stateAt)) {
                // All in this if statement only occurs on the first cycle/when air resets the cycle
                if (surfaceDepth == -1) {
                    surfaceY = y; // Reached surface. Place top state and switch to subsurface layers
                    firstLayer = true;
                    if (y < context.getSeaLevel() - 1) {
                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, -1);
                        if (surfaceDepth < -1) {
                            // No surface layers
                            surfaceDepth = 0;
                            context.setBlockState(y, simpleStates.TUFF);
                        } else if (surfaceDepth == -1) {
                            // Place one subsurface layer, skipping the top layer entirely
                            surfaceDepth = 0;
                            context.setBlockState(y, underWaterState);
                        } else {
                            context.setBlockState(y, underWaterState);
                        }
                        surfaceState = underWaterState;
                        underwaterLayer = true;
                    } else {
                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, -3);
                        if (surfaceDepth < -1) {
                            // No surface layers
                            context.setBlockState(y, simpleStates.TUFF);
                            surfaceDepth = 0;
                        } else if (surfaceDepth == -1) {
                            // Place one subsurface layer, skipping the top layer entirely
                            surfaceDepth = 0;
                            context.setBlockState(y, underState);
                        } else {
                            context.setBlockState(y, topState);
                        }
                        surfaceState = midState;
                        underwaterLayer = false;
                    }
                } else if (surfaceDepth > 0) {
                    // Subsurface layers
                    surfaceDepth--;
                    context.setBlockState(y, surfaceState);
                    if (surfaceDepth == 0) {
                        // Next subsurface layer
                        if (firstLayer) {
                            firstLayer = false;
                            if (underwaterLayer) {
                                surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, 0);
                            } else {
                                surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, 0);
                                surfaceState = underState;
                            }
                        }
                    }
                } else if (tuffDepth > 0) {
                    context.setBlockState(y, simpleStates.TUFF);
                    tuffDepth--;
                }
            }
        }
    }
}
