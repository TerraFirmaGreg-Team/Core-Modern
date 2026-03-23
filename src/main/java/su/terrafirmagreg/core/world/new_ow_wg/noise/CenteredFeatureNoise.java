/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.noise;

import static net.dries007.tfc.world.TFCChunkGenerator.SEA_LEVEL_Y;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.biome.IBiomeExtension;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;
import su.terrafirmagreg.core.world.new_ow_wg.chunk.TFGChunkHeightFiller;

public class CenteredFeatureNoise {
    public static CenteredFeatureNoiseSampler cinder(Seed seed) {
        return new CenteredFeatureNoiseSampler() {
            final TFGCellular2D cellNoise = new TFGCellular2D(seed.seed()).spread(0.009f);
            final Noise2D jitterNoise = new OpenSimplex2D(seed.seed() + 8179234123L).octaves(2).scaled(-0.0016f, 0.0016f).spread(0.128f);

            @Override
            public double setColumnAndSampleHeight(double heightIn, int x, int z, BiomeSourceExtension biomeSource) {
                TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                final BiomeExtension biome = biomeSource.getBiomeExtension(QuartPos.fromBlock((int) cell.x()), QuartPos.fromBlock((int) cell.y()));
                if (biome.isVolcanic()) {
                    final int rarity = biome.getVolcanoRarity();
                    if (checkCellRarity(cell, rarity)) {
                        if (biome == TFGBiomes.ACTIVE_SHIELD_VOLCANO)
                            return modifyHeightShieldVolcano(cell, x, z, biome, heightIn);
                        else
                            return modifyHeight(cell, x, z, biome, heightIn);
                    }
                }
                return TFGChunkHeightFiller.NOT_PRESENT_RETURN;
            }

            public BiomeExtension getCenterBiome(int x, int z, BiomeSourceExtension biomeSource) {
                TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                return biomeSource.getBiomeExtension(QuartPos.fromBlock((int) cell.x()), QuartPos.fromBlock((int) cell.y()));
            }

            /**
             * Calculate the closeness value to a volcano, in the range [0, 1]. 1 = Center of a volcano, 0 = Nowhere near.
             */
            public float calculateEasing(int x, int z, int rarity) {
                final TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                if (checkCellRarity(cell, rarity)) {
                    return calculateClampedEasing((float) cell.f1());
                }
                return 0;
            }

            private double modifyHeight(TFGCellular2D.TFGCell cell, int x, int z, BiomeExtension biome, double heightIn) {
                IBiomeExtension be = (IBiomeExtension) biome;
                final double f1 = cell.f1();
                final double easing = Mth.clamp(calculateEasing((float) f1) + jitterNoise.noise(x, z), 0, 1);
                final double shape = calculateShape(1 - easing);
                final double volcanoAdditionalHeight = shape * be.tfg$getCenteredFeatureScaleHeight();
                final double volcanoHeight = (SEA_LEVEL_Y + be.tfg$getCenteredFeatureBaseHeight() + volcanoAdditionalHeight);
                final double weight = 10f * Mth.clamp((float) cell.f2() - f1, 0f, 0.1f);
                return Mth.lerp(easing * weight, heightIn, (0.2 * volcanoHeight + 0.8 * Math.max(volcanoHeight, heightIn + 0.6f * volcanoAdditionalHeight)));

            }

            private double modifyHeightShieldVolcano(TFGCellular2D.TFGCell cell, int x, int z, BiomeExtension biome, double heightIn) {
                if (cell != null) {
                    IBiomeExtension be = (IBiomeExtension) biome;
                    final double f1 = cell.f1();
                    final double easing = Mth.clamp(calculateEasing((float) f1) + (jitterNoise.noise(x, z)), 0, 1);
                    final double shape = calculateShape(1 - easing);
                    final double volcanoAdditionalHeight = shape * be.tfg$getCenteredFeatureScaleHeight();
                    final double volcanoHeight = (SEA_LEVEL_Y + be.tfg$getCenteredFeatureBaseHeight() + volcanoAdditionalHeight);
                    final double weight = 10f * Mth.clamp(cell.f2() - f1, 0f, 0.1f);
                    return Mth.lerp(easing * weight, heightIn, (0.2 * volcanoHeight + 0.8 * Math.max(volcanoHeight, heightIn + 0.6f * volcanoAdditionalHeight)));
                }
                return heightIn;
            }

            private static float calculateEasing(float f1) {
                return Mth.map(f1, 0, 0.23f, 1, 0);
            }

            private static float calculateClampedEasing(float f1) {
                return Mth.clamp(calculateEasing(f1), 0, 1);
            }

            /**
             * @param t The unscaled square distance from the volcano, roughly in [0, 1.2]
             * @return A noise function determining the volcano's height at any given position, in the range [0, 1]
             */
            private static double calculateShape(double t) {
                if (t > 0.025) {
                    return (5 / (9 * t + 1) - 0.5) * 0.279173646008;
                } else {
                    double a = (t * 9 + 0.05);
                    return (8 * a * a + 2.97663265306) * 0.279173646008;
                }
            }

            @Override
            public boolean isValidBiome(BiomeExtension biome) {
                return biome.isVolcanic();
            }

            /**
             * Calculate the center of the nearest volcano, if one exists, to the given x, z, at the given y.
             */
            @Override
            @Nullable
            public BlockPos calculateCenter(int x, int y, int z, int rarity) {
                final TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                if (checkCellRarity(cell, rarity)) {
                    return new BlockPos((int) cell.x(), y, (int) cell.y());
                }
                return null;
            }
        };
    }

    public static CenteredFeatureNoiseSampler tuffRing(Seed seed) {
        return new CenteredFeatureNoiseSampler() {
            final TFGCellular2D cellNoise = new TFGCellular2D(seed.seed(), 0.2f, 1).spread(0.003f);
            final Noise2D jitterNoise = new OpenSimplex2D(seed.seed() + 1234123L).octaves(2).scaled(-0.032f, 0.032f).spread(0.064f);
            final Noise2D addedCliffNoise = new OpenSimplex2D(seed.seed()).octaves(2).spread(0.1).scaled(-2, 10);
            final Noise2D everywhereNoise = new OpenSimplex2D(seed.next()).octaves(3).spread(0.03).scaled(-10, 10);

            @Override
            public double setColumnAndSampleHeight(double heightIn, int x, int z, BiomeSourceExtension biomeSource) {
                TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                final BiomeExtension biome = biomeSource.getBiomeExtension(QuartPos.fromBlock((int) cell.x()), QuartPos.fromBlock((int) cell.y()));
                IBiomeExtension be = (IBiomeExtension) biome;
                if (be.tfg$hasTuffRings()) {
                    final int rarity = biome.getVolcanoRarity();
                    if (checkCellRarity(cell, rarity)) {
                        return modifyHeight(cell, x, z, biome, heightIn);
                    }
                }
                return TFGChunkHeightFiller.NOT_PRESENT_RETURN;
            }

            public BiomeExtension getCenterBiome(int x, int z, BiomeSourceExtension biomeSource) {
                TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                return biomeSource.getBiomeExtension(QuartPos.fromBlock((int) cell.x()), QuartPos.fromBlock((int) cell.y()));
            }

            private double modifyHeight(TFGCellular2D.TFGCell cell, int x, int z, BiomeExtension biome, double heightIn) {
                if (cell != null) {
                    IBiomeExtension be = (IBiomeExtension) biome;
                    final double f1 = cell.f1();
                    final double easing = Mth.clamp(calculateEasing((float) f1) + jitterNoise.noise(x, z), 0, 1);
                    final double shape = calculateShape(1 - easing);
                    final double ringAdditionalHeight = (shape * be.tfg$getCenteredFeatureScaleHeight() + (shape > 0.5 ? addedCliffNoise.noise(x, z) : 0f));
                    final double gapAdjustedAdditionalHeight = Math.min(ringAdditionalHeight * getGapVerticalEasing(cell), ringAdditionalHeight);
                    final double ringHeight = SEA_LEVEL_Y + be.tfg$getCenteredFeatureBaseHeight() + gapAdjustedAdditionalHeight + everywhereNoise.noise(x, z);

                    // Abruptly scales down to the baseHeight near the edges of cells
                    final double delta = 25 * Mth.clamp(cell.f2() - f1, 0, 0.04);
                    return Mth.lerp(delta, heightIn, Math.max(ringHeight, heightIn));
                }
                return heightIn;
            }

            /**
             * Calculate the closeness value to a tuff ring center, in the range [0, 1]. 1 = Center, 0 = Nowhere near.
             */
            public float calculateEasing(int x, int z, int rarity) {
                final TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                if (checkCellRarity(cell, rarity)) {
                    return calculateClampedEasing((float) cell.f1());
                }
                return 0;
            }

            private static float calculateClampedEasing(float f1) {
                return Mth.clamp(calculateEasing(f1), 0, 1);
            }

            private static float calculateEasing(float f1) {
                return Mth.map(f1, 0, 0.23f, 1, 0);
            }

            /**
             * @param t The unscaled square distance from the tuff ring center, roughly in [0, 1.2]
             * @return A noise function determining the tuff ring's height at any given position, in the range [0, 1]
             */
            private static double calculateShape(double t) {
                return t < 0.03f ? 0 // Flat
                        : t < 0.10f ? t * 7f - 0.21f // Slope up to 0.5
                                : t < 0.15f ? t * 6.6667f // Cliff, 0.67 slope to 1.0
                                        : t < 0.20f ? 1 - (t - 0.15f) * 6.6667f // Ridge, slope 1.0 down to 0.67
                                                : 1.5f - (5 * t); // Cliff base, slope 0.5 down
            }

            @Override
            public boolean isValidBiome(BiomeExtension biome) {
                return biome.isVolcanic();
            }

            /**
             * Calculate the center of the nearest tuff ring, if one exists, to the given x, z, at the given y.
             */
            @Override
            @Nullable
            public BlockPos calculateCenter(int x, int y, int z, int rarity) {
                final TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                if (checkCellRarity(cell, rarity)) {
                    return new BlockPos((int) cell.x(), y, (int) cell.y());
                }
                return null;
            }
        };
    }

    /**
     * Method for adding a single gap to a cellular feature at a random angle
     *
     * @return a value ranging from 1 far from the gap, to 0 in the middle of a large gap
     */
    private static double getGapVerticalEasing(TFGCellular2D.TFGCell cell) {
        // Gap size from 0 to 2
        final double gapSize = ((1 + cell.noise()) * 100) % 1;

        // Only adjust this if a gap should exist at all
        double gapVerticalEasing = 1;
        if (gapSize > 0) {
            final double aGap = 4 * (Math.abs(cell.noise() * 10000) % 1);
            final double a1 = cell.angle();
            final double angleToGap = Math.abs(a1 - aGap);
            // If angle to gap is larger than the gap size, we are far from the gap and can skip calculations
            if (angleToGap < gapSize) {
                final double angleToGapEdge = Math.abs(Math.min(angleToGap + gapSize, angleToGap - gapSize));

                // Gap scale should be lowest at the center of the gap
                gapVerticalEasing = Mth.clampedMap(angleToGapEdge, 0, Math.max(0.3, 0.6 * gapSize), 1, 0);
            }
        }
        return gapVerticalEasing;
    }

    public static CenteredFeatureNoiseSampler tuya(Seed seed) {
        return new CenteredFeatureNoiseSampler() {
            final TFGCellular2D cellNoise = new TFGCellular2D(seed.seed(), 0.21f, 1).spread(0.0033f);
            final Noise2D jitterNoise = new OpenSimplex2D(seed.seed() + 1234123L).octaves(2).scaled(-0.016f, 0.016f).spread(0.128f);
            final Noise2D addedNoise = new OpenSimplex2D(seed.seed()).octaves(2).spread(0.1).scaled(-2, 3);

            @Override
            public double setColumnAndSampleHeight(double heightIn, int x, int z, BiomeSourceExtension biomeSource) {
                TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                final BiomeExtension biome = biomeSource.getBiomeExtension(QuartPos.fromBlock((int) cell.x()), QuartPos.fromBlock((int) cell.y()));
                IBiomeExtension be = (IBiomeExtension) biome;
                final int rarity = biome.getVolcanoRarity();
                if (be.tfg$hasTuyas()) {
                    if (checkCellRarity(cell, rarity)) {
                        return modifyHeight(cell, x, z, biome, heightIn);
                    }
                }
                return TFGChunkHeightFiller.NOT_PRESENT_RETURN;
            }

            public BiomeExtension getCenterBiome(int x, int z, BiomeSourceExtension biomeSource) {
                TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                return biomeSource.getBiomeExtension(QuartPos.fromBlock((int) cell.x()), QuartPos.fromBlock((int) cell.y()));
            }

            public double modifyHeight(TFGCellular2D.TFGCell cell, double x, double z, BiomeExtension biome, double heightIn) {
                if (cell != null) {
                    IBiomeExtension be = (IBiomeExtension) biome;
                    final double easing = Mth.clamp(calculateEasing((float) cell.f1()) + jitterNoise.noise(x, z), 0, 1);
                    final double shape = be.tfg$getCenteredFeatureIce() ? calculateIcyShape(1 - easing) : calculateShape(1 - easing);
                    final double additionalHeight = (float) (shape * be.tfg$getCenteredFeatureScaleHeight() + addedNoise.noise(x, z));
                    final double tuyaHeight = SEA_LEVEL_Y + be.tfg$getCenteredFeatureBaseHeight() + additionalHeight;
                    return Mth.lerp(easing, heightIn, 0.5f * (tuyaHeight + Math.max(tuyaHeight, heightIn + 0.4f * additionalHeight)));
                }
                return heightIn;
            }

            /**
             * @param t The unscaled square distance from the tuya, roughly in [0, 1.2]
             * @return A noise function determining the tuya's height at any given position, in the range [0, 1]
             */
            private static double calculateShape(double t) {
                return t < 0.015f ? Mth.map(t, 0f, 0.015f, 0.8f, 1f)// Caldera
                        : t < 0.125f ? Mth.map(t, 0.025f, 0.125f, 1f, 0.75f) // Shallow slope down
                                : Mth.clampedMap(t, 0.125f, 0.16f, 0.75f, 0f); // Cliff
            }

            /**
             * @param t The unscaled square distance from the tuya, roughly in [0, 1.2]
             * @return A noise function determining the tuya's height at any given position, in the range [0, 1]
             */
            private static double calculateIcyShape(double t) {
                return t < 0.015f ? Mth.map(t, 0f, 0.015f, 0.8f, 1f) // Caldera
                        : t < 0.125f ? Mth.map(t, 0.025f, 0.125f, 1f, 0.75f) // Shallow slope down
                                : t < 0.16 ? Mth.map(t, 0.125f, 0.16f, 0.75f, 0f) // Cliff
                                        : Mth.clampedMap(t, 0.16f, 0.19f, 0f, 0.5f); // Ice edge
            }

            /**
             * Calculate the closeness value to a tuya, in the range [0, 1]. 1 = Center of a tuya, 0 = Nowhere near.
             */
            public float calculateEasing(int x, int z, int rarity) {
                final TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                if (checkCellRarity(cell, rarity)) {
                    return calculateClampedEasing((float) cell.f1());
                }
                return 0;
            }

            private static float calculateEasing(float f1) {
                return Mth.map(f1, 0, 0.23f, 1, 0);
            }

            private static float calculateClampedEasing(float f1) {
                return Mth.clamp(calculateEasing(f1), 0, 1);
            }

            @Override
            public boolean isValidBiome(BiomeExtension biome) {
                IBiomeExtension be = (IBiomeExtension) biome;
                return be.tfg$hasTuyas();
            }

            /**
             * Calculate the center of the nearest tuya, if one exists, to the given x, z, at the given y.
             */
            @Nullable
            public BlockPos calculateCenter(int x, int y, int z, int rarity) {
                final TFGCellular2D.TFGCell cell = cellNoise.cell(x, z);
                if (checkCellRarity(cell, rarity)) {
                    return new BlockPos((int) cell.x(), y, (int) cell.y());
                }
                return null;
            }
        };
    }

}
