package su.terrafirmagreg.core.world.new_ow_wg.noise;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import su.terrafirmagreg.core.world.new_ow_wg.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;

public final class TuffRingNoise implements CenterOrDistanceNoise {

    private static float calculateEasing(float f1) {
        return Mth.map(f1, 0, 0.23f, 1, 0);
    }

    private static float calculateClampedEasing(float f1) {
        return Mth.clamp(calculateEasing(f1), 0, 1);
    }

    /**
     * @param t The unscaled square distance from the tuff ring center, roughly in [0, 1.2]
     * @return A noise function determining the tuff ring's height at any given position, in the range [0, 1]
     */
    private static float calculateShape(float t) {
        return t < 0.03f ? 0 // Flat
                : t < 0.10f ? t * 7f - 0.21f // Slope up to 0.5
                        : t < 0.15f ? t * 6.6667f // Cliff, 0.67 slope to 1.0
                                : t < 0.20f ? 1 - (t - 0.15f) * 6.6667f // Ridge, slope 1.0 down to 0.67
                                        : 1.5f - (5 * t); // Cliff base, slope 0.5 down
    }

    private final Cellular2D cellNoise;
    private final Noise2D jitterNoise;

    /**
     * @param seed The level seed - important, this is used from multiple different locations (base noise, surface builder, placement/decorator), and must have the same seed.
     */
    public TuffRingNoise(Seed seed) {
        cellNoise = new Cellular2D(seed.seed()).spread(0.003f);
        jitterNoise = new OpenSimplex2D(seed.seed() + 1234123L).octaves(2).scaled(-0.032f, 0.032f).spread(0.064f);
    }

    public double modifyHeight(double x, double z, Noise2D baseNoise, int rarity, int baseVolcanoHeight, int scaleHeight, long seed) {
        final Cellular2D.Cell cell = sampleCell(x, z, rarity);
        final double baseHeight = baseNoise.noise(x, z);
        if (cell != null) {
            final float f1 = (float) cell.f1();
            final float easing = Mth.clamp(TuffRingNoise.calculateEasing(f1) + (float) jitterNoise.noise(x, z), 0, 1);
            final float shape = TuffRingNoise.calculateShape(1 - easing);
            final float ringAdditionalHeight = shape * scaleHeight + (shape > 0.5 ? addNoise(seed, x, z) : 0f);
            final float ringHeight = TFCChunkGenerator.SEA_LEVEL_Y + baseVolcanoHeight + ringAdditionalHeight;
            //Linearly scales between baseHeight and the max of baseHeight and ringHeight near the edges of cells
            return Mth.lerp(50 * Mth.clamp(cell.f2() - f1, 0, 0.02), baseHeight, Math.max(ringHeight, baseHeight));
        }
        return baseHeight;
    }

    public float addNoise(long seed, double x, double z) {
        return (float) new OpenSimplex2D(seed).octaves(2).spread(0.1).scaled(-2, 8).noise(x, z);
    }

    /**
     * Calculate the closeness value to a tuff ring center, in the range [0, 1]. 1 = Center, 0 = Nowhere near.
     */
    public float calculateEasing(int x, int z, int rarity) {
        final Cellular2D.Cell cell = sampleCell(x, z, rarity);
        if (cell != null) {
            return calculateClampedEasing((float) cell.f1());
        }
        return 0;
    }

    /**
     * Calculate the center of the nearest tuff ring center, if one exists, to the given x, z, at the given y.
     */
    @Nullable
    public BlockPos calculateCenter(int x, int y, int z, int rarity) {
        final Cellular2D.Cell cell = sampleCell(x, z, rarity);
        if (cell != null) {
            return new BlockPos((int) cell.x(), y, (int) cell.y());
        }
        return null;
    }

    /**
     * Sample the nearest tuff ring cell to a given position.
     * Returns {@code null} if the cell was excluded due to a rarity condition, or if the cell was too close to adjacent cells (possibly causing overlapping tuff rings)
     */
    @Nullable
    private Cellular2D.Cell sampleCell(double x, double z, int rarity) {
        final Cellular2D.Cell cell = cellNoise.cell(x, z);
        if (Math.abs(cell.noise()) <= 1f / rarity) {
            return cell;
        }
        return null;
    }

    @Override
    public boolean isValidBiome(BiomeExtension biome) {
        return ((IBiomeExtension) biome).tfg$hasTuffRings();
    }

    @Override
    public int getRarity(BiomeExtension biome) {
        return ((IBiomeExtension) biome).tfg$getTuffRingRarity();
    }
}
