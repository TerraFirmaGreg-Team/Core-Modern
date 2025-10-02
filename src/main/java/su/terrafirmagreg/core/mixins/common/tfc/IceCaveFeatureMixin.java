package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.feature.cave.IceCaveFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGFluids;

@Mixin(value = IceCaveFeature.class)
public class IceCaveFeatureMixin {

    /**
     * @author Pyritie
     * @reason The original relies on OverworldClimateModel which no other dimension has access to,
     * also there's too many places to try and mixin individually, so this gets a whole overwrite
     *
     * Fun fact: TFC doesn't use this feature so we can do what we want here!
     */
    @Overwrite
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
                        level.setBlock(mutablePos, TFGFluids.MARS_WATER.createSourceBlock(), 3);
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

    @Inject(method = "getState", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$getState(RandomSource rand, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(rand.nextFloat() < 0.4F ? TFGBlocks.MARS_ICE.get().defaultBlockState() : TFGBlocks.DRY_ICE.get().defaultBlockState());
    }

    /**
     * @author Pyritie
     * @reason Replaces ice block
     */
    @Overwrite(remap = false)
    private void placeSphere(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos, RandomSource rand) {
        float radius = 1.0F + rand.nextFloat() * rand.nextFloat() * 3.0F;
        float radiusSquared = radius * radius;
        int size = Mth.ceil(radius);
        BlockPos pos = mutablePos.immutable();
        BlockState ice = TFGBlocks.MARS_ICE.get().defaultBlockState();

        for (int x = -size; x <= size; ++x) {
            for (int y = -size; y <= size; ++y) {
                for (int z = -size; z <= size; ++z) {
                    if ((float) (x * x + y * y + z * z) <= radiusSquared) {
                        mutablePos.set(pos).move(x, y, z);
                        if (world.isEmptyBlock(mutablePos)) {
                            world.setBlock(mutablePos, ice, 3);
                        }
                    }
                }
            }
        }
    }

    @Shadow(remap = false)
    private void placeDisc(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos, RandomSource random) {
    }

    @Shadow(remap = false)
    private void placeSpike(WorldGenLevel world, BlockPos.MutableBlockPos mutablePos, RandomSource rand, Direction direction) {
    }
}
