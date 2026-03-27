package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.Set;

import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
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
        System.out.println("attempting to find climate weight at: " + pos);
        System.out.println(level);
        System.out.println(level.getChunkSource());
        System.out.println(level.getChunkSource().getGenerator());
        System.out.println(ChunkDataProvider.get(level.getChunkSource().getGenerator()));
        System.out.println(level.getChunk(pos));
        var chunkData = ChunkDataProvider.get(level.getChunkSource().getGenerator()).get(level.getChunk(pos));

        System.out.println(chunkData);
        System.out.println(chunkData.status());
        float value = mode == Mode.TEMPERATURE
                ? chunkData.getAverageTemp(pos)
                : chunkData.getRainfall(pos);
        System.out.println("found climate weight");
        return value >= min && value <= max ? addedWeight : 0;
    }

    public static ClimateWeightModifier combined(
            float tempMin, float tempMax,
            float rainMin, float rainMax,
            int addedWeight) {
        return new ClimateWeightModifier(null, 0, 0, addedWeight) {
            @Override
            public int applyAsInt(ServerLevel level, BlockPos pos) {
                System.out.println("attempting to find climate weight at: " + pos);

                var chunkData = ChunkDataProvider.get(level.getChunkSource().getGenerator()).get(level.getChunk(pos));
                System.out.println("a");
                float temp = chunkData.getAverageTemp(pos);
                System.out.println("b");
                float rain = chunkData.getRainfall(pos);
                System.out.println("c");
                System.out.println("found climate weight");

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
                System.out.println("attempting to find climate weight at: " + pos);

                var chunkData = ChunkDataProvider.get(level.getChunkSource().getGenerator()).get(level.getChunk(pos));
                System.out.println("a");
                float temp = chunkData.getAverageTemp(pos);
                System.out.println("b");
                float rain = chunkData.getRainfall(pos);
                System.out.println("c");
                var biome = level.getBiome(pos).unwrapKey().orElse(null);
                System.out.println("found climate weight");

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
