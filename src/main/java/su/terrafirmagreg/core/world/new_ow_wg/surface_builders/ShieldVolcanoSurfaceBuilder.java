package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.TFGComplexSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.TFGSimpleSurfaceStates;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;

public class ShieldVolcanoSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory ACTIVE = seed -> new ShieldVolcanoSurfaceBuilder(seed, true, false);
    public static final SurfaceBuilderFactory DORMANT = seed -> new ShieldVolcanoSurfaceBuilder(seed, false, false);
    public static final SurfaceBuilderFactory SHORE = seed -> new ShieldVolcanoSurfaceBuilder(seed, false, true);

    private final boolean hasLavaFlows;
    private final boolean isSandy;
    private final long seed;
    private final TFGSimpleSurfaceStates simpleStates;
    private final TFGComplexSurfaceStates complexStates;

    ShieldVolcanoSurfaceBuilder(long seed, boolean hasLavaFlows, boolean isSandy) {
        this.hasLavaFlows = hasLavaFlows;
        this.isSandy = isSandy;
        this.seed = seed;
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
        this.complexStates = TFGComplexSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final int x = context.pos().getX();
        final int z = context.pos().getZ();
        final SurfaceState top;
        final SurfaceState mid;
        final SurfaceState bot;
        final SurfaceState underwater;

        if (isSandy) {
            top = complexStates.VOLCANIC_SHORE_SAND;
            mid = complexStates.VOLCANIC_SHORE_SAND;
            bot = complexStates.VOLCANIC_SHORE_SANDSTONE;
            underwater = complexStates.VOLCANIC_SHORE_SAND;
        } else {
            top = complexStates.VOLCANIC_TOP_GRASS_TO_GRAVEL;
            mid = complexStates.VOLCANIC_MID_DIRT_TO_GRAVEL;
            bot = simpleStates.BASALT_GRAVEL;
            underwater = simpleStates.BASALT_GRAVEL;
        }

        if (!hasLavaFlows) {
            buildSurface(context, startY, endY, top, mid, bot, underwater);
        } else {
            final Noise2D smoothNoise = TFGBiomeNoise.lavaFlowMaterial(seed);
            final double noiseValue = smoothNoise.noise(x, z);
            final Noise2D lavaFlows = TFGBiomeNoise.lavaFlow(seed);
            final double flowValue = lavaFlows.noise(x, z);

            if (flowValue < 0.40)
                buildSurface(context, startY, endY, top, mid, bot, underwater);
            else if (flowValue < 0.50) {
                if (noiseValue > 0)
                    buildSurface(context, startY, endY, simpleStates.SNOWY_BASALT_GRAVEL, simpleStates.BASALT_GRAVEL, simpleStates.BASALT, simpleStates.BASALT_GRAVEL);
                else
                    buildSurface(context, startY, endY, top, mid, bot, underwater);
            } else if (flowValue < 0.75) {
                if (noiseValue > 0)
                    buildSurface(context, startY, endY, simpleStates.SNOWY_BASALT_GRAVEL, simpleStates.BASALT_GRAVEL, simpleStates.BASALT, simpleStates.BASALT_GRAVEL);
                else
                    buildSurface(context, startY, endY, simpleStates.SNOWY_BASALT_COBBLE, simpleStates.BASALT_COBBLE, simpleStates.BASALT, simpleStates.BASALT_COBBLE);
            } else {
                if (noiseValue > -0.6)
                    buildSurface(context, startY, endY, simpleStates.SNOWY_BASALT, simpleStates.BASALT, simpleStates.BASALT, simpleStates.BASALT_COBBLE);
                else
                    buildSurface(context, startY, endY, simpleStates.SNOWY_BASALT_COBBLE, simpleStates.BASALT_COBBLE, simpleStates.BASALT, simpleStates.BASALT_COBBLE);
            }
        }
    }

    public void buildSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topState, SurfaceState midState, SurfaceState underState, SurfaceState underWaterState) {
        int surfaceDepth = -1;
        int surfaceY = 0;
        boolean underwaterLayer = false, firstLayer = false;
        SurfaceState surfaceState = simpleStates.BASALT;

        int basaltDepth = (int) (20 * context.weight());

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
                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, -1, 5);
                        if (surfaceDepth < -1) {
                            // No surface layers
                            surfaceDepth = 0;
                            context.setBlockState(y, simpleStates.BASALT);
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
                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, -3, 5);
                        if (surfaceDepth < -1) {
                            // No surface layers
                            context.setBlockState(y, simpleStates.BASALT);
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
                            surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 0, 5);
                            if (underwaterLayer) {
                                surfaceState = underState;
                            }
                        }
                    }
                } else if (basaltDepth > 0) {
                    context.setBlockState(y, simpleStates.BASALT);
                    basaltDepth--;
                }
            }
        }
    }
}
