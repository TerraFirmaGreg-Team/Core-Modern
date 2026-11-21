package su.terrafirmagreg.core.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.common.block.VenusFireBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Venus;

public final class VenusEnvironmentalHelpers {

    public static final int ACID_FIRE_CHANCE = 4;

    public static void tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler) {
        if (!level.dimension().equals(Planet.VENUS) || !level.isRaining() || level.random.nextInt(ACID_FIRE_CHANCE) != 0)
            return;

        final ChunkPos chunkPos = chunk.getPos();
        final BlockPos lcgPos = level.getBlockRandomPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), 15);
        final BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, lcgPos);

        if (!level.isRainingAt(surfacePos))
            return;

        profiler.push("tfgVenusAcidFire");
        if (level.getBlockState(surfacePos).isAir()) {
            if (VenusFireBlock.canBurnAt(level, surfacePos.below())) {
                level.setBlockAndUpdate(surfacePos, TFGBlocks_Venus.VENUS_FIRE.getDefaultState());
            }
        } else if (VenusFireBlock.canBurnAt(level, surfacePos)) {
            VenusFireBlock.burn(level, surfacePos);
        }
        profiler.pop();
    }
}
