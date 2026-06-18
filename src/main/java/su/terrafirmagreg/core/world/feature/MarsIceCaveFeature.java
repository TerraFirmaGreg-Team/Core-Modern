package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Mars;

public class MarsIceCaveFeature extends Feature<NoneFeatureConfiguration> {

    public MarsIceCaveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final RandomSource random = context.random();

        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = new ChunkPos(pos);
        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final ChunkData chunkData = provider.get(level, chunkPos);

        for (int i = 0; i < 72; i++) {
            mutablePos.setWithOffset(pos, random.nextInt(15) - random.nextInt(15), -3, random.nextInt(15) - random.nextInt(15));
            float maxTemperature = chunkData.getAverageTemp(mutablePos);
            if (maxTemperature > -85) {
                return false;
            }

            if (random.nextFloat() < 0.1F && FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos))) {
                for (int j = 0; j < 10; j++) {
                    mutablePos.move(0, -1, 0);
                    if (!FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos))) {
                        break;
                    }
                }
                BlockState finalState = level.getBlockState(mutablePos);
                mutablePos.move(Direction.UP);
                if (Helpers.isBlock(finalState, Tags.Blocks.STONE)) {
                    placeDisc(level, mutablePos, random);
                } else if (Helpers.isBlock(finalState, BlockTags.ICE) && random.nextFloat() < 0.1F) {
                    placeDisc(level, mutablePos, random);
                }
            } else if (mutablePos.getY() < 105 && random.nextFloat() < 0.1F) //occluding thin areas
            {
                for (int j = 0; j < 8; j++) {
                    mutablePos.move(Direction.UP, j);
                    if (!FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos))) {
                        break;
                    }
                }

                if (!FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos))) {
                    mutablePos.move(Direction.DOWN, 3);
                    if (FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos)))
                        placeSphere(level, mutablePos, random);
                }
            }

            if (random.nextFloat() < 0.01F) //extra springs
            {
                mutablePos.setY(4 + random.nextInt(7));
                if (FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos))) {
                    mutablePos.move(Direction.UP);
                    if (Helpers.isBlock(level.getBlockState(mutablePos), Tags.Blocks.STONE)) {
                        level.setBlock(mutablePos, TFGFluids.MARS_WATER.createSourceBlock(), Block.UPDATE_ALL);
                        level.scheduleTick(mutablePos, TFGFluids.MARS_WATER.getSource(), 0);
                    }
                }
            }

            if (random.nextFloat() < 0.05F) //large spikes
            {
                if (mutablePos.getY() < 105 && Helpers.isBlock(level.getBlockState(mutablePos), Tags.Blocks.STONE)) {
                    mutablePos.move(Direction.DOWN);
                    if (FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos))) {
                        placeSpike(level, mutablePos, random, Direction.DOWN);
                    } else {
                        mutablePos.move(Direction.UP, 2);
                        if (FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos)))
                            placeSpike(level, mutablePos, random, Direction.UP);
                    }
                }
            }
        }
        return true;
    }

    private BlockState getState(RandomSource rand) {
        return rand.nextFloat() < 0.4F ? TFGBlocks_Mars.MARS_ICE.get().defaultBlockState() : TFGBlocks.DRY_ICE.get().defaultBlockState();
    }

    private void placeSphere(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos, RandomSource rand) {
        float radius = 1.0F + rand.nextFloat() * rand.nextFloat() * 3.0F;
        float radiusSquared = radius * radius;
        int size = Mth.ceil(radius);
        BlockPos pos = mutablePos.immutable();
        BlockState ice = TFGBlocks_Mars.MARS_ICE.get().defaultBlockState();

        for (int x = -size; x <= size; ++x) {
            for (int y = -size; y <= size; ++y) {
                for (int z = -size; z <= size; ++z) {
                    if ((float) (x * x + y * y + z * z) <= radiusSquared) {
                        mutablePos.set(pos).move(x, y, z);
                        if (world.isEmptyBlock(mutablePos)) {
                            world.setBlock(mutablePos, ice, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
        }
    }

    private void placeSpike(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos, RandomSource rand, Direction direction) {
        final BlockState state = getState(rand);
        final BlockPos pos = mutablePos.immutable();
        int height = 6 + rand.nextInt(11);
        int radius = 2 + rand.nextInt(1);
        int maxHeightReached = 0;
        for (int y = -3; y <= height; y++) {
            float radiusSquared = radius * (1 - 1.5f * Math.abs(y) / height);
            if (radiusSquared < 0) {
                continue;
            }
            radiusSquared *= radiusSquared;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    mutablePos.set(pos).move(x, y * direction.getStepY(), z);
                    float actualRadius = ((x * x) + (z * z)) / radiusSquared;
                    if (actualRadius < 0.7) {
                        // Fill in actual blocks
                        setBlock(world, mutablePos, state);
                        if (x == 0 && z == 0) {
                            maxHeightReached = y;
                        }
                    } else if (actualRadius < 0.85 && rand.nextBoolean()) {
                        // Only fill in if continuing downwards
                        if (world.getBlockState(mutablePos.offset(0, -direction.getStepY(), 0)) == state) {
                            setBlock(world, mutablePos, state);
                        }
                    }
                }
            }
        }
        mutablePos.set(pos).move(direction, maxHeightReached - 1);
    }

    private void placeDisc(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos, RandomSource random) {
        final float radius = 1 + random.nextFloat() * random.nextFloat() * 3.5f;
        final float radiusSquared = radius * radius;
        final int size = Mth.ceil(radius);
        final BlockPos pos = mutablePos.immutable();
        final BlockState ice = getState(random);

        for (Direction d : Direction.Plane.HORIZONTAL) {
            mutablePos.move(d);
            mutablePos.move(Direction.DOWN, 2);
            if (world.isEmptyBlock(mutablePos))
                return;
            mutablePos.move(d.getOpposite());
            mutablePos.move(Direction.UP, 2);
        }
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (x * x + z * z <= radiusSquared) {
                    mutablePos.set(pos).move(x, -1, z);
                    if (!world.isEmptyBlock(mutablePos))
                        mutablePos.move(Direction.UP);
                    setBlock(world, mutablePos, ice);
                }
            }
        }
    }
}
