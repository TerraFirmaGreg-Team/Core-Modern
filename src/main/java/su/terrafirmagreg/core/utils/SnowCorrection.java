package su.terrafirmagreg.core.utils;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.IcePileBlock;
import net.dries007.tfc.common.blocks.IcicleBlock;
import net.dries007.tfc.common.blocks.SnowPileBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.ThinSpikeBlock;
import net.dries007.tfc.common.blocks.plant.KrummholzBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.ClimateModel;
import net.dries007.tfc.util.tracker.WorldTracker;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGPoiTypes;
import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.mixins.common.minecraft.PoiSectionAccessor;
import su.terrafirmagreg.core.mixins.common.minecraft.SectionStorageAccessor;
import su.terrafirmagreg.core.world.IChunkData;

/*
port of TFC's 1.21 new snow melting logic
- Gets time since last tick in the chunk
- If more than 4000 ticks have passed, continue
- Do math every 4000 ticks until reach current tick or 1 month
- Add or melt snow based on results
*/
public class SnowCorrection {
    private static final Holder<PoiType> CLIMATE = TFGPoiTypes.CLIMATE.getHolder().orElseThrow();
    private static final int TICKS_PER_SNOW_ACCUMULATION = 80;
    private static final int TICKS_PER_SNOW_MELT_PER_SNOW_ACCUMULATION = 3;
    private static final int TICKS_PER_SNOW_MELT = TICKS_PER_SNOW_ACCUMULATION * TICKS_PER_SNOW_MELT_PER_SNOW_ACCUMULATION;

    private static final int UPDATES_PER_SNOW_MELT_SKIP = 1 + 4_000 / TICKS_PER_SNOW_MELT;
    private static final int UPDATES_PER_SNOW_ACCUMULATION_SKIP = 1 + 4_000 / TICKS_PER_SNOW_ACCUMULATION;

    private static final int MAX_UPDATES_PER_TICK = TFGConfig.SERVER.snowMaxAccumulationOnUpdate.get();

    public static void onTickChunk(ServerLevel level, ChunkAccess chunk) {
        if (MAX_UPDATES_PER_TICK <= 0) {
            return;
        }
        if (!(chunk instanceof LevelChunk levelChunk)) {
            return;
        }

        final WorldTracker tracker = WorldTracker.get(level);
        final ClimateModel model = tracker.getClimateModel();
        final ChunkPos chunkPos = chunk.getPos();
        final ChunkData data = ChunkData.get(levelChunk);
        final long currentTick = Calendars.SERVER.getTicks();
        final long currentCalendarTick = Calendars.SERVER.getCalendarTicks();
        final long lastRandomTick = ((IChunkData) data).tfg$getLastRandomTick();
        final long timeSinceTick = currentTick - lastRandomTick;
        final BlockPos climateCheckSurfacePos = getRandomSurfacePos(level, chunkPos);
        final int daysInMonth = Calendars.SERVER.getCalendarDaysInMonth();
        final float rainfall = data.getRainfall(climateCheckSurfacePos);
        if (timeSinceTick > 4_000) {
            long calendarTick = currentCalendarTick - Math.min(192_000, timeSinceTick);
            int netChangeInSnow = 0;
            while (calendarTick < currentCalendarTick) {
                calendarTick += 4_000;
                final float estimatedTemperature = model.getTemperature(level, climateCheckSurfacePos, calendarTick, daysInMonth);
                if (estimatedTemperature > 0.5f) {
                    netChangeInSnow = netChangeInSnow - UPDATES_PER_SNOW_MELT_SKIP;
                }
                if (estimatedTemperature < -2f && isRaining(rainfall, calendarTick, level)) {
                    final float fuzz = Mth.clampedMap(estimatedTemperature, -2f, -12f, 0.5f, 1f);
                    netChangeInSnow = netChangeInSnow + (int) (UPDATES_PER_SNOW_ACCUMULATION_SKIP * fuzz);
                }

            }
            if (netChangeInSnow > 0) {
                // Then, if we're performing a large number of updates, we want to first count the amount of snow in the chunk,
                // and only do updates if it's between a threshold
                netChangeInSnow = Math.min(MAX_UPDATES_PER_TICK, Math.min(256 - countExistingSnowInChunk(level, chunkPos), netChangeInSnow));

                for (int i = 0; i < netChangeInSnow; i++) {
                    handleSnowAccumulation(level, getSequentialSurfacePos(level, chunkPos, chunk, data, true));
                }
            } else if (netChangeInSnow < 0) {
                // If it has been more than a month since the chunk was ticked,
                // apply a multiplier to the melt based on how long it has been
                final int meltFactor = (int) (Math.max(timeSinceTick / 192_000, 1));
                netChangeInSnow = Math.min(MAX_UPDATES_PER_TICK, -netChangeInSnow * meltFactor);
                handleSnowMelting(level, chunkPos, netChangeInSnow);
            }
            //In the original, this would've done the current melting / accumulation after, but since this is a backport, we just do the correction and leave it at that.
            ((IChunkData) data).tfg$setLastRandomTick(chunk, currentTick);
        }
        //Random check if workers will be enraged by the snow (every 10 seconds)
        if (currentTick % 240 == 0 && (model.getTemperature(level, climateCheckSurfacePos, currentCalendarTick, daysInMonth) > 2f)) {
            handleSnowMelting(level, chunkPos, 1000);
        }
    }

    private static BlockPos getRandomSurfacePos(ServerLevel level, ChunkPos chunkPos) {
        final BlockPos randomPos = level.getBlockRandomPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), 15);
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomPos);
    }

    // Replicates normal rain chances
    private static boolean isRaining(float rainfall, float raintick, ServerLevel level) {
        if ((new Random((long) (level.getSeed() + Math.floor(raintick / 6000)))).nextDouble() > 0.1875) {
            return false;
        }
        return Math.random() - Mth.clampedMap(rainfall, 0f, 500f, 1, 0) > 0;
    }

    @SuppressWarnings("unchecked")
    private static SectionStorageAccessor<PoiSection> getPoiManager(ServerLevel level) {
        return (SectionStorageAccessor<PoiSection>) level.getPoiManager();
    }

    @Nullable
    private static Set<PoiRecord> getPoiRecords(SectionStorageAccessor<PoiSection> poi, ChunkPos chunkPos, int sectionY) {
        final long sectionKey = SectionPos.asLong(chunkPos.x, sectionY, chunkPos.z);
        final Optional<PoiSection> section = poi.invoke$getOrLoad(sectionKey);
        return section.isPresent() ? ((PoiSectionAccessor) section.get()).accessor$byType().get(CLIMATE) : null;
    }

    private static int countExistingSnowInChunk(ServerLevel level, ChunkPos chunkPos) {
        int total = 0;

        final SectionStorageAccessor<PoiSection> poi = getPoiManager(level);
        for (int sectionY = level.getMaxSection() - 1; sectionY >= level.getMinSection(); sectionY--) {
            final Set<PoiRecord> objects = getPoiRecords(poi, chunkPos, sectionY);
            if (objects != null) {
                total += objects.size();
            }
        }
        return total;
    }

    private static void handleSnowMelting(ServerLevel level, ChunkPos chunkPos, int amount) {
        // PoiManager doesn't have the methods we need, and they look pretty slow. We just need a randomly sampled poi from this chunk, and we
        // don't really care about section. So this is likely more efficient.
        final SectionStorageAccessor<PoiSection> poi = getPoiManager(level);
        for (int sectionY = level.getMinSection(); sectionY < level.getMaxSection(); sectionY++) {
            final Set<PoiRecord> entries = getPoiRecords(poi, chunkPos, sectionY);
            if (entries != null && !entries.isEmpty()) {
                // Handle two cases:
                // - removing all (amount >= entries.size())
                // - removing some (amount < entries.size())
                final List<PoiRecord> copyOfEntries = new ArrayList<>(entries); // Must be a mutable view, since we swap to random sample later
                if (amount >= copyOfEntries.size()) {
                    for (PoiRecord entry : copyOfEntries) {
                        removeSnowAt(level, entry.getPos());
                    }
                    amount -= copyOfEntries.size();
                } else {
                    final List<PoiRecord> sampleOfEntries = Helpers.uniqueRandomSample(copyOfEntries, amount, level.random);
                    for (PoiRecord entry : sampleOfEntries) {
                        removeSnowAt(level, entry.getPos());
                    }
                    amount -= sampleOfEntries.size();
                }

                if (amount <= 0) {
                    return;
                }
            }
        }
    }

    private static void removeSnowAt(ServerLevel level, BlockPos pos) {
        // Snow melting - both snow and snow piles
        BlockState state = level.getBlockState(pos);
        if (isSnow(state)) {
            // When melting snow, we melt layers at +2 from expected, while the temperature is still below zero
            // This slowly reduces massive excess amounts of snow, if they're present, but doesn't actually start melting snow a lot when we're still below freezing.
            SnowPileBlock.removePileOrSnow(level, pos, state);
        } else if (state.getBlock() instanceof KrummholzBlock) {
            KrummholzBlock.updateFreezingInColumn(level, pos, false);
        } else if (isIce(state)) {
            IcePileBlock.removeIcePileOrIce(level, pos, state);
        } else if (state.getBlock() == TFCBlocks.ICICLE.get()) {
            // Scan downwards to find the lowest icicle in the column to melt
            final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

            cursor.setWithOffset(pos, Direction.DOWN);
            BlockState belowState = level.getBlockState(cursor);
            while (belowState.getBlock() == TFCBlocks.ICICLE.get()) {
                cursor.move(Direction.DOWN);
                belowState = level.getBlockState(cursor);
            }

            cursor.move(Direction.UP);
            level.removeBlock(cursor, false); // Remove the icicle
            cursor.move(Direction.UP);

            // Update the block above, if it is also an icicle
            final BlockState stateAbove = level.getBlockState(cursor);
            if (stateAbove.getBlock() == TFCBlocks.ICICLE.get()) {
                level.setBlock(cursor, stateAbove.setValue(IcicleBlock.TIP, true), Block.UPDATE_ALL);
            }
        }
    }

    public static boolean isSnow(BlockState state) {
        return state.getBlock() == Blocks.SNOW || state.getBlock() == TFCBlocks.SNOW_PILE.get();
    }

    public static boolean isIce(BlockState state) {
        return state.getBlock() == Blocks.ICE || state.getBlock() == TFCBlocks.ICE_PILE.get() || state.getBlock() == TFCBlocks.SEA_ICE.get();
    }

    private static void handleSnowAccumulation(ServerLevel level, BlockPos surfacePos) {
        // Handle up to two block tall plants if they can be piled
        // This means we need to check three levels deep
        BlockPos groundPos, belowGroundPos;

        if (placeSnowOrSnowPile(level, surfacePos))
            return;
        if (placeSnowOrSnowPile(level, groundPos = surfacePos.below()))
            return;
        if (placeSnowOrSnowPile(level, belowGroundPos = surfacePos.below(2)))
            return;

        // Otherwise, try placing an ice pile
        // First, since we want to handle water with a single block above, if we find no water, but we find one below, we choose that instead
        // However, we have to also exclude ice here, since we don't intend to freeze two layers down
        BlockState groundState = level.getBlockState(groundPos);
        if (isIce(groundState)) {
            return;
        }
        if (groundState.getFluidState().getType() != Fluids.WATER) {
            groundPos = belowGroundPos;
            groundState = level.getBlockState(groundPos);
        }

        IcePileBlock.placeIcePileOrIce(level, groundPos, groundState, false);

        // Then place icicles at a lower rate, under overhangs. The lower rate is because the search for icicles is mildly expensive of a check
        if (level.random.nextInt(16) == 0) {
            // Place icicles under overhangs
            final BlockPos iciclePos = findIcicleLocation(level, surfacePos);
            if (iciclePos != null) {
                BlockPos posAbove = iciclePos.above();
                BlockState stateAbove = level.getBlockState(posAbove);
                if (Helpers.isBlock(stateAbove, BlockTags.ICE)) {
                    return;
                }
                if (Helpers.isBlock(stateAbove, TFCBlocks.ICICLE.get())) {
                    level.setBlock(posAbove, stateAbove.setValue(ThinSpikeBlock.TIP, false), Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
                level.setBlock(iciclePos, TFCBlocks.ICICLE.get().defaultBlockState().setValue(ThinSpikeBlock.TIP, true), Block.UPDATE_ALL);
            }
        }
    }

    private static boolean placeSnowOrSnowPile(ServerLevel level, BlockPos initialPos) {
        // First, try and find an optimal position, to smoothen out snow accumulation
        // This will only move to the side, if we're currently at a snow location
        final BlockPos pos = findOptimalSnowLocation(level, initialPos, level.getBlockState(initialPos));
        final BlockState state = level.getBlockState(pos);

        // If we didn't move to the side, then we still need to pass a can see sky check
        // If we did, we might've moved under an overhang from a previously valid snow location
        if (initialPos.equals(pos) && !level.canSeeSky(pos)) {
            return false;
        }
        return placeSnowOrSnowPileAt(level, pos, state);
    }

    private static boolean placeSnowOrSnowPileAt(ServerLevel level, BlockPos pos, BlockState state) {
        // Then, handle possibilities
        if (SnowPileBlock.canPlaceSnowPile(level, pos, state)) {
            SnowPileBlock.placeSnowPile(level, pos, state, false);
            return true;
        } else if (state.getBlock() instanceof KrummholzBlock) {
            KrummholzBlock.updateFreezingInColumn(level, pos, true);
        } else if (state.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, pos)) {
            // Vanilla snow placement (single layers)
            level.setBlock(pos, Blocks.SNOW.defaultBlockState(), Block.UPDATE_ALL);
            return true;
        } else {
            // Fills cauldrons with snow
            state.getBlock().handlePrecipitation(state, level, pos, Biome.Precipitation.SNOW);
        }
        return false;
    }

    private static BlockPos findOptimalSnowLocation(ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos targetPos = null;
        int found = 0;
        if (isSnow(state)) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                final BlockPos adjPos = pos.relative(direction);
                final BlockState adjState = level.getBlockState(adjPos);
                if ((adjState.isAir() || Helpers.isBlock(adjState.getBlock(), TFCTags.Blocks.CAN_BE_SNOW_PILED))
                        && Blocks.SNOW.defaultBlockState().canSurvive(level, adjPos)) {
                    found++;
                    if (targetPos == null || level.random.nextInt(found) == 0) {
                        targetPos = adjPos;
                    }
                }
            }
            if (targetPos != null) {
                return targetPos;
            }
        }
        return pos;
    }

    @Nullable
    private static BlockPos findIcicleLocation(ServerLevel level, BlockPos pos) {
        final Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(level.random);
        BlockPos adjacentPos = pos.relative(side);
        final int adjacentHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING, adjacentPos.getX(), adjacentPos.getZ());
        BlockPos foundPos = null;

        int found = 0;
        for (int y = 0; y < adjacentHeight; y++) {
            final BlockState stateAt = level.getBlockState(adjacentPos);
            final BlockPos posAbove = adjacentPos.above();
            final BlockState stateAbove = level.getBlockState(posAbove);
            if (stateAt.isAir() && (stateAbove.getBlock() == TFCBlocks.ICICLE.get() || stateAbove.isFaceSturdy(level, posAbove, Direction.DOWN))) {
                found++;
                if (foundPos == null || level.random.nextInt(found) == 0) {
                    foundPos = adjacentPos;
                }
            }
            adjacentPos = posAbove;
        }

        if (foundPos == null) {
            return null;
        }

        // Ensure that icicles are always below a maximum length, which is determined by location (so that each not every location gets the same length).
        // This is technically a weird heuristic (icicle -> block -> icicle) might mess it up, but not in any meaningful way that is player visible
        final int maxLength = 1 + (Helpers.hash(7189237951231L, pos.getX(), 0, pos.getZ()) % 3);
        if (level.getBlockState(foundPos.above(maxLength)).getBlock() == TFCBlocks.ICICLE.get()) {
            return null;
        }

        return foundPos;
    }

    private static BlockPos getSequentialSurfacePos(ServerLevel level, ChunkPos chunkPos, ChunkAccess access, ChunkData data, boolean updateChunk) {
        final BlockPos pos = ((IChunkData) data).tfg$getNextSnowPos(chunkPos);
        if (updateChunk) {
            ((IChunkData) data).tfg$iterateSnowPos(access);
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos);
    }
}
