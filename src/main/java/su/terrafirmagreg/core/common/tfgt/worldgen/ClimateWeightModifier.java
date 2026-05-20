package su.terrafirmagreg.core.common.tfgt.worldgen;

import java.util.*;
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

import su.terrafirmagreg.core.TFGCore;

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

    /// Returns Map keyed by "temperature" and/or "rainfall"
    /// @return Map of List(float) containing minValue, maxValue
    public Map<Mode, List<Float>> getClimates() {
        List<Float> climate = new ArrayList<>(List.of(min, max));
        return new HashMap<>(Map.of(mode, climate));
    }

    /// Return Null unless overridden, or
    /// @return Set of ResourceKey(biome)
    public Set<ResourceKey<Biome>> getBiomes() {
        return null;
    }

    /// @throws NullPointerException If there's an existing fluid drilling rig in an old worldgen chunk
    public ChunkData getChunkData(ServerLevel level, BlockPos pos) {
        ChunkAccess currentChunk = CHUNK_ACCESS_CACHE.get(new ChunkPos(pos));
        return ChunkDataProvider.get(level.getChunkSource().getGenerator()).get(currentChunk);
    }

    public int applyAsInt(ServerLevel level, BlockPos pos) {
        try {
            ChunkData chunkData = getChunkData(level, pos);
            float value = mode == Mode.TEMPERATURE
                    ? chunkData.getAverageTemp(pos)
                    : chunkData.getRainfall(pos);
            return value >= min && value <= max ? addedWeight : 0;
        } catch (NullPointerException | IllegalStateException ex) {
            TFGCore.LOGGER.error(ex.toString());
            return 0;
        }
    }

    public static ClimateWeightModifier combined(
            float tempMin, float tempMax,
            float rainMin, float rainMax,
            int addedWeight) {
        return new ClimateWeightModifier(null, 0, 0, addedWeight) {
            @Override
            public Map<Mode, List<Float>> getClimates() {
                List<Float> tempList = new ArrayList<>(List.of(tempMin, tempMax));
                List<Float> rainList = new ArrayList<>(List.of(rainMin, rainMax));
                return new HashMap<>(Map.of(
                        Mode.TEMPERATURE, tempList,
                        Mode.RAINFALL, rainList));
            }

            @Override
            public int applyAsInt(ServerLevel level, BlockPos pos) {
                try {
                    ChunkData chunkData = getChunkData(level, pos);
                    float temp = chunkData.getAverageTemp(pos);
                    float rain = chunkData.getRainfall(pos);

                    return temp >= tempMin && temp <= tempMax
                            && rain >= rainMin && rain <= rainMax
                                    ? addedWeight
                                    : 0;
                } catch (NullPointerException | IllegalStateException ex) {
                    TFGCore.LOGGER.error(ex.toString());
                    return 0;
                }
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
            public Set<ResourceKey<Biome>> getBiomes() {
                return biomes;
            }

            @Override
            public Map<Mode, List<Float>> getClimates() {
                List<Float> tempList = new ArrayList<>(List.of(tempMin, tempMax));
                List<Float> rainList = new ArrayList<>(List.of(rainMin, rainMax));
                return new HashMap<>(Map.of(
                        Mode.TEMPERATURE, tempList,
                        Mode.RAINFALL, rainList));
            }

            @Override
            public int applyAsInt(ServerLevel level, BlockPos pos) {
                try {
                    ChunkData chunkData = getChunkData(level, pos);
                    float temp = chunkData.getAverageTemp(pos);
                    float rain = chunkData.getRainfall(pos);
                    var biome = level.getBiome(pos).unwrapKey().orElse(null);

                    return temp >= tempMin && temp <= tempMax
                            && rain >= rainMin && rain <= rainMax
                            && (biomes.isEmpty() || biomes.contains(biome))
                                    ? addedWeight
                                    : 0;
                } catch (NullPointerException | IllegalStateException ex) {
                    TFGCore.LOGGER.error(ex.toString());
                    return 0;
                }
            }
        };
    }
}
