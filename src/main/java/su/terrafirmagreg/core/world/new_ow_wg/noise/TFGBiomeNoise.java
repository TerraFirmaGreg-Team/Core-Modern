package su.terrafirmagreg.core.world.new_ow_wg.noise;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.*;
import net.dries007.tfc.world.region.Units;
import net.minecraft.util.Mth;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;

public class TFGBiomeNoise {
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
     * Adds volcanoes to a base noise height map
     */
    public static Noise2D addVolcanoes(Seed seed, Noise2D baseNoise, int rarity, int baseVolcanoHeight, int scaleVolcanoHeight, boolean onShieldVolcano) {
        final TFGVolcanoNoise volcanoes = new TFGVolcanoNoise(seed);
        return (x, z) -> onShieldVolcano ? volcanoes.modifyShieldVolcanoHeight(x, z, baseNoise.noise(x, z), rarity, baseVolcanoHeight, scaleVolcanoHeight)
                : volcanoes.modifyHeight(x, z, baseNoise.noise(x, z), rarity, baseVolcanoHeight, scaleVolcanoHeight);
    }

    /**
     * Adds volcanoes to a base noise height map
     */
    public static Noise2D addTuffRings(Seed seed, Noise2D baseNoise, int rarity, int baseRingHeight, int scaleRingHeight) {
        final TuffRingNoise rings = new TuffRingNoise(seed);
        return (x, z) -> rings.modifyHeight(x, z, baseNoise, rarity, baseRingHeight, scaleRingHeight, seed.seed());
    }

    /**
     * Adds tuya volcanoes to a base noise height map
     */
    public static Noise2D addTuyas(Seed seed, Noise2D baseNoise, int rarity, int baseVolcanoHeight, int scaleVolcanoHeight, boolean icy) {
        final TuyaNoise tuyas = new TuyaNoise(seed);
        return (x, z) -> tuyas.modifyHeight(x, z, baseNoise.noise(x, z), rarity, baseVolcanoHeight, scaleVolcanoHeight, icy);
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
        final Cellular2D cells = new TFGCellular2D(seed, 0.21f, 1).spread(0.03);
        final Noise2D wiggle = new OpenSimplex2D(seed).scaled(-0.04, 0.04).spread(0.12);
        return (x, z) -> {
            Cellular2D.Cell cell = cells.cell(x, z);

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
    public static Noise2D sharpHills(long seed, float minMeight, float maxHeight) {
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
                .scaled(-0.75f, 0.7f, SEA_LEVEL_Y - minMeight, SEA_LEVEL_Y + maxHeight);
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
}
