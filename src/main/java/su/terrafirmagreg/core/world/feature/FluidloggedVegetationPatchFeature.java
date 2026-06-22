package su.terrafirmagreg.core.world.feature;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class FluidloggedVegetationPatchFeature extends Feature<FluidloggedVegetationPatchConfig> {

    public FluidloggedVegetationPatchFeature(Codec<FluidloggedVegetationPatchConfig> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<FluidloggedVegetationPatchConfig> context) {
        WorldGenLevel worldgenlevel = context.level();
        FluidloggedVegetationPatchConfig config = context.config();
        RandomSource randomsource = context.random();
        BlockPos blockpos = context.origin();
        Predicate<BlockState> predicate = (bs) -> bs.is(config.replaceable());
        int i = config.xzRadius().sample(randomsource) + 1;
        int j = config.xzRadius().sample(randomsource) + 1;
        Set<BlockPos> set = this.placeFluidPatch(worldgenlevel, config, randomsource, blockpos, predicate, i, j);
        this.distributeVegetation(context, worldgenlevel, config, randomsource, set, i, j);
        return !set.isEmpty();
    }

    protected Set<BlockPos> placeGroundPatch(WorldGenLevel level, FluidloggedVegetationPatchConfig config, RandomSource random, BlockPos pos, Predicate<BlockState> state, int xRadius, int zRadius) {
        BlockPos.MutableBlockPos mutableBlockPos = pos.mutable();
        BlockPos.MutableBlockPos mutableFloorPos = mutableBlockPos.mutable();
        Direction direction = config.surface().getDirection();
        Direction direction1 = direction.getOpposite();
        Set<BlockPos> set = new HashSet<>();

        for (int i = -xRadius; i <= xRadius; ++i) {
            boolean flag = i == -xRadius || i == xRadius;

            for (int j = -zRadius; j <= zRadius; ++j) {
                boolean flag1 = j == -zRadius || j == zRadius;
                boolean flag2 = flag || flag1;
                boolean flag3 = flag && flag1;
                boolean flag4 = flag2 && !flag3;
                if (!flag3 && (!flag4 || config.extraEdgeColumnChance() != 0.0F && !(random.nextFloat() > config.extraEdgeColumnChance()))) {
                    mutableBlockPos.setWithOffset(pos, i, 0, j);

                    for (int k = 0; level.isStateAtPosition(mutableBlockPos, BlockBehaviour.BlockStateBase::isAir) && k < config.verticalRange(); ++k) {
                        mutableBlockPos.move(direction);
                    }

                    for (int i1 = 0; level.isStateAtPosition(mutableBlockPos, (bs) -> !bs.isAir()) && i1 < config.verticalRange(); ++i1) {
                        mutableBlockPos.move(direction1);
                    }

                    mutableFloorPos.setWithOffset(mutableBlockPos, config.surface().getDirection());
                    BlockState blockstate = level.getBlockState(mutableFloorPos);
                    if (level.isEmptyBlock(mutableBlockPos) && blockstate.isFaceSturdy(level, mutableFloorPos, config.surface().getDirection().getOpposite())) {
                        int l = config.depth().sample(random) + (config.extraBottomBlockChance() > 0.0F && random.nextFloat() < config.extraBottomBlockChance() ? 1 : 0);
                        BlockPos blockpos = mutableFloorPos.immutable();
                        boolean flag5 = this.placeGround(level, config, state, random, mutableFloorPos, l);
                        if (flag5) {
                            set.add(blockpos);
                        }
                    }
                }
            }
        }

        return set;
    }

    protected Set<BlockPos> placeFluidPatch(
            WorldGenLevel level, FluidloggedVegetationPatchConfig config, RandomSource random, BlockPos pos, Predicate<BlockState> state, int xRadius, int zRadius) {
        var groundPositions = placeGroundPatch(level, config, random, pos, state, xRadius, zRadius);
        var fluidPositions = new HashSet<BlockPos>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (BlockPos groundPos : groundPositions) {
            if (!isExposed(level, groundPositions, groundPos, mutablePos)) {
                fluidPositions.add(groundPos);
            }
        }

        for (BlockPos fluidPos : fluidPositions) {
            level.setBlock(fluidPos, config.fluidState(), Block.UPDATE_CLIENTS);
        }
        return fluidPositions;
    }

    private static boolean isExposed(WorldGenLevel level, Set<BlockPos> positions, BlockPos pos, BlockPos.MutableBlockPos mutablePos) {
        return isExposedDirection(level, pos, mutablePos, Direction.NORTH)
                || isExposedDirection(level, pos, mutablePos, Direction.EAST)
                || isExposedDirection(level, pos, mutablePos, Direction.SOUTH)
                || isExposedDirection(level, pos, mutablePos, Direction.WEST)
                || isExposedDirection(level, pos, mutablePos, Direction.DOWN);
    }

    private static boolean isExposedDirection(WorldGenLevel level, BlockPos pos, BlockPos.MutableBlockPos mutablePos, Direction direction) {
        mutablePos.setWithOffset(pos, direction);
        return !level.getBlockState(mutablePos).isFaceSturdy(level, mutablePos, direction.getOpposite());
    }

    protected void distributeVegetation(FeaturePlaceContext<FluidloggedVegetationPatchConfig> context, WorldGenLevel level, FluidloggedVegetationPatchConfig config, RandomSource random,
            Set<BlockPos> possiblePositions, int xRadius, int zRadius) {
        for (BlockPos blockpos : possiblePositions) {
            if (config.vegetationChance() > 0.0F && random.nextFloat() < config.vegetationChance()) {
                this.placeVegetation(level, config, context.chunkGenerator(), random, blockpos);
            }
        }

    }

    protected void placeVegetation(WorldGenLevel level, FluidloggedVegetationPatchConfig config, ChunkGenerator chunkGenerator, RandomSource random, BlockPos pos) {
        boolean isPlaced = (config.vegetationFeature().value()).place(level, chunkGenerator, random, pos.relative(config.surface().getDirection().getOpposite()));

        if (isPlaced) {
            BlockState bs = level.getBlockState(pos);
            if (bs.hasProperty(BlockStateProperties.WATERLOGGED) && !bs.getValue(BlockStateProperties.WATERLOGGED)) {
                level.setBlock(pos, bs.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), 2);
            }

            if (bs.getBlock() instanceof IFluidLoggable fluidLoggable) {
                fluidLoggable.placeLiquid(level, pos, bs, config.fluidState().getFluidState());
            }
        }
    }

    protected boolean placeGround(WorldGenLevel level, FluidloggedVegetationPatchConfig config, Predicate<BlockState> replaceableblocks, RandomSource random, BlockPos.MutableBlockPos mutablePos,
            int maxDistance) {
        for (int i = 0; i < maxDistance; ++i) {
            BlockState groundState = config.groundState().getState(random, mutablePos);
            BlockState currentState = level.getBlockState(mutablePos);
            if (!groundState.is(currentState.getBlock())) {
                if (!replaceableblocks.test(currentState)) {
                    return i != 0;
                }

                level.setBlock(mutablePos, groundState, Block.UPDATE_CLIENTS);
                mutablePos.move(config.surface().getDirection());
            }
        }

        return true;
    }
}
