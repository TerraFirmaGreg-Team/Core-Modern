/* Originally from [TerraFirmaCraft] (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package su.terrafirmagreg.core.world.placements;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.world.noise.Noise2D;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import su.terrafirmagreg.core.world.TFGPlacements;
import su.terrafirmagreg.core.world.new_ow_wg.Seed;
import su.terrafirmagreg.core.world.new_ow_wg.noise.TFGBiomeNoise;

public class IntertidalPlacement extends PlacementModifier {
    public static final Codec<IntertidalPlacement> PLACEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("min_elevation", -64).forGetter(c -> c.minElevation),
            Codec.INT.optionalFieldOf("max_elevation", 320).forGetter(c -> c.maxElevation)).apply(instance, IntertidalPlacement::new));

    private final int minElevation;
    private final int maxElevation;

    public IntertidalPlacement(int minElevation, int maxElevation) {
        this.minElevation = minElevation;
        this.maxElevation = maxElevation;
    }

    public int getMinElevation() {
        return minElevation;
    }

    public int getMaxElevation() {
        return maxElevation;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TFGPlacements.INTERTIDAL.get();
    }

    public boolean isValid(Noise2D highTideNoise, BlockPos pos) {
        final int heightDiff = (int) (pos.getY() - highTideNoise.noise(pos.getX(), pos.getZ()));
        return minElevation <= heightDiff && heightDiff <= maxElevation;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        final Seed seed = Seed.of(context.getLevel().getSeed());
        Noise2D highTideHeightNoise = TFGBiomeNoise.shoreTideLevelNoise(seed);
        if (isValid(highTideHeightNoise, pos)) {
            return Stream.of(pos);
        }
        return Stream.empty();
    }
}
