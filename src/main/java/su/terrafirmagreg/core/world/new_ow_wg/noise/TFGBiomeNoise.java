/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.noise;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.*;
import net.dries007.tfc.world.region.Units;
import net.minecraft.util.Mth;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;

public class TFGBiomeNoise {
    /**
     * Signed version f connected valley noise, usable for creating asymmetrical features in valleys
     */
    public static Noise2D connectedValleyBaseNoise(long seed) {
        return new OpenSimplex2D(seed).spread(0.0025);
    }

    /**
     * Basic noise map used by several biomes with connected ridge-noise valleys
     */
    public static Noise2D connectedValleyNoise(long seed) {
        return connectedValleyBaseNoise(seed).abs();
    }

    /**
     * Very flat biome
     */
    public static Noise2D flats(long seed) {
        return new OpenSimplex2D(seed)
                .octaves(4)
                .spread(0.03f)
                .scaled(SEA_LEVEL_Y - 12, SEA_LEVEL_Y + 8)
                .clamped(SEA_LEVEL_Y, SEA_LEVEL_Y + 2);
    }

    /**
     * Noise just above sea level
     */
    public static Noise2D saltFlats(long seed) {
        return new OpenSimplex2D(seed)
                .octaves(4)
                .spread(0.05f)
                .scaled(SEA_LEVEL_Y - 16, SEA_LEVEL_Y + 10)
                .clamped(SEA_LEVEL_Y - 2, SEA_LEVEL_Y);
    }

    /**
     * Noise just above sea level
     */
    public static Noise2D dunes(long seed, int minHeight, int maxHeight) {
        return new OpenSimplex2D(seed)
                .spread(0.02)
                .scaled(-3, 3)
                .add((x, z) -> x / 6 + 20 * Math.sin(z / 240))
                .map(value -> 1.3 * (Math.abs((value % 5) - 1) * ((value % 5) - (value % 1) > 0 ? 0.5 : 2) - 1))
                .clamped(-1, 1)
                .lazyProduct(new OpenSimplex2D(seed)
                        .octaves(4)
                        .spread(0.1)
                        .scaled(-1, 2)
                        .clamped(0.4, 1))
                .scaled(SEA_LEVEL_Y + minHeight, SEA_LEVEL_Y + maxHeight);
    }

    /**
     * Shield volcanoes with minimal erosion, recent lava flows on surface, no/small calderas
     */
    public static Noise2D activeShieldVolcano(long seed, Noise2D hotspot) {
        final double edgeElev = SEA_LEVEL_Y + 1;
        final double calderaEdgeElev = SEA_LEVEL_Y + 115;
        final double cliffEdgeElev = SEA_LEVEL_Y + 90;
        final double calderaCenterElev = SEA_LEVEL_Y + 60;

        final Noise2D volcano = hotspot.map(y -> y < 0.75 ? Mth.map(y, 0, 0.75, edgeElev, calderaEdgeElev) // Slope upwards to mountain top or crater rim
                : y < 0.78 ? Mth.map(y, 0.75, 0.78, calderaEdgeElev, cliffEdgeElev) // Cliff at edge of crater
                        : Mth.map(y, 0.78, 1, cliffEdgeElev, calderaCenterElev)); // Interior of crater

        final Noise2D flows = lavaFlow(seed).map(y -> y < 0.45 ? 0 : 1);
        final OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03f).scaled(-100f, 100f);
        final Noise2D surface = new OpenSimplex2D(seed + 1)
                .octaves(4)
                .spread(0.06f)
                .warped(warp)
                .map(x -> x > 0.4 ? x - 0.8f : -x)
                .scaled(-0.4f, 0.8f, -8, 8);

        return volcano.add(flows).add(surface);

    }

    /**
     * Shield volcanoes with some erosion, no recent lava flows, large calderas with open sides
     */
    public static Noise2D dormantShieldVolcano(long seed, Noise2D hotspot) {
        final double seaElev = SEA_LEVEL_Y + 9;
        final double mtnBaseElev = SEA_LEVEL_Y + 40;
        final double calderaEdgeElev = SEA_LEVEL_Y + 70;
        final double cliffEdgeElev = SEA_LEVEL_Y + 50;
        final double calderaCenterElev = SEA_LEVEL_Y + 15;

        final Noise2D volcano = hotspot.map(y -> y < 0.45 ? Mth.map(y, 0, 0.45, seaElev, mtnBaseElev) // Coastal slopes
                : y < 0.7 ? Mth.map(y, 0.45, 0.7, mtnBaseElev, calderaEdgeElev) // Mountain side/slope up to caldera
                        : y < 0.73 ? Mth.map(y, 0.7, 0.73, calderaEdgeElev, cliffEdgeElev) // Caldera cliff
                                : y < 0.85 ? Mth.map(y, 0.73, 0.85, cliffEdgeElev, calderaCenterElev) : calderaCenterElev); // Downward slope to flat bottom of caldera

        final OpenSimplex2D warp = new OpenSimplex2D(seed + 43L).octaves(4).spread(0.03f).scaled(-100f, 100f);
        final Noise2D surface = new OpenSimplex2D(seed + 44L)
                .octaves(4)
                .spread(0.06f)
                .warped(warp)
                .map(x -> x > 0.4 ? x - 0.8f : -x)
                .scaled(-0.4f, 0.8f, -12, 12);

        return volcano.add(surface);
    }

    /**
     * Shield volcanoes with increased erosion, no recent lava flows, large calderas with open sides
     */
    public static Noise2D extinctShieldVolcano(long seed, Noise2D hotspot) {
        final double seaElev = SEA_LEVEL_Y + 6;
        final double mtnBaseElev = SEA_LEVEL_Y + 25;
        final double calderaEdgeElev = SEA_LEVEL_Y + 55;
        final double cliffEdgeElev = SEA_LEVEL_Y + 25;
        final double calderaCenterElev = SEA_LEVEL_Y - 10;

        final Noise2D volcano = hotspot.map(y -> y < 0.4 ? Mth.map(y, 0, 0.4, seaElev, mtnBaseElev) // Coastal slopes
                : y < 0.6 ? Mth.map(y, 0.4, 0.6, mtnBaseElev, calderaEdgeElev) // Mountain side/slope up to caldera
                        : y < 0.62 ? Mth.map(y, 0.6, 0.62, calderaEdgeElev, cliffEdgeElev) // Caldera cliff
                                : y < 0.75 ? Mth.map(y, 0.62, 0.75, cliffEdgeElev, calderaCenterElev) : calderaCenterElev); // Downward slope to flat bottom of caldera

        final OpenSimplex2D warp = new OpenSimplex2D(seed + 43L).octaves(4).spread(0.03f).scaled(-100f, 100f);
        final Noise2D surface = new OpenSimplex2D(seed + 44L)
                .octaves(4)
                .spread(0.06f)
                .warped(warp)
                .map(x -> x > 0.4 ? x - 0.8f : -x)
                .scaled(-0.4f, 0.8f, -9, 9);

        return volcano.add(surface);
    }

    /**
     * Shield volcanoes with large calderas with open sides, and rough surfaces to fill with glaciers
     */
    public static Noise2D glaciatedShieldVolcano(long seed, Noise2D hotspot) {
        final double seaElev = SEA_LEVEL_Y + 15;
        final double mtnBaseElev = SEA_LEVEL_Y + 70;
        final double calderaEdgeElev = SEA_LEVEL_Y + 100;
        final double cliffEdgeElev = SEA_LEVEL_Y + 60;
        final double calderaCenterElev = SEA_LEVEL_Y + 50;

        final Noise2D volcano = hotspot.map(y -> y < 0.45 ? Mth.map(y, 0, 0.45, seaElev, mtnBaseElev) // Coastal slopes
                : y < 0.72 ? Mth.map(y, 0.45, 0.72, mtnBaseElev, calderaEdgeElev) // Mountain side/slope up to caldera
                        : y < 0.74 ? Mth.map(y, 0.72, 0.74, calderaEdgeElev, cliffEdgeElev) // Caldera cliff
                                : y < 0.85 ? Mth.map(y, 0.74, 0.85, cliffEdgeElev, calderaCenterElev) : calderaCenterElev); // Downward slope to flat bottom of caldera

        final OpenSimplex2D warp = new OpenSimplex2D(seed + 43L).octaves(4).spread(0.03f).scaled(-100f, 100f);
        final Noise2D surface = new OpenSimplex2D(seed + 44L)
                .octaves(4)
                .spread(0.02f)
                .warped(warp)
                .map(x -> x > 0.4 ? x - 0.8f : -x)
                .scaled(-0.4f, 0.8f, -48, 32);

        return volcano.add(surface);
    }

    /**
     * Ice surface for Ice Sheet Shield Volcanoes
     * Heights added to iceSheetBaseLevel for smooth transition to ice sheet biomes
     */
    public static Noise2D shieldVolcanoIceSheetSurface(long seed, Noise2D hotspot) {
        final double edgeElev = 0;
        final double calderaCenterElev = 51;

        return hotspot.map(y -> y < 0.9 ? Mth.map(y, 0.0, 0.9, edgeElev, calderaCenterElev) : calderaCenterElev) // Interior of crater
                .add(iceSheetSurfaceHeight(seed));
    }

    /**
     * Ice surface for Glaciated Shield Volcanoes
     */
    public static Noise2D shieldVolcanoGlacierSurface(long seed, Noise2D hotspot) {
        final Noise2D base = new OpenSimplex2D(seed).octaves(3).spread(0.05f).scaled(-5, 5);
        final double lowElev = SEA_LEVEL_Y - 60;
        final double edgeElev = SEA_LEVEL_Y + 75;
        final double calderaRimElev = SEA_LEVEL_Y + 92;
        final double calderaCenterElev = SEA_LEVEL_Y + 98;

        return hotspot.map(y -> y < 0.40 ? lowElev
                : y < 0.58 ? Mth.map(y, 0.40, 0.58, lowElev, edgeElev) // Crater edge to base
                        : y < 0.72 ? Mth.map(y, 0.58, 0.72, edgeElev, calderaRimElev) // Crater edge to base
                                : y < 0.9 ? Mth.map(y, 0.72, 0.9, calderaRimElev, calderaCenterElev) : calderaCenterElev) // Interior of crater
                .add(base);
    }

    /**
     * Shield volcanoes with large calderas flooded by the ocean
     */
    public static Noise2D sunkenShieldVolcano(long seed, Noise2D hotspot) {
        final Noise2D volcano = hotspot.map(y -> y < 0.25 ? 50
                : y < 0.45 ? Mth.map(y, 0.25, 0.45, 50, SEA_LEVEL_Y)
                        : y < 0.6 ? Mth.map(y, 0.45, 0.6, SEA_LEVEL_Y, 95)
                                : y < 0.62 ? Mth.map(y, 0.6, 0.62, 94, 80)
                                        : y < 0.75 ? Mth.map(y, 0.62, 0.75, 80, 52) : 52);

        final OpenSimplex2D warp = new OpenSimplex2D(seed + 43L).octaves(4).spread(0.03f).scaled(-100f, 100f);
        final Noise2D surface = new OpenSimplex2D(seed + 44L)
                .octaves(4)
                .spread(0.06f)
                .warped(warp)
                .map(x -> x > 0.4 ? x - 0.8f : -x)
                .scaled(-0.4f, 0.8f, -6, 6);

        final Noise2D scale = new OpenSimplex2D(seed + 789913L).octaves(2).spread(0.008f).scaled(0.45, 1);

        return volcano.lazyProduct(scale).add(surface);
    }

    /**
     * Shield volcanoes with heavily eroded calderas
     */
    public static Noise2D ancientShieldVolcano(long seed, double minElev, double maxElev, Noise2D hotspot) {
        final Noise2D volcano = hotspot.map(y -> y < 0.15 ? 90
                : y < 0.6 ? Mth.map(y, 0.15, 0.6, 90, 130)
                        : y < 0.63 ? Mth.map(y, 0.6, 0.63, 129, 108)
                                : y < 0.7 ? Mth.map(y, 0.63, 0.7, 108, 90) : 90);

        final OpenSimplex2D warp = new OpenSimplex2D(seed + 43L).octaves(4).spread(0.03f).scaled(-100f, 100f);
        final Noise2D surface = new OpenSimplex2D(seed + 44L)
                .octaves(4)
                .spread(0.06f)
                .warped(warp)
                .map(x -> x > 0.4 ? x - 0.8f : -x)
                .scaled(-0.4f, 0.8f, -20, 0);

        final Noise2D valleys = new OpenSimplex2D(seed + 90183L).spread(0.01).ridged().octaves(3).scaled(maxElev * 2.2, minElev);

        final Noise2D scale = new OpenSimplex2D(seed + 789913L).octaves(2).spread(0.008f).scaled(0.6, 1);

        return TFGNoiseHelpers.min(volcano.lazyProduct(scale), valleys).add(surface);
    }

    /**
     * Used for various shores
     */
    public static Noise3D cliffNoise(Seed seed) {
        return new OpenSimplex3D(seed.seed()).octaves(2).spread(0.1f);
    }

    public static Noise2D lowerTerraceNoise(Seed seed) {
        return BiomeNoise.hills(seed.seed(), 7, 15);
    }

    public static Noise2D upperTerraceNoise(Seed seed) {
        return BiomeNoise.hills(seed.seed(), 18, 30);
    }

    /**
     * As temporal tides are infeasible, vary the heights of beaches relative to sea level such that
     * some beaches represent high tide conditions, and others low-tide conditions
     * Highest tide is at zero, lowest tide is at 4
     */
    public static Noise2D shoreTideLevelNoise(Seed seed) {
        return new OpenSimplex2D(seed.seed()).octaves(3).spread(0.005f).scaled(SEA_LEVEL_Y - 6, SEA_LEVEL_Y + 6).clamped(SEA_LEVEL_Y, SEA_LEVEL_Y + 4).add(new OpenSimplex2D(seed.seed()).spread(0.03));
    }

    /**
     * Simple f2 - f1 cellular noise, with slightly fuzzy edges
     */
    public static Noise2D seaIceNoise(long seed) {
        final TFGCellular2D cells = new TFGCellular2D(seed, 0.21f, 1).spread(0.03);
        final Noise2D wiggle = new OpenSimplex2D(seed).scaled(-0.04, 0.04).spread(0.12);
        return (x, z) -> {
            TFGCellular2D.TFGCell cell = cells.cell(x, z);

            return cell.f2() - cell.f1() + wiggle.noise(x, z);
        };
    }

    /**
     * Ridged noise used to place simulated lava flows on surfaces
     */
    public static Noise2D lavaFlow(long seed) {
        return new OpenSimplex2D(seed + 23891L).ridged().spread(0.01);
    }

    /**
     * Small scale noise used to vary the material of lava flows
     */
    public static Noise2D lavaFlowMaterial(long seed) {
        return new OpenSimplex2D(seed).octaves(2).spread(0.25);
    }

    public static double sharpHillsMap(double in) {
        final double in0 = 1.0f, in1 = 0.67f, in2 = 0.15f, in3 = -0.15f, in4 = -0.67f, in5 = -1.0f;
        final double out0 = 1.0f, out1 = 0.7f, out2 = 0.5f, out3 = -0.5f, out4 = -0.7f, out5 = -1.0f;

        if (in > in1)
            return Mth.map(in, in1, in0, out1, out0);
        if (in > in2)
            return Mth.map(in, in2, in1, out2, out1);
        if (in > in3)
            return Mth.map(in, in3, in2, out3, out2);
        if (in > in4)
            return Mth.map(in, in4, in3, out4, out3);
        else
            return Mth.map(in, in5, in4, out5, out4);
    }

    /**
     * Effectively a {@code lerp(noiseA(), noiseB(), piecewise(noiseB()) + noiseC()} with the following additional techniques:
     * <ul>
     *     <li>{@code noiseA} is scaled to outside it's range, then biased towards 1.0, to expose more cliffs, as opposed to hills </li>
     *     <li>{@code piecewise()} is a piecewise linear function that creates cliff shapes from the standard noise distribution.</li>
     *     <li>{@code noiseC} is added on top to provide additional variance (in places where the piecewise function would otherwise flatten areas.</li>
     * </ul>
     */
    public static Noise2D sharpHills(long seed, float minHeight, float maxHeight) {
        final Noise2D base = new OpenSimplex2D(seed)
                .octaves(4)
                .spread(0.08f);

        final Noise2D lerp = new OpenSimplex2D(seed + 7198234123L)
                .spread(0.013f)
                .scaled(-0.3f, 1.6f)
                .clamped(0, 1);

        final Noise2D lerpMapped = (x, z) -> {
            double in = base.noise(x, z);
            return Mth.lerp(lerp.noise(x, z), in, sharpHillsMap(in));
        };

        final OpenSimplex2D variance = new OpenSimplex2D(seed + 67981832123L)
                .octaves(3)
                .spread(0.06f)
                .scaled(-0.2f, 0.2f);

        return lerpMapped
                .add(variance)
                .scaled(-0.75f, 0.7f, SEA_LEVEL_Y - minHeight, SEA_LEVEL_Y + maxHeight);
    }

    /**
     * Generates a flat base with twisting carved canyons using many smaller terraces.
     * Inspired by imagery of Drumheller, Alberta
     */
    public static Noise2D badlands(long seed, int height, float depth) {
        return new OpenSimplex2D(seed)
                .octaves(4)
                .spread(0.025f)
                .scaled(SEA_LEVEL_Y + height, SEA_LEVEL_Y + height + 10)
                .add(new OpenSimplex2D(seed + 1)
                        .octaves(4)
                        .spread(0.04f)
                        .ridged()
                        .map(x -> 1.3f * -(x > 0 ? x * x * x : 0.5f * x))
                        .scaled(-1f, 0.3f, -1f, 1f)
                        .terraces(15)
                        .scaled(-depth, 0))
                .map(x -> x < SEA_LEVEL_Y ? SEA_LEVEL_Y - 0.3f * (SEA_LEVEL_Y - x) : x);
    }

    /**
     * Similar to mountains, but cliffs closer to sea level
     */
    public static Noise2D rockyIslands(long seed) {
        final Noise2D baseNoise = new OpenSimplex2D(seed) // A simplex noise forms the majority of the base
                .octaves(4)
                .spread(0.14f)
                .map(x -> {
                    final double x0 = 0.125f * (x + 1) * (x + 1) * (x + 1); // Power scaled, flattens most areas but maximizes peaks
                    return SEA_LEVEL_Y - 15 + 50 * x0; // Scale the entire thing
                });

        // Cliff noise consists of noise that's been artificially clamped over half the domain, which is then selectively added above a base height level
        final Noise2D cliffNoise = new OpenSimplex2D(seed + 2).octaves(2).spread(0.01f).scaled(-10, 18).map(x -> x > 0 ? x : 0);
        final Noise2D cliffHeightNoise = new OpenSimplex2D(seed + 3).octaves(2).spread(0.01f).scaled(SEA_LEVEL_Y - 5, SEA_LEVEL_Y + 5);

        return (x, z) -> {
            double height = baseNoise.noise(x, z);
            if (height > SEA_LEVEL_Y - 10) // Only sample each cliff noise layer if the base noise could be influenced by it
            {
                final double cliffHeight = cliffHeightNoise.noise(x, z) - height;
                if (cliffHeight < 0) {
                    final double mappedCliffHeight = Mth.clampedMap(cliffHeight, 0, -1, 0, 1);
                    height += mappedCliffHeight * cliffNoise.noise(x, z);
                }
            }
            return height;
        };
    }

    /**
     * Currently erupting location in a hotspot chain, used for biome noise and regional hotspot placement
     */
    public static Noise2D activeHotSpots(long seed) {
        final double horizontalScale = 0.003;
        final double cutoff = 0.75;
        final double rescale = 7.2;

        return new OpenSimplex2D(seed).map(y -> {
            y = y > cutoff ? y - cutoff : 0;
            y = (y * rescale);
            return y;
        }).octaves(3).spread(horizontalScale);
    }

    /**
     * Second location in a hotspot chain, used for biome noise and regional hotspot placement
     */
    public static Noise2D dormantHotSpots(long seed) {
        return hotSpotWarp(activeHotSpots(seed), plateRegions(seed), 1024, 0).map(y -> Math.max(y - 0.1, 0) * 1.111);
    }

    /**
     * Third location in a hotspot chain, used for biome noise and regional hotspot placement
     */
    public static Noise2D extinctHotSpots(long seed) {
        return hotSpotWarp(activeHotSpots(seed), plateRegions(seed), 2048, 0.25).map(y -> Math.max(y - 0.2, 0) * 1.25);
    }

    /**
     * Fourth location in a hotspot chain, used for biome noise and regional hotspot placement
     */
    public static Noise2D ancientHotSpots(long seed) {
        return hotSpotWarp(activeHotSpots(seed), plateRegions(seed), 3072, 0.5).map(y -> Math.max(y - 0.3, 0) * 1.4286);
    }

    /**
     * All hotspot locations combined into one map
     */
    public static Noise2D hotSpotIntensity(long seed) {
        return TFGNoiseHelpers.max(TFGNoiseHelpers.max(TFGNoiseHelpers.max(
                activeHotSpots(seed), dormantHotSpots(seed)), extinctHotSpots(seed)), ancientHotSpots(seed));
    }

    /**
     * This takes noise maps of each of the age categories of hotspots, and maps which one is dominant at every location in the world
     */
    public static Noise2D hotSpotAge(long seed) {
        Noise2D active = activeHotSpots(seed);
        Noise2D dormant = dormantHotSpots(seed);
        Noise2D extinct = extinctHotSpots(seed);
        Noise2D ancient = ancientHotSpots(seed);

        return mapAges(active, dormant, extinct, ancient);
    }

    /**
     * A domain-warp designed to warp the location of noise peaks without distorting their shapes
     * From an input value, procedurally determines a displacement vector
     *
     * @param warp          noise map to generate offsets from, designed to be used with a cellular hash map
     * @param velocityScale first-order distance scaling
     * @param accelScale    second-order distance scaling
     * @return this noise function, with a cellular domain warp effect
     */
    public static Noise2D hotSpotWarp(Noise2D noiseToWarp, Noise2D warp, int velocityScale, double accelScale) {
        return (x, z) -> {
            // Random vector
            final double ux = warp.noise(x, z);
            // Random magnitude from pev vector by multiplying and taking modulo, random direction based on magnitude
            final double uz = (Math.abs(ux * 16) % 1 > 0.5 ? 1 : -1) * (ux * 256) % 1;

            // Increase magnitude of vector to ensure islands in the same chain don't generate on top of each other
            final int sx = ux > 0 ? 1 : -1;
            final int sz = uz > 0 ? 1 : -1;
            final double vx = (ux + sx) * velocityScale;
            final double vz = (uz + sz) * velocityScale;

            // Perpendicular acceleration vector to create curved chains
            final double ax = -(vz) * accelScale;
            final double az = vx * accelScale;

            return noiseToWarp.noise(x + vx + ax, z + vz + az);
        };
    }

    public static Noise2D mapAges(Noise2D activeNoise, Noise2D youngNoise, Noise2D oldNoise, Noise2D oldestNoise) {
        return (x, z) -> {
            final double active = activeNoise.noise(x, z);
            final double young = youngNoise.noise(x, z);
            final double old = oldNoise.noise(x, z);
            final double oldest = oldestNoise.noise(x, z);

            if (Math.max(Math.max(active, young), Math.max(old, oldest)) <= -0.8) {
                return 0;
            } else if (active > young && active > old && active > oldest) {
                return 1;
            } else if (young > active && young > old && young > oldest) {
                return 2;
            } else if (old > young && old > oldest && old > active) {
                return 3;
            } else if (oldest > young && oldest > old && oldest > active) {
                return 4;
            } else
                return 0;
        };
    }

    /**
     * Not related to the region generator cells, this is used to randomize hotspot track directions within large scale regions
     */
    public static TFGCellular2D plateRegions(long seed) {
        return new TFGCellular2D(seed).spread(0.00590625f / Units.CELL_WIDTH_IN_GRID);
    }

    /**
     * Return a constant. Used for technical biomes such as shore where the height seen in game is not based on the biome noise
     * but the biome noise is still needed for blending
     */
    public static Noise2D constant(int height) {
        return (x, z) -> (SEA_LEVEL_Y + height);
    }

    /**
     * Noise for stair-step canyons
     */
    public static Noise2D stairCanyons(long seed) {
        final int minHeight = SEA_LEVEL_Y + 4;
        final int plateauHeight = SEA_LEVEL_Y + 22;

        return stairStepCliffs(seed, canyonBaseNoise(seed, minHeight, plateauHeight, 0.05)).add(new OpenSimplex2D(seed).octaves(3).spread(0.08).scaled(-5, 5));
    }

    public static Noise2D mesas(long seed) {
        final int minHeight = SEA_LEVEL_Y + 4;
        final int plateauHeight = SEA_LEVEL_Y + 22;

        return stairStepCliffs(seed, canyonBaseNoise(seed, minHeight, plateauHeight, 0.12)).add(new OpenSimplex2D(seed).octaves(3).spread(0.08).scaled(-5, 5));
    }

    public static Noise2D buttes(long seed) {
        final int minHeight = SEA_LEVEL_Y + 4;
        final int plateauHeight = SEA_LEVEL_Y + 22;

        return stairStepCliffs(seed, canyonBaseNoise(seed, minHeight, plateauHeight, 0.18)).add(new OpenSimplex2D(seed).octaves(3).spread(0.08).scaled(-5, 5));
    }

    public static Noise2D hoodoos(long seed) {
        final int minHeight = SEA_LEVEL_Y + 4;
        final int maxHeight = SEA_LEVEL_Y + 22;
        final Noise2D maxBaseNoise = canyonBaseNoise(seed, minHeight, maxHeight, 0.03);
        final Noise2D minBaseNoise = canyonBaseNoise(seed, minHeight, maxHeight, 0.20);
        final Noise2D hoodooNoise = TFGNoiseHelpers.clampedScaled(new OpenSimplex2D(seed + 1)
                .octaves(3)
                .spread(0.12f)
                .abs(),
                0.20, 0.60, minHeight, maxHeight);
        return stairStepCliffs(seed, TFGNoiseHelpers.max(TFGNoiseHelpers.min(maxBaseNoise, hoodooNoise), minBaseNoise), 5, 8, 7).add(new OpenSimplex2D(seed).octaves(3).spread(0.08).scaled(-3, 3));
    }

    /**
     * Base noise used by steep-canyon biomes (buttes, mesas, etc) to align valleys
     *
     * @param minHeight   Minimum output height
     * @param maxHeight   Maximum output height
     * @param valleyWidth Used to vary the widths of canyons without changing the frequency
     */

    public static Noise2D canyonBaseNoise(long seed, int minHeight, int maxHeight, double valleyWidth) {
        final double valleyEdge = valleyWidth + 0.28;
        return TFGNoiseHelpers.clampedScaled(new OpenSimplex2D(seed + 1)
                .octaves(4)
                .spread(0.03f)
                .abs(),
                valleyWidth, valleyEdge, minHeight, maxHeight);
    }

    public static Noise2D stairStepCliffs(long seed, Noise2D input) {
        return stairStepCliffs(seed, input, 5, 12, 7);
    }

    /**
     * Unified cliff bands
     *
     * @param input         The base noise to add cliffs to. Should be scaled and adjusted to sea level
     * @param minCliffStart The lowest height above sea level that the base of a cliff should appear
     * @param maxCliffStart The highest height above sea level that the base of a cliff should appear
     * @param cliffHeight   Height of each tier of cliffs
     */
    public static Noise2D stairStepCliffs(long seed, Noise2D input, int minCliffStart, int maxCliffStart, int cliffHeight) {
        final Noise2D cliffStartHeightNoise = new OpenSimplex2D(seed + 3).octaves(2).spread(0.008f).scaled(SEA_LEVEL_Y + minCliffStart, SEA_LEVEL_Y + maxCliffStart);
        final Noise2D cliffNoise = new OpenSimplex2D(seed + 7).spread(0.003f).scaled(-cliffHeight, 2 * cliffHeight).clamped(0, cliffHeight);
        final Noise2D doubleCliffNoise = cliffNoise.add(cliffNoise);

        final Noise2D secondCliffStartHeightNoise = cliffStartHeightNoise.add(doubleCliffNoise);
        final Noise2D secondCliffNoise = new OpenSimplex2D(seed + 19).spread(0.003f).scaled(-cliffHeight, 2 * cliffHeight).clamped(0, cliffHeight);

        final Noise2D thirdCliffStartHeightNoise = secondCliffStartHeightNoise.add(doubleCliffNoise);
        final Noise2D thirdCliffNoise = new OpenSimplex2D(seed + 25).spread(0.003f).scaled(-cliffHeight, 2 * cliffHeight).clamped(0, cliffHeight);

        final Noise2D slopeNoise = new OpenSimplex2D(seed + 33).spread(0.008).scaled(-2, 2);

        return TFGNoiseHelpers
                .slopedCliffMap(
                        TFGNoiseHelpers.slopedCliffMap(TFGNoiseHelpers.slopedCliffMap(input, cliffStartHeightNoise, cliffNoise, cliffNoise.scaled(0, 7, 3, 6).add(slopeNoise)),
                                secondCliffStartHeightNoise, secondCliffNoise, cliffNoise.scaled(0, 7, 3, 6).add(slopeNoise)),
                        thirdCliffStartHeightNoise, thirdCliffNoise, cliffNoise.scaled(0, 7, 3, 6).add(slopeNoise));
    }

    /**
     * Bowl dolines/shallow sinkholes, similar noise to cone karsts
     * Can be applied over any base terrain noise map, subtracts from the base terrain
     */
    public static Noise2D bowlDolines(long seed, Noise2D baseTerrainNoise, double scale) {
        final Noise2D bowls = new OpenSimplex2D(seed)
                .octaves(3)
                .spread(0.72 / scale)
                .map(x -> {
                    x = -0.5 * Math.cos(Math.PI * x) + 0.5;
                    x = (Math.max(x, 0.1) - 0.1);
                    x = -scale * x;
                    return x;
                });

        return baseTerrainNoise.add(bowls);
    }

    /**
     * Fengcong, aka "Cone Karsts"
     * Can be applied over any base terrain noise map, adds to the base terrain
     */
    public static Noise2D fengcong(long seed, Noise2D baseTerrainNoise) {
        final double scale = 37;

        final Noise2D cones = new OpenSimplex2D(seed)
                .octaves(3)
                .spread(0.06)
                .map(y -> {
                    y = -0.5 * Math.cos(Math.PI * (Math.abs(y))) + 0.5;
                    y = (Math.max(y, 0.25) - 0.25) / 0.75;
                    y = scale * y;
                    return y;
                });

        return baseTerrainNoise.add(cones);
    }

    /**
     * Fenglin, aka "Tower Karsts"
     * Can be applied over any base terrain noise map, adds to the base terrain
     */
    public static Noise2D fenglin(long seed, Noise2D baseTerrainNoise, double scale) {
        final Noise2D cliffScale = new OpenSimplex2D(seed + 78535267L)
                .spread(0.06)
                .scaled(0, 0.25);

        final Noise2D cliffStartHeight = new OpenSimplex2D(seed + 390798L)
                .spread(0.06)
                .scaled(0, 0.7);

        final Noise2D cliffBase = new OpenSimplex2D(seed)
                .octaves(2)
                .spread(0.05)
                .map(y -> {
                    y = Math.abs(y) - 0.45;
                    y = y > 0 ? Math.sqrt(y / 0.55) : 0;
                    return y;
                });
        final Noise2D towers = fenglinCliffMap(cliffBase, cliffStartHeight, cliffScale)
                .map(y -> scale * y);

        return baseTerrainNoise.add(towers);
    }

    /**
     * If {@code base} is higher than {@code compare}, this will return the sum {@code base * (1 - addend) + addend}. Otherwise, this
     * will return the value of {@code base}.
     *
     * @return A new noise function
     */
    public static Noise2D fenglinCliffMap(Noise2D baseNoise, Noise2D compareNoise, Noise2D addendNoise) {
        return (x, z) -> {
            final double base = baseNoise.noise(x, z);
            if (base > compareNoise.noise(x, z)) {
                final double addend = addendNoise.noise(x, z);
                return base * (1 - addend) + addend;
            } else {
                return base;
            }
        };
    }

    /**
     * Inspired by the bare Karst at Burren, Ireland
     * Can be applied over any base terrain noise map, adds to the base terrain
     */
    public static Noise2D burren(long seed, Noise2D baseTerrainNoise, double scale) {
        final int minHeight = SEA_LEVEL_Y + 2;

        final Noise2D crevices = burrenCrevices(seed).map(y -> y < 0.15 ? -scale : y < 0.4 ? (y - 0.4) * scale : 0);

        return crevices.add(baseTerrainNoise).map(y -> Math.max(y, minHeight));
    }

    /**
     * The seeded, absolute value noise used by the Burren Biome
     * Used by surface builder as well as Burren noise
     */
    public static Noise2D burrenCrevices(long seed) {
        return new OpenSimplex2D(seed + 398767567L)
                .octaves(2)
                .spread(0.08f)
                .abs();
    }

    /**
     * Medium hills stretched in the north-south axis
     */
    public static Noise2D drumlins(long seed) {
        return TFGNoiseHelpers.stretchZ(new OpenSimplex2D(seed).octaves(3).spread(0.04f).scaled(SEA_LEVEL_Y - 16, SEA_LEVEL_Y + 32), 2.5);
    }

    /**
     * Inspired by the "Stone Forests" of Shilin, China
     * Can be applied over any base terrain noise map, takes the max value of the two noises
     */
    public static Noise2D shilin(long seed, Noise2D baseTerrainNoise, double scale) {
        final int minHeight = SEA_LEVEL_Y + 2;

        final Noise2D ridges = shilinRidges(seed);

        final Noise2D bumps = new OpenSimplex2D(seed + 83436545633L).spread(0.16).scaled(0.6, 1.0);

        return TFGNoiseHelpers.max(ridges.lazyProduct(bumps).scaled(SEA_LEVEL_Y, SEA_LEVEL_Y + scale), baseTerrainNoise)
                .map(y -> Math.max(y, minHeight));
    }

    /**
     * Unscaled shilin noise, not applied to terrain
     * Used by surface builder as well as Shilin noise
     */
    public static Noise2D shilinRidges(long seed) {
        final double widthTop = 0.1;
        final double widthBot = 0.2;

        // Basic ridge shapes following zeroes in the noise
        final Noise2D ridges = new OpenSimplex2D(seed + 398767567L)
                .octaves(2)
                .spread(0.06f)
                .map(
                        y -> {
                            y = Math.abs(y);
                            return y < widthTop ? 1 : y < widthBot ? 1 + (0.67 * (y - widthTop) / (widthTop - widthBot)) : 0;
                        });

        // Cuts continuous paths through ridges to make them more passable
        final Noise2D cuts = new OpenSimplex2D(seed + 45764379L)
                .octaves(2)
                .spread(0.03f)
                .map(
                        y -> {
                            y = Math.abs(y);
                            y = y < widthTop * 0.65 ? 1 : y < widthBot * 1.2 ? 1 + ((y - widthTop * 0.65) / (widthTop * 0.65 - widthBot * 1.2)) : 0;

                            return 1 - y;
                        });

        return ridges.lazyProduct(cuts);
    }

    /**
     * Cenotes/deep sinkholes, similar noise to tower karsts
     * Can be applied over any base terrain noise map, subtracts from the base terrain
     */
    public static Noise2D cenotes(long seed, Noise2D baseTerrainNoise, double vertScale, double horizScale) {
        final Noise2D cliffScale = new OpenSimplex2D(seed + 78535267L)
                .spread(0.72 / horizScale)
                .scaled(0, 0.4);

        final Noise2D cliffStartHeight = new OpenSimplex2D(seed + 390798L)
                .spread(0.72 / horizScale)
                .scaled(0, 0.7);

        final Noise2D cliffBase = new OpenSimplex2D(seed)
                .octaves(2)
                .spread(0.6 / horizScale)
                .map(y -> {
                    y = Math.abs(y) - 0.45;
                    y = y > 0 ? Math.sqrt(y / 0.55) : 0;
                    return y;
                });
        final Noise2D cenotes = fenglinCliffMap(cliffBase, cliffStartHeight, cliffScale)
                .map(y -> -vertScale * y);

        return baseTerrainNoise.add(cenotes);
    }

    /**
     * Multi-tiered sinkholes inspired by the Xiaozhai Tiankeng
     * Essentially applies two "cenotes" of different sizes on top of each other
     */
    public static Noise2D tiankeng(long seed, Noise2D baseTerrainNoise) {
        final Noise2D cliffScale = new OpenSimplex2D(seed + 78535267L)
                .spread(0.04)
                .scaled(0, 0.04);
        final Noise2D cliffStartHeight = new OpenSimplex2D(seed + 390798L)
                .spread(0.04)
                .scaled(0, 0.7);

        final Noise2D wideCliffBase = new OpenSimplex2D(seed)
                .octaves(2)
                .spread(0.02)
                .map(y -> {
                    y = Math.abs(y) - 0.3;
                    y = y > 0 ? Math.sqrt(y / 0.7) : 0;
                    return y;
                });

        final Noise2D deepCliffBase = new OpenSimplex2D(seed)
                .octaves(2)
                .spread(0.02)
                .map(y -> {
                    y = Math.abs(y) - 0.65;
                    y = y > 0 ? Math.sqrt(y / 0.35) : 0;
                    return y;
                });

        return (x, z) -> {
            // Multiple copies of `fenglinCliffMap()` but avoiding repeated evaluation
            final double compare = cliffStartHeight.noise(x, z);
            final double addend = cliffScale.noise(x, z);
            final double wideBase = wideCliffBase.noise(x, z);
            final double deepBase = deepCliffBase.noise(x, z);

            return baseTerrainNoise.noise(x, z)
                    + -22 * (wideBase > compare ? wideBase * (1 - addend) + addend : wideBase)
                    + -24 * (deepBase > compare ? deepBase * (1 - addend) + addend : deepBase);
        };
    }

    /**
     * Jointed surface of ice sheets/glaciers, should be added to biome noise, should not be included in surface builders
     */
    public static Noise2D glacialSurfaceTexture(long seed) {
        final Noise2D warp = new OpenSimplex2D(seed + 413L).spread(0.02).scaled(-12, 12);
        return (x, z) -> {
            final double yOfX = Math.min(TFGNoiseHelpers.triangle(25, 18, 0.035, x + warp.noise(x, z)), 0.0);
            // Reversed order of x, and z in the .noise() call is intentional here
            final double yOfZ = Math.min(TFGNoiseHelpers.triangle(40, 30, 0.025, z + warp.noise(z, x)), 0.0);
            return Math.min(yOfX, yOfZ);
        };
    }

    /**
     * The standard height for the base of an ice sheet
     */
    public static Noise2D glacialBase(long seed) {
        return TFGNoiseHelpers.addConstant(knobAndKettle(seed), 1.5);
    }

    /**
     * The standard height for the base of an ice sheet in biomes near oceans
     */
    public static Noise2D glacialOceanicBase(long seed) {
        return (x, z) -> SEA_LEVEL_Y - 4;
    }

    /**
     * The standard height for the top of an ice sheet, without texturing
     */
    public static Noise2D iceSheetSurfaceHeight(long seed) {
        return BiomeNoise.hills(seed, 23, 38);
    }

    /**
     * The standard height for the top of an ice sheet in mountainous biomes, without texturing
     */
    public static Noise2D montaneIceSheetSurfaceHeight(long seed) {
        return BiomeNoise.hills(seed, 40, 48);
    }

    /**
     * The standard height for the top of an ice sheet in oceanic biomes, without texturing
     */
    public static Noise2D oceanicIceSheetSurfaceHeight(long seed) {
        return BiomeNoise.hills(seed, 18, 26);
    }

    /**
     * The standard height for the top of glaciers in glacial cirque biomes
     * This function is at the correct height for oceanic glacial mountains. It is shifted up for standard glacial mountains
     */
    public static Noise2D glacialCirquesIceSurfaceHeight(long seed) {
        return connectedValleyNoise(seed)
                .map(y -> y < 0.38 ? -100 : y < 0.43 ? Mth.map(y, 0.38, 0.43, -50, 0) : Mth.map(y, 0.43, 1, 0, 32))
                .add(BiomeNoise.hills(seed, 15, 23)
                        .add(TFGBiomeNoise.glacialCirquesCliffsScale(seed)));
    }

    /**
     * Should mirror the upper surface of {@link TFGBiomeNoise#glacialCirquesIceSurfaceHeight(long)}, but 3 blocks highers
     * Reverses slope instead of plunging vertically at edges of glaciers to avoid creating cliffs that block the mouths of cirques
     */
    public static Noise2D glacialCirquesCliffsStartHeight(long seed) {
        return connectedValleyNoise(seed)
                .map(y -> y < 0.43
                        ? Mth.map(y, 0, 0.43, 32, 0)
                        : Mth.map(y, 0.43, 1, 0, 32))
                .add(BiomeNoise.hills(seed, 18, 26));
    }

    public static Noise2D glacialCirquesCliffsScale(long seed) {
        return new OpenSimplex2D(seed + 78267L).spread(0.015).add(glacialValleyShapeNoise(seed)).scaled(-10, 8).clamped(0, 7);

    }

    /**
     * U-shaped valleys for glacial mountains
     */
    public static Noise2D glacialValleyShapeNoise(long seed) {
        return connectedValleyBaseNoise(seed).map(y -> Math.min(6 * y * y, 0.75 + 0.25 * y)).add(new OpenSimplex2D(seed + 5287L).octaves(4).spread(0.06).scaled(-0.2, 0.2));
    }

    /**
     * This function is at the correct height for oceanic glacial mountains. It is shifted up for standard glacial mountains
     */
    public static Noise2D glacialCirques(long seed) {

        // Noise for the large, continuous valleys
        final Noise2D shape = glacialValleyShapeNoise(seed);
        final Noise2D shapeMap = connectedValleyNoise(seed);

        // Glacial mountain noise is based on cellular noise. Cells are either bowl-shaped cirques, or cone-shaped horns
        final double cellScale = 0.010;
        final TFGCellular2D cells = new TFGCellular2D(seed, 2).spread(cellScale);
        final Noise2D warp = new OpenSimplex2D(seed).spread(0.02).add(shapeMap).scaled(-1, 2, -0.25, 0.2);
        final Noise2D roughPeaks = new OpenSimplex2D(seed).octaves(3).spread(0.08).scaled(0.6, 1.6);

        // Cliffs in valleys
        final Noise2D cliffScale = new OpenSimplex2D(seed + 785267L).spread(0.01).scaled(-12, 15).clamped(0, 10);
        final Noise2D cliffStartHeight = TFGNoiseHelpers.addConstant(oceanicIceSheetSurfaceHeight(seed), -8);

        final Noise2D cirques = (x, z) -> {
            TFGCellular2D.TFGCell cell = cells.cell(x, z);

            final double f1 = cell.f1();
            final double f2 = cell.f2();
            final double f2f1 = (f1 > 0 ? (f2 - f1) : 1);

            final double shapeAtCenter = shapeMap.noise(cell.cx() / cellScale, cell.cy() / cellScale);

            // Whether a cell is a cirque or a horn is based on the shape noise, a way of approximating the distance to the nearest valley
            if (shapeAtCenter > 0.60) {
                // Horn height function
                double y = (f2f1 + warp.noise(x, z));
                final double rough = roughPeaks.noise(x, z);
                final double scale = Math.min(Helpers.lerp(2 * y, 1.0, (rough)), rough);
                y = 1 + scale * y;
                return y;
            } else {
                // Cirque height function
                double y = 1 - (f2f1 - warp.noise(x, z));
                y = 0.5 * (1 + y * y);

                final double shapeAtPoint = shapeMap.noise(x, z);
                final double valleyCloseness = Math.min(shapeAtPoint - shapeAtCenter, 0);

                return y + Mth.clampedMap(f2f1, 0, 0.1, 0, valleyCloseness);
            }
        };
        return TFGNoiseHelpers.cliffMap(TFGNoiseHelpers.cliffMap(
                TFGNoiseHelpers.addConstant(cirques.scaled(0, 1, 12, 64).lazyProduct(shape), SEA_LEVEL_Y - 15), cliffStartHeight, cliffScale), glacialCirquesCliffsStartHeight(seed),
                glacialCirquesCliffsScale(seed));
    }

    /**
     * Polygonal incisions 1 block deep to superimpose on terrain
     * Inspired by polygonal ground due to frost action
     */
    public static Noise2D patternedGround(long seed) {
        TFGCellular2D cells = new TFGCellular2D(seed, 0.25f, 1).spread(0.05);
        return (x, z) -> {
            TFGCellular2D.TFGCell cell = cells.cell(x, z);

            return cell.f2() - cell.f1() < 0.12 ? -1 : 0;
        };
    }

    /**
     * Polygonal ridges 1 block high to superimpose on terrain
     * Inspired by polygonal ground due to frost action
     */
    public static Noise2D invertedPatternedGround(long seed) {
        final Noise2D base = BiomeNoise.hills(seed, -4, 3);
        TFGCellular2D cells = new TFGCellular2D(seed, 0.25f, 1).spread(0.05);

        return (x, z) -> {
            final double height = base.noise(x, z);
            TFGCellular2D.TFGCell cell = cells.cell(x, z);
            final double f2f1 = cell.f2() - cell.f1();
            if (height >= SEA_LEVEL_Y) {
                return height + (f2f1 < 0.12 ? 1 : 0);
            } else {
                return f2f1 < 0.12 ? SEA_LEVEL_Y - 1 : f2f1 < 0.22 ? SEA_LEVEL_Y - 2 : SEA_LEVEL_Y - 3;
            }
        };
    }

    /**
     * Lifted rings to superimpose on terrain
     * Based on sorted circles found in the Svalbard Archipelago and other polar climates
     */
    public static Noise2D stoneCircles(long seed) {
        TFGCellular2D cells = new TFGCellular2D(seed, 0.26f, 1).spread(0.09);
        return (x, z) -> {
            TFGCellular2D.TFGCell cell = cells.cell(x, z);

            final double f1 = cell.f1();

            return f1 > 0.06 && f1 < 0.13 ? 1 : 0;
        };
    }

    /**
     * Flat land with scattered ponds and small mounds
     */
    public static Noise2D knobAndKettle(long seed) {
        return new OpenSimplex2D(seed).octaves(2).spread(0.03f)
                .map(y -> y > 0.3 ? y - 0.3 : y < -0.3 ? y + 0.3 : 0)
                .scaled(-12, 10).add(BiomeNoise.hills(seed, -3, 3));
    }
}
