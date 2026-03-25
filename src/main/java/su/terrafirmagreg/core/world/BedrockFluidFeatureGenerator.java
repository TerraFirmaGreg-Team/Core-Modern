package su.terrafirmagreg.core.world;

import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;

import su.terrafirmagreg.core.world.feature.FluidGasVentFeature;
import su.terrafirmagreg.core.world.feature.FluidPoolFeature;

public class BedrockFluidFeatureGenerator {

    public static void generateSpout(ServerLevel serverLevel, ChunkPos chunkPos,
            ResourceLocation featureId) {

        var cfRegistry = serverLevel.registryAccess()
                .registry(Registries.CONFIGURED_FEATURE)
                .orElse(null);
        if (cfRegistry == null)
            return;

        var configuredFeature = cfRegistry.get(featureId);
        if (configuredFeature == null)
            return;

        if (!(configuredFeature.config() instanceof FluidSproutConfiguration))
            return;

        if (serverLevel.random.nextFloat() > 0.01f)
            return;

        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int originY = serverLevel.getMinBuildHeight() + (surfaceY - serverLevel.getMinBuildHeight()) / 2;

        configuredFeature.place(serverLevel,
                serverLevel.getChunkSource().getGenerator(),
                serverLevel.random,
                new BlockPos(x, originY, z));
    }

    public static void generateStructure(ServerLevel serverLevel, ChunkPos chunkPos,
            ResourceLocation featureId) {

        var cfRegistry = serverLevel.registryAccess()
                .registry(Registries.CONFIGURED_FEATURE)
                .orElse(null);
        if (cfRegistry == null)
            return;

        var configuredFeature = cfRegistry.get(featureId);
        if (configuredFeature == null)
            return;

        if (!(configuredFeature.feature() instanceof FluidGasVentFeature))
            return;

        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

        configuredFeature.place(serverLevel,
                serverLevel.getChunkSource().getGenerator(),
                serverLevel.random,
                new BlockPos(x, surfaceY, z));
    }

    public static void generatePool(ServerLevel serverLevel, ChunkPos chunkPos,
            ResourceLocation featureId) {

        var cfRegistry = serverLevel.registryAccess()
                .registry(Registries.CONFIGURED_FEATURE)
                .orElse(null);
        if (cfRegistry == null)
            return;

        var configuredFeature = cfRegistry.get(featureId);
        if (configuredFeature == null)
            return;

        if (!(configuredFeature.feature() instanceof FluidPoolFeature))
            return;

        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = serverLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);

        configuredFeature.place(serverLevel,
                serverLevel.getChunkSource().getGenerator(),
                serverLevel.random,
                new BlockPos(x, surfaceY, z));
    }
}
