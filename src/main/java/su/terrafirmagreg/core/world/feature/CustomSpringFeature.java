package su.terrafirmagreg.core.world.feature;

import java.util.*;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.feature.FissureFeature;
import net.dries007.tfc.world.feature.HotSpringConfig;
import net.dries007.tfc.world.noise.Metaballs2D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.TFGCore;

// A copy of TFC's HotSpringFeature except the whole thing is made of whatever is selected
// as the wall block, instead of only the fissures.

public class CustomSpringFeature extends Feature<HotSpringConfig> {

    public CustomSpringFeature(Codec<HotSpringConfig> codec) {
        super(codec);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean place(FeaturePlaceContext<HotSpringConfig> context) {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final RandomSource random = context.random();
        final HotSpringConfig config = context.config();

        final Metaballs2D noise = Metaballs2D.simple(Helpers.fork(random), config.radius());
        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final Fluid fluid = config.fluidState().getFluidState().getType();

        if (config.wallState().isEmpty()) {
            TFGCore.LOGGER.error("`wall_block` is required when using the CustomSpringFeature!");
            return false;
        }
        final BlockState wallBlock = config.wallState().get();

        final boolean useFilledEmptyCheck = config.fluidState().isAir();
        final Set<BlockPos> filledEmptyPositions = new HashSet<>(); // Only used if the fill state is air - prevents positions from being 'filled' with air and affecting the shape of the spring
        final List<BlockPos> fissureStartPositions = new ArrayList<>();

        for (int x = -config.radius(); x <= config.radius(); x++) {
            for (int z = -config.radius(); z <= config.radius(); z++) {
                final int localX = pos.getX() + x;
                final int localZ = pos.getZ() + z;
                final int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, localX, localZ) - 1;

                // Disallow underwater locations
                if ((!config.allowUnderwater() && y <= TFCChunkGenerator.SEA_LEVEL_Y) || !noise.inside(x, z)) {
                    continue;
                }

                mutablePos.set(localX, y + 1, localZ);
                final BlockState stateAbove = level.getBlockState(mutablePos);
                if (!isEmptyBlock(config, stateAbove)) {
                    if (stateAbove.canBeReplaced()) {
                        setBlock(level, mutablePos, stateAbove.getFluidState().createLegacyBlock());
                        mutablePos.move(0, 1, 0);
                        level.scheduleTick(mutablePos, level.getBlockState(mutablePos).getBlock(), 1);
                    } else {
                        continue; // Solid, non-replaceable block above. So don't replace with hot spring blocks
                    }
                }

                boolean edge = false;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    mutablePos.set(localX, y, localZ).move(direction);
                    final BlockState stateAt = level.getBlockState(mutablePos);
                    if (isEmptyBlock(config, stateAt) && (!useFilledEmptyCheck || !filledEmptyPositions.contains(mutablePos))) {
                        edge = true;
                        break;
                    }
                }

                // surface depth is deeper near the center of the hot spring
                // Range: [0.3, 1.0]
                final float centerFactor = 1 - 0.7f * Mth.clamp(2 * (x * x + z * z) / (float) (config.radius() * config.radius()), 0, 1);
                final int surfaceDepth = (int) ((8 + random.nextInt(3)) * centerFactor);
                if (edge) {
                    final int startY = random.nextInt(12) == 0 ? -1 : 0; // Creates holes which allow the water to flow, rarely
                    if (startY == -1) {
                        mutablePos.set(localX, y, localZ);
                        setBlock(level, mutablePos, Blocks.AIR.defaultBlockState());
                    }
                } else {
                    mutablePos.set(localX, y, localZ);
                    final BlockPos posAt = mutablePos.immutable();
                    fissureStartPositions.add(posAt);

                    setBlock(level, mutablePos, config.fluidState());
                    if (fluid != Fluids.EMPTY) {
                        level.scheduleTick(mutablePos, fluid, 0);
                    }
                    if (useFilledEmptyCheck) {
                        filledEmptyPositions.add(posAt);
                    }

                    mutablePos.set(localX, y - 1, localZ);
                    setFissureBaseBlock(config, level, mutablePos, wallBlock);
                }

                for (int dy = edge ? 0 : -2; dy >= -surfaceDepth; dy--) {
                    mutablePos.set(localX, y + dy, localZ);
                    if (!setFissureBaseBlock(config, level, mutablePos, wallBlock)) {
                        break;
                    }
                }
            }
        }

        if (fissureStartPositions.isEmpty()) {
            return false;
        }

        final int fissureStarts = 1 + random.nextInt(1 + random.nextInt(Mth.clamp(fissureStartPositions.size(), 1, 7)));
        final List<BlockPos> selected = Helpers.uniqueRandomSample(fissureStartPositions, fissureStarts, random);
        for (BlockPos start : selected) {
            FissureFeature.placeFissure(level, start, pos, mutablePos, random, config.fluidState(), wallBlock, 10, 22, 6, 16, 12, config.decoration().orElse(null));
        }

        return true;
    }

    private boolean setFissureBaseBlock(HotSpringConfig config, WorldGenLevel level, BlockPos pos, BlockState state) {
        final BlockState stateAt = level.getBlockState(pos);
        if (isEmptyBlock(config, stateAt)) {
            return false;
        }
        level.setBlock(pos, state, 2);
        return true;
    }

    private static boolean isEmptyBlock(HotSpringConfig config, BlockState state) {
        return config.allowUnderwater() ? FluidHelpers.isAirOrEmptyFluid(state) : state.isAir();
    }
}
