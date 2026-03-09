/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.surface_builders;

import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGNoiseHelpers;
import su.terrafirmagreg.core.world.new_ow_wg.surface_states.TFGSimpleSurfaceStates;

public class IceSheetSurfaceBuilder implements SurfaceBuilder {
    public static final SurfaceBuilderFactory NORMAL = seed -> new IceSheetSurfaceBuilder(seed,
            TFGBiomeNoise.glacialBase(seed),
            TFGBiomeNoise.iceSheetSurfaceHeight(seed),
            true, false, false);
    public static final SurfaceBuilderFactory EDGE = seed -> new IceSheetSurfaceBuilder(seed,
            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialBase(seed), 1.6),
            TFGBiomeNoise.iceSheetSurfaceHeight(seed),
            true, false, false);
    public static final SurfaceBuilderFactory EDGE_LAKE = seed -> new IceSheetSurfaceBuilder(seed,
            BiomeNoise.lake(seed),
            TFGBiomeNoise.iceSheetSurfaceHeight(seed),
            false, false, false);
    public static final SurfaceBuilderFactory HIDDEN_LAKE = seed -> new IceSheetSurfaceBuilder(seed,
            TFGBiomeNoise.glacialOceanicBase(seed),
            TFGBiomeNoise.iceSheetSurfaceHeight(seed),
            false, false, false);
    public static final SurfaceBuilderFactory ICE_SHEET_MOUNTAINS = seed -> new IceSheetSurfaceBuilder(seed,
            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirques(seed), 39),
            TFGNoiseHelpers.max(TFGBiomeNoise.montaneIceSheetSurfaceHeight(seed),
                    TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed), 39)),
            false, true, false);
    public static final SurfaceBuilderFactory GLACIATED_MOUNTAINS = seed -> new IceSheetSurfaceBuilder(seed,
            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirques(seed), 39),
            TFGNoiseHelpers.addConstant(TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed), 39),
            false, true, false);
    public static final SurfaceBuilderFactory OCEANIC = seed -> new IceSheetSurfaceBuilder(seed,
            TFGBiomeNoise.glacialOceanicBase(seed),
            TFGBiomeNoise.oceanicIceSheetSurfaceHeight(seed),
            false, false, true);
    public static final SurfaceBuilderFactory ICE_SHEET_OCEANIC_MOUNTAINS = seed -> new IceSheetSurfaceBuilder(seed,
            TFGBiomeNoise.glacialCirques(seed),
            TFGNoiseHelpers.max(TFGBiomeNoise.oceanicIceSheetSurfaceHeight(seed),
                    TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed)),
            false, true, true);
    public static final SurfaceBuilderFactory GLACIATED_OCEANIC_MOUNTAINS = seed -> new IceSheetSurfaceBuilder(seed,
            TFGBiomeNoise.glacialCirques(seed),
            TFGBiomeNoise.glacialCirquesIceSurfaceHeight(seed),
            false, true, true);

    private final long seed;
    private final Noise2D iceSurfaceNoise;
    private final Noise2D baseNoise;
    private final boolean hasMoraines;
    private final boolean hasStonyPeaks;
    private final boolean isShoreBiome;
    private final TFGSimpleSurfaceStates simpleStates;

    IceSheetSurfaceBuilder(long seed, Noise2D baseNoise, Noise2D iceSurfaceNoise, boolean hasMoraines, boolean hasStonyPeaks, boolean isShoreBiome) {
        this.seed = seed;
        this.baseNoise = baseNoise;
        this.iceSurfaceNoise = iceSurfaceNoise;
        this.hasMoraines = hasMoraines;
        this.hasStonyPeaks = hasStonyPeaks;
        this.isShoreBiome = isShoreBiome;
        this.simpleStates = TFGSimpleSurfaceStates.INSTANCE();
    }

    @Override
    public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
        final int seaLevel = context.getSeaLevel();

        final int x = context.pos().getX();
        final int z = context.pos().getZ();

        final int glacierBaseHeight = (int) Math.ceil(baseNoise.noise(x, z));
        final int glacierSurfaceHeight = (int) Math.ceil(iceSurfaceNoise.noise(x, z));

        int iceDepth;

        // Base Groundwater check allows for exposed ice near where rivers cut into ice sheet
        if (hasMoraines && context.rainfall() <= 20f) {
            final double moraineCrestHeight = Math.min((0.5 * (glacierSurfaceHeight + glacierBaseHeight)), glacierBaseHeight + 18);
            iceDepth = Math.max((int) ((startY - moraineCrestHeight) * 2), 0);
        } else {
            iceDepth = 35;
        }

        if (hasStonyPeaks && startY > glacierSurfaceHeight + 2.5) {
            TFGNormalSurfaceBuilder.ROCKY.buildSurface(context, startY, endY);
        } else if (startY < glacierBaseHeight - 1.5) {
            TFGNormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY);
        } else if (isShoreBiome && startY <= seaLevel) {
            ShoreAndOceanSurfaceBuilder.MOUNTAINS.apply(seed).buildSurface(context, startY, endY);
        } else {
            int surfaceDepth = -1;
            int surfaceY;

            final SurfaceState snowState = simpleStates.SNOW;
            final SurfaceState iceState = simpleStates.PACKED_ICE;
            final SurfaceState blueIceState = simpleStates.BLUE_ICE;
            final SurfaceState moraineTopState = simpleStates.SNOWY_MORAINE;
            final SurfaceState moraineState = simpleStates.MORAINE;

            for (int y = startY; y >= glacierBaseHeight - 2; --y) {
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
                            // And stop ice from being placed below this
                            iceDepth = 0;
                        } else {
                            context.setBlockState(y, snowState);
                        }
                        surfaceDepth = 1;
                    } else if (iceDepth > 0 && y > glacierBaseHeight) {
                        // Subsurface layers
                        iceDepth--;
                        context.setBlockState(y, y < glacierSurfaceHeight - 16 ? blueIceState : iceState);
                    } else {
                        // Subsurface layers
                        context.setBlockState(y, y == startY ? moraineTopState : moraineState);
                    }
                }
            }
        }
    }
}
