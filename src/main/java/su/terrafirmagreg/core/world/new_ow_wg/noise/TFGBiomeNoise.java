package su.terrafirmagreg.core.world.new_ow_wg.noise;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.*;

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
}
