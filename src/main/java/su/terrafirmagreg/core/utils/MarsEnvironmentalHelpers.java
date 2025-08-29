/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.utils;

import earth.terrarium.adastra.api.planets.Planet;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.tracker.WorldTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.blocks.LayerBlock;
import su.terrafirmagreg.core.config.TFGConfig;

public final class MarsEnvironmentalHelpers {

    public static final float DUST_SETTLE_SPEED = 0.5f; // sand piles will build at this speed or lower
    public static final float DUST_LOOSEN_SPEED = 2.0f; // sand piles will erode at this speed or higher
    public static final int DUST_SETTLE_RANDOM_TICK_CHANCE = 50;
    public static final int DUST_LOOSEN_RANDOM_TICK_CHANCE = 50;

    // fuck it we're doing sand rain overrides
    public static boolean isAtmosphereDusty(Level level, BlockPos pos)
    {
        return level.isRaining() && WorldTracker.get(level).isRaining(level, pos);
//        return true;
    }

    public static boolean isSandPile(BlockState state)
    {
        return Helpers.isBlock(state, Blocks.SAND);
    }

    // TODO: get working
    public static void tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler)
    {
        if (!level.dimension().equals(Planet.MARS)) return;

        final ChunkPos chunkPos = chunk.getPos();
        final BlockPos lcgPos = level.getBlockRandomPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), 15);
        final BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, lcgPos);
        final float temperature = Climate.getTemperature(level, surfacePos);
        final Vec2 wind = Climate.getWindVector(level, surfacePos);

        profiler.push("tfgSand");
        doSandPiles(level, surfacePos, temperature, wind);
        profiler.pop();
    }

    private static void doSandPiles(Level level, BlockPos surfacePos, float temperature, Vec2 wind)
    {
        // Snow only accumulates during rain
        final RandomSource random = level.random;
        final int expectedLayers = (int) getExpectedSandPileLayerHeight(temperature);
        if (wind.length() <= DUST_SETTLE_SPEED /* && isAtmosphereDusty(level, surfacePos)*/)
        {
            if (random.nextInt(TFGConfig.SERVER.sandAccumulateChance.get()) == 0)
            {
                // Handle smoother snow placement: if there's an adjacent position with less snow, switch to that position instead
                // Additionally, handle up to two block tall plants if they can be piled
                // This means we need to check three levels deep
                if (!placeSandPile(level, surfacePos, random, expectedLayers))
                {
                    if (!placeSandPile(level, surfacePos.below(), random, expectedLayers))
                    {
                        placeSandPile(level, surfacePos.below(2), random, expectedLayers);
                    }
                }
            }
        }
        else
        {
            if (random.nextInt(TFCConfig.SERVER.snowMeltChance.get()) == 0)
            {
                removeSandPileAt(level, surfacePos, temperature, expectedLayers);
                if (random.nextFloat() < 0.2f)
                {
                    removeSandPileAt(level, surfacePos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random)), temperature, expectedLayers);
                }
            }
        }
    }

    // TODO: after adding sand piles
    private static void removeSandPileAt(Level level, BlockPos surfacePos, float temperature, int expectedLayers) {
//        // Snow melting - both snow and snow piles
//        final BlockState state = level.getBlockState(surfacePos);
//        if (isSnow(state))
//        {
//            // When melting snow, we melt layers at +2 from expected, while the temperature is still below zero
//            // This slowly reduces massive excess amounts of snow, if they're present, but doesn't actually start melting snow a lot when we're still below freezing.
//            SnowPileBlock.removePileOrSnow(level, surfacePos, state, temperature > 0f ? expectedLayers : expectedLayers + 2);
//        }
    }

    private static boolean placeSandPile(Level level, BlockPos initialPos, RandomSource random, int expectedLayers) {
        if (expectedLayers < 1)
        {
            // Don't place snow if we're < 1 expected layers
            return false;
        }
//
        // First, try and find an optimal position, to smoothen out snow accumulation
        // This will only move to the side, if we're currently at a snow location
        final BlockPos pos = findOptimalSandPileLocation(level, initialPos, level.getBlockState(initialPos), random);
        final BlockState state = level.getBlockState(pos);

        // If we didn't move to the side, then we still need to pass a can see sky check
        // If we did, we might've moved under an overhang from a previously valid snow location
        if (initialPos.equals(pos) && !level.canSeeSky(pos))
        {
            return false;
        }
        return placeSandPileAt(level, pos, state, random, expectedLayers);
    }

    private static boolean placeSandPileAt(LevelAccessor level, BlockPos pos, BlockState state, RandomSource random, int expectedLayers)
    {
        // Then, handle possibilities
        if (isSandPile(state) && state.getValue(LayerBlock.LAYERS) < 7)
        {
            // Snow and snow layers can accumulate snow
            // The chance that this works is reduced the higher the pile is
            final int currentLayers = state.getValue(LayerBlock.LAYERS);
            final BlockState newState = state.setValue(LayerBlock.LAYERS, currentLayers + 1);
            if (newState.canSurvive(level, pos) && random.nextInt(1 + 3 * currentLayers) == 0 && expectedLayers > currentLayers)
            {
                level.setBlock(pos, newState, 3);
            }
            return true;
        }
//        else if (SnowPileBlock.canPlaceSnowPile(level, pos, state))
//        {
//            SnowPileBlock.placeSnowPile(level, pos, state, false);
//            return true;
//        }
        else if (state.isAir() && TFGBlocks.MARS_SAND_PILE.get().defaultBlockState().canSurvive(level, pos))
        {
            // Vanilla snow placement (single layers)
            level.setBlock(pos, TFGBlocks.MARS_SAND_PILE.get().defaultBlockState(), 2 | 1);
            return true;
        }
//        else if (level instanceof Level fullLevel)
//        {
//            // Fills cauldrons with snow
//            state.getBlock().handlePrecipitation(state, fullLevel, pos, Biome.Precipitation.SNOW);
//        }
        return false;
    }

    /**
     * Based on the wind strength provided, returns an approximate estimate for how high sand should be layering.
     */
    public static float getExpectedSandPileLayerHeight(float windStrength) {
        // nearly zero wind = 7 layers
        // moderately windy = 2 layers
        // extremely windy = 0 layers
        // let's try a cubic easing function where f(0) = 7
        return (float) (3.0 / Math.pow(windStrength + 0.625, 2));
    }

    private static BlockPos findOptimalSandPileLocation(LevelAccessor level, BlockPos pos, BlockState state, RandomSource random)
    {
        BlockPos targetPos = null;
        int found = 0;
        if (isSandPile(state))
        {
            for (Direction direction : Direction.Plane.HORIZONTAL)
            {
                final BlockPos adjPos = pos.relative(direction);
                final BlockState adjState = level.getBlockState(adjPos);
                if ((isSandPile(adjState) && adjState.getValue(LayerBlock.LAYERS) < state.getValue(LayerBlock.LAYERS)) // Adjacent snow that's lower than this one
                        || ((adjState.isAir() || Helpers.isBlock(adjState.getBlock(), TFGTags.Blocks.CanBeSandPiled)) && TFGBlocks.MARS_SAND_PILE.get().defaultBlockState().canSurvive(level, adjPos))) // Or, empty space that could support snow
                {
                    found++;
                    if (targetPos == null || random.nextInt(found) == 0)
                    {
                        targetPos = adjPos;
                    }
                }
            }
            if (targetPos != null)
            {
                return targetPos;
            }
        }
        return pos;
    }
}
