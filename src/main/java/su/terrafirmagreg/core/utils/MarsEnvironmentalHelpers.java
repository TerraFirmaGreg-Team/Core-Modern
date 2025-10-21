/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.utils;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec2;

import earth.terrarium.adastra.api.planets.Planet;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.AbstractLayerBlock;
import su.terrafirmagreg.core.common.data.blocks.SandPileBlock;
import su.terrafirmagreg.core.config.TFGConfig;

// Most of this code is from TFC's EnvironmentHelpers class for its snow stuff

public final class MarsEnvironmentalHelpers {

    public static final float DUST_SETTLE_SPEED = 0.4f; // sand piles will build at this speed or lower
    public static final float DUST_LOOSEN_SPEED = 0.2f; // sand piles will erode at this speed or higher

    public static Vec2 wind_override = Vec2.ZERO;
    public static float dustiness_override = 0.0f;

    public static boolean isSand(BlockState state) {
        return state.getBlock() instanceof SandPileBlock;
    }

    public static void tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler) {
        if (!level.dimension().equals(Planet.MARS))
            return;

        final ChunkPos chunkPos = chunk.getPos();
        final BlockPos lcgPos = level.getBlockRandomPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ(), 15);
        final BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, lcgPos);
        final float temperature = Climate.getTemperature(level, surfacePos);

        // Skip sand stuff on the poles
        if (temperature < -108f)
            return;

        final Vec2 wind = Climate.getWindVector(level, surfacePos);

        profiler.push("tfgSand");
        doSand(level, surfacePos, wind);
        profiler.pop();
    }

    private static void doSand(Level level, BlockPos surfacePos, Vec2 wind) {
        // Snow only accumulates during rain
        final RandomSource random = level.random;
        final float windLength = wind.length();
        final int expectedLayers = (int) getExpectedSandLayerHeight(windLength);
        if (windLength >= DUST_SETTLE_SPEED) {
            if (random.nextInt(TFGConfig.SERVER.sandAccumulateChance.get()) == 0) {
                // Handle smoother snow placement: if there's an adjacent position with less snow, switch to that position instead
                // Additionally, handle up to two block tall plants if they can be piled
                // This means we need to check three levels deep
                if (!placeSandOrSandPile(level, surfacePos, random, expectedLayers)) {
                    if (!placeSandOrSandPile(level, surfacePos.below(), random, expectedLayers)) {
                        placeSandOrSandPile(level, surfacePos.below(2), random, expectedLayers);
                    }
                }
            }
        } else if (windLength <= DUST_LOOSEN_SPEED) {
            if (random.nextInt(TFGConfig.SERVER.sandDecumulateChance.get()) == 0) {
                removeSandAt(level, surfacePos, expectedLayers);
                if (random.nextFloat() < 0.2f) {
                    removeSandAt(level, surfacePos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random)), expectedLayers);
                }
            }
        }
    }

    private static void removeSandAt(LevelAccessor level, BlockPos surfacePos, int expectedLayers) {
        final BlockState state = level.getBlockState(surfacePos);
        if (isSand(state)) {
            SandPileBlock.removePileOrSand(level, surfacePos, state, expectedLayers);
        }
    }

    private static boolean placeSandOrSandPile(Level level, BlockPos initialPos, RandomSource random, int expectedLayers) {
        if (expectedLayers < 1) {
            // Don't place sand if we're < 1 expected layers
            return false;
        }

        // First, try and find an optimal position, to smoothen out sand accumulation
        // This will only move to the side, if we're currently at a sand location
        final BlockPos pos = findOptimalSandLocation(level, initialPos, level.getBlockState(initialPos), random);
        final BlockState state = level.getBlockState(pos);

        // If we didn't move to the side, then we still need to pass a can see sky check
        // If we did, we might've moved under an overhang from a previously valid sand location
        if (initialPos.equals(pos) && !level.canSeeSky(pos)) {
            return false;
        }
        return placeSandOrSandPileAt(level, pos, state, random, expectedLayers);
    }

    private static boolean placeSandOrSandPileAt(LevelAccessor level, BlockPos pos, BlockState state, RandomSource random, int expectedLayers) {
        // Then, handle possibilities
        if (isSand(state) && state.getValue(AbstractLayerBlock.LAYERS) < 7) {
            // Sand and sand layers can accumulate sand
            // The chance that this works is reduced the higher the pile is
            final int currentLayers = state.getValue(AbstractLayerBlock.LAYERS);
            final BlockState newState = state.setValue(AbstractLayerBlock.LAYERS, currentLayers + 1);
            if (newState.canSurvive(level, pos) && random.nextInt(1 + 3 * currentLayers) == 0 && expectedLayers > currentLayers) {
                level.setBlock(pos, newState, 3);
            }
            return true;
        } else if (SandPileBlock.canPlaceSandPile(level, pos, state)) {
            SandPileBlock.placeSandPile(level, pos, state, false);
            return true;
        } else if (state.isAir() && TFGBlocks.MARS_SAND_LAYER_BLOCK.get().defaultBlockState().canSurvive(level, pos)) {
            // Vanilla sand placement (single layers)
            level.setBlock(pos, PlanetEnvironmentalHelpers.getSandBlockForBiome(level, pos).defaultBlockState(), 3);
            return true;
        }

        return false;
    }

    /**
     * Based on the wind strength provided, returns an approximate estimate for how high sand should be layering.
     */
    public static float getExpectedSandLayerHeight(float windStrength) {
        // nearly zero wind = 7 layers
        // moderately windy = 2 layers
        // extremely windy = 0 layers
        // let's try a cubic easing function where f(0) = 7
        //return (float) (3.0 / Math.pow(windStrength + 0.625, 2));

        if (windStrength <= DUST_LOOSEN_SPEED) {
            return 0;
        } else if (windStrength <= 0.25f) {
            return 1;
        } else if (windStrength <= 0.35f) {
            return 2;
        } else /*if (windStrength >= DUST_SETTLE_SPEED)*/ {
            return 3;
        }
    }

    private static BlockPos findOptimalSandLocation(LevelAccessor level, BlockPos pos, BlockState state, RandomSource random) {
        BlockPos targetPos = null;
        int found = 0;
        if (isSand(state)) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                final BlockPos adjPos = pos.relative(direction);
                final BlockState adjState = level.getBlockState(adjPos);
                if ((isSand(adjState) && adjState.getValue(AbstractLayerBlock.LAYERS) < state.getValue(AbstractLayerBlock.LAYERS)) // Adjacent snow that's lower than this one
                        || ((adjState.isAir() || Helpers.isBlock(adjState.getBlock(), TFCTags.Blocks.CAN_BE_SNOW_PILED))
                                && TFGBlocks.MARS_SAND_LAYER_BLOCK.get().defaultBlockState().canSurvive(level, adjPos))) // Or, empty space that could support snow
                {
                    found++;
                    if (targetPos == null || random.nextInt(found) == 0) {
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

    // Debug Commands
    public static void setDustIntensity(float intensity) {
        dustiness_override = intensity;
    }

    public static void setWind(float strength) {
        wind_override = wind_override.lengthSquared() == 0.0
                ? new Vec2(1, 0).scale(strength)
                : wind_override.normalized().scale(strength);
    }

    public static void setWind(Vec2 vector) {
        wind_override = vector;
    }
}
