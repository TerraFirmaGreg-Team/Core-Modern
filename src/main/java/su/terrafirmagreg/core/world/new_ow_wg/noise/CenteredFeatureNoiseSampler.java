/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.new_ow_wg.noise;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.minecraft.core.BlockPos;

/**
 * Interface for cellular noise features that are dependent on the biome at the center of a 2D cell
 */
public interface CenteredFeatureNoiseSampler {
    default double setColumnAndSampleHeight(double heightIn, int x, int z, BiomeSourceExtension biomeSource) {
        return heightIn;
    }

    BiomeExtension getCenterBiome(int x, int z, BiomeSourceExtension biomeSource);

    /**
     * @return {@code true} if {@code biome} can generate these noise features at all.
     */
    boolean isValidBiome(BiomeExtension biome);

    /**
     * @return A {@code rarity} parameter for this noise feature within the given biome. Will only be called if {@link #isValidBiome}
     * returns {@code true}. This value is provided to the following methods.
     */
    default int getRarity(BiomeExtension biome) {
        return biome.getVolcanoRarity();
    }

    /**
     * @return A value representing how close we are to a given center point in {@code [0, 1]}, where higher values are closer to the center.
     */
    default float calculateEasing(BlockPos pos, BiomeExtension biome) {
        return calculateEasing(pos.getX(), pos.getZ(), getRarity(biome));
    }

    /**
     * @return A value representing how close we are to a given center point in {@code [0, 1]}, where higher values are closer to the center.
     */
    float calculateEasing(int x, int z, int rarity);

    /**
     * @return The nearest center point to the given position.
     */
    @Nullable
    default BlockPos calculateCenter(BlockPos pos, BiomeExtension biome) {
        return calculateCenter(pos.getX(), pos.getY(), pos.getZ(), getRarity(biome));
    }

    /**
     * @return The nearest center point to the given position.
     */
    @Nullable
    BlockPos calculateCenter(int x, int y, int z, int rarity);

    /**
     * Sample the nearest cellular noise feature cell to a given position.
     * Returns false if the cell was excluded due to a rarity condition, or if the cell was too close to adjacent cells (possibly causing overlapping volcanoes)
     */
    default boolean checkCellRarity(TFGCellular2D.TFGCell cell, int rarity) {
        if (rarity == 0)
            return false;
        return Math.abs(cell.noise()) <= 1.0 / rarity;
    }
}
