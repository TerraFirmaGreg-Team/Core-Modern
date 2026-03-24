/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.placements;

import java.util.stream.Stream;

import net.dries007.tfc.world.biome.BiomeExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.biome.TFGBiomes;
import su.terrafirmagreg.core.world.new_ow_wg.noise.CenteredFeatureNoiseSampler;

/**
 * A placement modifier for an arbitrary {@link CenteredFeatureNoiseSampler} instance. A subclass only
 * needs to override {@link #createContext(Seed)} to be able to provide either "generate at the center of",
 * or "generate within a distance of", these features.
 */
public abstract class CenterOrDistanceToPlacement<T extends CenteredFeatureNoiseSampler> extends PlacementModifier {
    final boolean center;
    final float distance;

    private final ThreadLocal<LocalContext<T>> localContext;

    public CenterOrDistanceToPlacement(boolean center, float distance) {
        this.center = center;
        this.distance = distance;
        this.localContext = ThreadLocal.withInitial(() -> null);
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        final WorldGenLevel level = context.getLevel();
        final long seed = level.getSeed();

        LocalContext<T> local = localContext.get();
        if (local == null || local.seed != seed) {
            local = new LocalContext<>(seed, createContext(Seed.unsafeOf(seed)));
            localContext.set(local);
        }

        final Biome biome = level.getBiome(pos).value();
        final BiomeExtension extension = TFGBiomes.getExtensionOrThrow(level, biome);
        if (local.context.isValidBiome(extension)) {
            if (center) {
                final BlockPos centerPos = local.context.calculateCenter(pos, extension);
                if (centerPos != null &&
                        SectionPos.blockToSectionCoord(centerPos.getX()) == SectionPos.blockToSectionCoord(pos.getX()) &&
                        SectionPos.blockToSectionCoord(centerPos.getZ()) == SectionPos.blockToSectionCoord(pos.getZ()) &&
                        // We only check whether the center biome is correct for the center version of the feature, because this check
                        // only works when the center is in the chunk we are placing within
                        local.context.isValidBiome(TFGBiomes.getExtensionOrThrow(level, level.getBiome(centerPos).value()))) {
                    return Stream.of(centerPos);
                }
            } else if (local.context.calculateEasing(pos, extension) > this.distance) {
                return Stream.of(pos);
            }
        }
        return Stream.empty();
    }

    protected abstract T createContext(Seed seed);

    private record LocalContext<T>(long seed, T context) {
    }
}
