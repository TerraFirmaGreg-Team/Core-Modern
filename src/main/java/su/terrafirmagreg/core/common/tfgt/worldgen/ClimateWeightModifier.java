package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.Set;

import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

public class ClimateWeightModifier {

    public enum Mode {
        TEMPERATURE, RAINFALL
    }

    private final Mode mode;
    private final float min;
    private final float max;
    private final int addedWeight;

    public ClimateWeightModifier(Mode mode, float min, float max, int addedWeight) {
        this.mode = mode;
        this.min = min;
        this.max = max;
        this.addedWeight = addedWeight;
    }

    public int applyAsInt(ServerLevel level, BlockPos pos) {
        float value = mode == Mode.TEMPERATURE
                ? Climate.getAverageTemperature(level, pos)
                : Climate.getRainfall(level, pos);
        return value >= min && value <= max ? addedWeight : 0;
    }

    public static ClimateWeightModifier combined(
            float tempMin, float tempMax,
            float rainMin, float rainMax,
            int addedWeight) {
        return new ClimateWeightModifier(null, 0, 0, addedWeight) {
            @Override
            public int applyAsInt(ServerLevel level, BlockPos pos) {
                float temp = Climate.getAverageTemperature(level, pos);
                float rain = Climate.getRainfall(level, pos);
                return temp >= tempMin && temp <= tempMax
                        && rain >= rainMin && rain <= rainMax
                                ? addedWeight
                                : 0;
            }
        };
    }

    public static ClimateWeightModifier combinedWithBiome(
            float tempMin, float tempMax,
            float rainMin, float rainMax,
            Set<ResourceKey<Biome>> biomes,
            int addedWeight) {
        return new ClimateWeightModifier(null, 0, 0, addedWeight) {
            @Override
            public int applyAsInt(ServerLevel level, BlockPos pos) {
                float temp = Climate.getAverageTemperature(level, pos);
                float rain = Climate.getRainfall(level, pos);
                var biome = level.getBiome(pos).unwrapKey().orElse(null);
                return temp >= tempMin && temp <= tempMax
                        && rain >= rainMin && rain <= rainMax
                        && (biomes.isEmpty() || biomes.contains(biome))
                                ? addedWeight
                                : 0;
            }
        };
    }

    public Mode getMode() {
        return mode;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public int getAddedWeight() {
        return addedWeight;
    }
}
