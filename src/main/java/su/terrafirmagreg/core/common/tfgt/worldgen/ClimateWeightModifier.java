package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

import lombok.Getter;

@Getter
public class ClimateWeightModifier {

    public static final ConcurrentHashMap<ChunkPos, ChunkAccess> CHUNK_ACCESS_CACHE = new ConcurrentHashMap<>();

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

    public ChunkData getChunkData(ServerLevel level, BlockPos pos) {
        ChunkAccess currentChunk = CHUNK_ACCESS_CACHE.get(new ChunkPos(pos));
        return ChunkDataProvider.get(level.getChunkSource().getGenerator()).get(currentChunk);
    }

    public int applyAsInt(ServerLevel level, BlockPos pos) {
        ChunkData chunkData = getChunkData(level, pos);

        float value = mode == Mode.TEMPERATURE
                ? chunkData.getAverageTemp(pos)
                : chunkData.getRainfall(pos);
        return value >= min && value <= max ? addedWeight : 0;
    }

    public static ClimateWeightModifier combined(
            float tempMin, float tempMax,
            float rainMin, float rainMax,
            int addedWeight) {
        return new ClimateWeightModifier(null, 0, 0, addedWeight) {
            @Override
            public int applyAsInt(ServerLevel level, BlockPos pos) {

                ChunkData chunkData = getChunkData(level, pos);
                float temp = chunkData.getAverageTemp(pos);
                float rain = chunkData.getRainfall(pos);

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

                ChunkData chunkData = getChunkData(level, pos);
                float temp = chunkData.getAverageTemp(pos);
                float rain = chunkData.getRainfall(pos);
                var biome = level.getBiome(pos).unwrapKey().orElse(null);

                return temp >= tempMin && temp <= tempMax
                        && rain >= rainMin && rain <= rainMax
                        && (biomes.isEmpty() || biomes.contains(biome))
                                ? addedWeight
                                : 0;
            }
        };
    }
}
