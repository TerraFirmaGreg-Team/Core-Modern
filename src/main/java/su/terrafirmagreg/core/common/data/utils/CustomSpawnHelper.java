package su.terrafirmagreg.core.common.data.utils;

import java.util.HashMap;
import java.util.Map;

import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeSourceExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.config.TFGConfig;

public class CustomSpawnHelper {

    public static CustomSpawnCondition getFromConfig() {
        return CUSTOM_SPAWN_CONDITIONS.get(TFGConfig.COMMON.NEW_WORLD_SPAWN.get());
    }

    public static void resetConfigValue() {
        TFGConfig.COMMON.NEW_WORLD_SPAWN.set(DEFAULT_SPAWN.id);
    }

    public static boolean testWithinRanges(float temperature, float rainfall, CustomSpawnCondition condition) {
        float[] tempRange = condition.temperatureRange;
        float[] rainRange = condition.rainfallRange;

        //System.out.println(tempRange[0] + " <= " + temperature + " <= " + tempRange[1]);
        //System.out.println(rainRange[0] + " <= " + rainfall + " <= " + rainRange[1]);
        if (tempRange[0] <= temperature && temperature <= tempRange[1]) {
            //System.out.println("Temp Match");
            if (rainRange[0] <= rainfall && rainfall <= rainRange[1]) {
                //System.out.println("Rain Match");
                return true;
            }
        }
        return false;
    }

    //Adapted from TFC code, but with more config
    public static BlockPos findSpawnBiome(int spawnCenterX, int spawnCenterZ, int spawnRadius, RandomSource random, ChunkGeneratorExtension extension) {
        int step = Math.max(1, spawnRadius / 256);
        int centerX = QuartPos.fromBlock(spawnCenterX);
        int centerZ = QuartPos.fromBlock(spawnCenterZ);
        int maxRadius = QuartPos.fromBlock(spawnRadius);
        BlockPos found = null;
        int count = 0;

        for (int radius = maxRadius; radius <= maxRadius; radius += step) {
            for (int dx = -radius; dx <= radius; dx += step) {
                for (int dz = -radius; dz <= radius; dz += step) {
                    int quartX = centerX + dz;
                    int quartZ = centerZ + dx;
                    BiomeExtension biome = ((BiomeSourceExtension) extension.self().getBiomeSource()).getBiomeExtensionNoRiver(quartX, quartZ);
                    if (biome.isSpawnable()) {
                        if (found == null || random.nextInt(count + 1) == 0) {
                            found = new BlockPos(QuartPos.toBlock(quartX), 0, QuartPos.toBlock(quartZ));
                        }

                        ++count;
                    }
                }
            }
        }

        if (found == null) {
            TerraFirmaCraft.LOGGER.warn("Unable to find spawn biome!");
            return new BlockPos(spawnCenterX, 0, spawnCenterZ);
        } else {
            return found;
        }
    }

    public static HashMap<String, CustomSpawnCondition> CUSTOM_SPAWN_CONDITIONS = new HashMap<>();

    public static HashMap<String, MutableComponent> SPAWN_DIFFICULTIES = new HashMap<>(Map.of(
            "easy", Component.translatable("tfg.gui.spawn_difficulty.easy"),
            "normal", Component.translatable("tfg.gui.spawn_difficulty.normal"),
            "hard", Component.translatable("tfg.gui.spawn_difficulty.hard"),
            "extreme", Component.translatable("tfg.gui.spawn_difficulty.extreme")));

    public static final CustomSpawnCondition DESERT_SPAWN = new CustomSpawnCondition(
            "desert",
            -10000,
            10000,
            1,
            new float[] { 20f, 30f },
            new float[] { 0f, 90f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("extreme"));

    public static final CustomSpawnCondition POLAR_SPAWN = new CustomSpawnCondition(
            "polar",
            -2000,
            -10000,
            1,
            new float[] { -20f, -10f },
            new float[] { 100f, 250f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("hard"));

    public static final CustomSpawnCondition TEMPERATE_SPAWN = new CustomSpawnCondition(
            "temperate",
            2500,
            1500,
            1,
            new float[] { 5f, 15f },
            new float[] { 250f, 350f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("normal"));

    public static final CustomSpawnCondition TROPICAL_SPAWN = new CustomSpawnCondition(
            "tropical",
            10000,
            10000,
            1,
            new float[] { 20f, 30f },
            new float[] { 350f, 500f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("easy"));

    public static final CustomSpawnCondition BENEATH_SPAWN = new CustomSpawnCondition(
            "beneath",
            0,
            0,
            1,
            new float[] { -20f, 20f },
            new float[] { 0f, 400f },
            Level.NETHER,
            SPAWN_DIFFICULTIES.get("normal"));

    public static final CustomSpawnCondition DEFAULT_SPAWN = new CustomSpawnCondition(
            "default",
            0,
            0,
            1,
            new float[] { -20f, 20f },
            new float[] { 0f, 400f },
            Level.OVERWORLD,
            SPAWN_DIFFICULTIES.get("normal"));

    /**
     * Registers a new CustomSpawnCondition in the CUSTOM_SPAWN_CONDITIONS map.
     * @param condition The CustomSpawnCondition to register.
     */
    private static void initNewType(CustomSpawnCondition condition) {
        CUSTOM_SPAWN_CONDITIONS.put(condition.id, condition);
    }

    static {
        initNewType(DESERT_SPAWN);
        initNewType(POLAR_SPAWN);
        initNewType(DEFAULT_SPAWN);
        initNewType(TEMPERATE_SPAWN);
        initNewType(TROPICAL_SPAWN);
        initNewType(BENEATH_SPAWN);
    }

    /// Holds spawn conditions for a particular custom world spawn
    /// @param id string used for mapping
    /// @param spawnCenterX int block pos estimate on the X axis
    /// @param spawnCenterZ int block pos estimate on the Z axis
    /// @param spawnRadiusMultiplier int multiplier on default radius
    /// @param temperatureRange inclusive range to check for the temperature
    /// @param rainfallRange inclusive range to check for the rainfall
    /// @param dimension level that this spawn occurs in
    /// @param difficulty translatable component that displays the difficulty
    public record CustomSpawnCondition(
            String id,
            int spawnCenterX,
            int spawnCenterZ,
            int spawnRadiusMultiplier,
            float[] temperatureRange,
            float[] rainfallRange,
            ResourceKey<Level> dimension,
            MutableComponent difficulty) {
    }
}
