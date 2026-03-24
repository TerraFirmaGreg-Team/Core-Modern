/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGNoiseHelpers;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class IceSheetShieldVolcanoSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory ICE_SHEET = seed -> new IceSheetShieldVolcanoSurfaceBuilder(seed,
            TFGBiomeNoise.glaciatedShieldVolcano(seed,
                    TFGBiomeNoise.hotSpotIntensity(seed)),
            TFGNoiseHelpers.max(TFGBiomeNoise.iceSheetSurfaceHeight(seed),
                    TFGBiomeNoise.shieldVolcanoIceSheetSurface(seed, TFGBiomeNoise.hotSpotIntensity(seed))),
            false, true, SEA_LEVEL_Y);
    public static final SurfaceBuilderFactory GLACIATED = seed -> new IceSheetShieldVolcanoSurfaceBuilder(seed,
            TFGBiomeNoise.glaciatedShieldVolcano(seed,
                    TFGBiomeNoise.hotSpotIntensity(seed)),
            TFGNoiseHelpers.max(TFGBiomeNoise.iceSheetSurfaceHeight(seed),
                    TFGBiomeNoise.shieldVolcanoGlacierSurface(seed, TFGBiomeNoise.hotSpotIntensity(seed))),
            false, true, SEA_LEVEL_Y + 30);

    private final long seed;
    private final Noise2D iceSurfaceNoise;
    private final Noise2D baseNoise;
    private final boolean hasMoraines;
    private final boolean hasStonyPeaks;
    private final int minFreezingHeight;
    private final TFGSimpleSurfaceStates simpleStates;
    private final SurfaceBuilder baseVolcanoSurfaceBuilder;

    IceSheetShieldVolcanoSurfaceBuilder(long seed, Noise2D baseNoise, Noise2D iceSurfaceNoise, boolean hasMoraines, boolean hasStonyPeaks, int minFreezingHeight) {
        this.seed = seed;
        this.baseNoise = baseNoise;
        this.iceSurfaceNoise = iceSurfaceNoise;
        this.hasMoraines = hasMoraines;
        this.hasStonyPeaks = hasStonyPeaks;
        this.minFreezingHeight = minFreezingHeight;
        this.baseVolcanoSurfaceBuilder = ShieldVolcanoSurfaceBuilder.DORMANT.apply(seed);
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final int x = context.pos().getX();
        final int z = context.pos().getZ();

        final int glacierBaseHeight = (int) Math.ceil(baseNoise.noise(x, z));
        final int glacierSurfaceHeight = (int) Math.ceil(iceSurfaceNoise.noise(x, z));

        int iceDepth;
        // Base Groundwater check allows for exposed ice near where rivers cut into ice sheet
        if (hasMoraines && context.rainfall() <= 100f) {
            final double moraineCrestHeight = Math.min((0.5 * (glacierSurfaceHeight + glacierBaseHeight)), glacierBaseHeight + 18);
            iceDepth = Math.max((int) ((startY - moraineCrestHeight) * 2), 0);
        } else {
            iceDepth = 35;
        }

        final int seaLevel = context.getSeaLevel();
        if (startY <= seaLevel) {
            ShoreAndOceanSurfaceBuilder.OLD_SHIELD_VOLCANO.apply(seed).buildSurface(context, startY, endY);
        } else if (startY < minFreezingHeight || (hasStonyPeaks && startY > glacierSurfaceHeight + 2.5) || (startY < glacierBaseHeight - 1.5)) {
            this.baseVolcanoSurfaceBuilder.buildSurface(context, startY, endY);
        } else {
            int surfaceDepth = -1;
            int surfaceY = 0;

            final SurfaceState snowState = simpleStates.SNOW;
            final SurfaceState iceState = simpleStates.PACKED_ICE;
            final SurfaceState blueIceState = simpleStates.BLUE_ICE;
            final SurfaceState moraineTopState = simpleStates.SNOWY_BASALT_MORAINE;
            final SurfaceState moraineState = simpleStates.BASALT_MORAINE;
            final SurfaceState basaltState = simpleStates.BASALT;

            for (int y = startY; y >= glacierBaseHeight - 22; --y) {
                final BlockState stateAt = context.getBlockState(y);
                if (stateAt.isAir()) {
                    surfaceDepth = -1; // Reached air, reset surface depth
                } else if (context.isDefaultBlock(stateAt)) {
                    // All in this if statement only occurs on the first cycle/when air resets the cycle
                    if (surfaceDepth == -1) {
                        surfaceY = y; // Reached surface. Place top state and switch to subsurface layers

                        surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 5, -3);
                        if (surfaceDepth <= -1) {
                            // skip placing snow on steep slopes
                            if (iceDepth < 1) {
                                context.setBlockState(y, moraineState);
                            }
                            // avoids placing ice on steep slopes where glacier base height = terrain height
                            else if (y <= glacierBaseHeight) {
                                iceDepth = 0;
                            } else {
                                context.setBlockState(y, iceState);
                            }
                        } else if (iceDepth == 0 || y <= seaLevel || y < glacierBaseHeight) {
                            // Skip placing snow where there is no glacier, or underwater
                            context.setBlockState(y, moraineState);
                        } else {
                            context.setBlockState(y, snowState);
                        }
                        surfaceDepth = 1;
                    } else if (iceDepth > 0 && y > glacierBaseHeight) {
                        // Subsurface layers
                        iceDepth--;
                        context.setBlockState(y, y < glacierSurfaceHeight - 16 ? blueIceState : iceState);
                    } else if (y > glacierBaseHeight) {
                        // Subsurface layers
                        context.setBlockState(y, y == startY ? moraineTopState : moraineState);
                    } else {
                        // Subsurface layers
                        context.setBlockState(y, basaltState);
                    }
                }
            }
        }
    }
}
