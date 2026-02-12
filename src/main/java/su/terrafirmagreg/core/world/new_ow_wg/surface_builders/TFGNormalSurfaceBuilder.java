package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGComplexSurfaceStates;

public enum TFGNormalSurfaceBuilder implements SurfaceBuilderFactory.Invariant {
    INSTANCE(-1),
    ROCKY(-3);

    private static TFGComplexSurfaceStates complexStates = TFGComplexSurfaceStates.INSTANCE();
    private final int subsurfaceMinDepth;

    TFGNormalSurfaceBuilder(int subsurfaceMinDepth) {
        this.subsurfaceMinDepth = subsurfaceMinDepth;
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        buildSurface(context, startY, endY, complexStates.TOP_GRASS_TO_GRAVEL, complexStates.MID_DIRT_TO_GRAVEL, complexStates.UNDER_GRAVEL);
    }

    public void buildSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topCaveState, SurfaceState midCaveState, SurfaceState underCaveState, int caveHeight) {
        buildSurface(context, startY, endY, complexStates.TOP_GRASS_TO_GRAVEL, complexStates.MID_DIRT_TO_GRAVEL, complexStates.UNDER_GRAVEL, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL, topCaveState,
                midCaveState, underCaveState, caveHeight);
    }

    public void buildSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topState, SurfaceState midState, SurfaceState underState) {
        buildSurface(context, startY, endY, topState, midState, underState, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL);
    }

    public void buildSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topState, SurfaceState midState, SurfaceState underState, SurfaceState underWaterState,
            SurfaceState thinUnderWaterState) {
        buildSurface(context, startY, endY, topState, midState, underState, underWaterState, thinUnderWaterState, topState, midState, underState, SEA_LEVEL_Y);
    }

    public void buildSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topState, SurfaceState midState, SurfaceState underState, SurfaceState underWaterState,
            SurfaceState thinUnderWaterState, SurfaceState topCaveState, SurfaceState midCaveState, SurfaceState underCaveState, int caveHeight) {
        int surfaceDepth = -1;
        int surfaceY = 0;
        boolean underwaterLayer = false, firstLayer = false, hasPlacedFirstSurface = false;
        SurfaceState surfaceState = SurfaceStates.RAW;

        for (int y = startY; y >= endY; --y) {
            final BlockState stateAt = context.getBlockState(y);
            if (stateAt.isAir()) {
                surfaceDepth = -1; // Reached air, reset surface depth
                if (y <= caveHeight) // Use alternate surface states at depth
                {
                    topState = topCaveState;
                    midState = midCaveState;
                    underState = underCaveState;
                } else if (hasPlacedFirstSurface) {
                    topState = surfaceState;
                    midState = surfaceState;
                    underState = surfaceState;
                }
            } else if (context.isDefaultBlock(stateAt)) {
                if (surfaceDepth == -1) {
                    surfaceY = y; // Reached surface. Place top state and switch to subsurface layers
                    firstLayer = true;
                    if (y < context.getSeaLevel() - 1) {
                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, -1);
                        if (surfaceDepth < -1) {
                            // No surface layers
                            surfaceDepth = 0;
                        } else if (surfaceDepth == -1) {
                            // Place one subsurface layer, skipping the top layer entirely
                            surfaceDepth = 0;
                            context.setBlockState(y, thinUnderWaterState);
                        } else {
                            context.setBlockState(y, underWaterState);
                        }
                        surfaceState = underWaterState;
                        underwaterLayer = true;
                    } else {
                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, subsurfaceMinDepth);
                        if (surfaceDepth < -1) {
                            // No surface layers
                            surfaceDepth = 0;
                        } else if (surfaceDepth == -1) {
                            surfaceDepth = 0;
                            context.setBlockState(y, underState);
                        } else {
                            context.setBlockState(y, topState);
                        }
                        surfaceState = midState;
                        underwaterLayer = false;
                    }
                } else if (surfaceDepth > 0) {
                    hasPlacedFirstSurface = true;

                    // Subsurface layers
                    surfaceDepth--;
                    context.setBlockState(y, surfaceState);
                    if (surfaceDepth == 0) {
                        // Next subsurface layer
                        if (firstLayer) {
                            firstLayer = false;
                            if (underwaterLayer) {
                                surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, 0);
                                surfaceState = thinUnderWaterState;
                            } else {
                                surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, 0);
                                surfaceState = underState;
                            }
                        }
                    }
                }
            }
        }
    }
}
