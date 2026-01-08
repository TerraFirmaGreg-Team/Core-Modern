package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;

@Mixin(HugeFungusFeature.class)
public class HugeFungusFeatureMixin {
    // Make sure that changes in the cap radius still connect to blocks above
    //  Done by checking if the block one lower and outward is filled
    //  If so, then the block below gets filled too
    @WrapOperation(method = "placeHat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/HugeFungusFeature;placeHatBlock(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/HugeFungusConfiguration;Lnet/minecraft/core/BlockPos$MutableBlockPos;FFF)V"))
    private void fillStepGaps(
            HugeFungusFeature instance,
            LevelAccessor level,
            RandomSource random,
            HugeFungusConfiguration config,
            BlockPos.MutableBlockPos pos,
            float decorChance,
            float hatChance,
            float vineChance,
            Operation<Void> original,
            @Local(ordinal = 5) int x,
            @Local(ordinal = 6) int z,
            @Local(ordinal = 4) int radius) {

        if (hatChance == 0.2F) { // Interior blocks
            original.call(instance, level, random, config, pos, 0.4F, hatChance, vineChance);
            return;
        } else {
            original.call(instance, level, random, config, pos, decorChance, hatChance, vineChance);
        }

        int outX = x == radius ? 1 : (x == -radius ? -1 : 0);
        int outZ = z == radius ? 1 : (z == -radius ? -1 : 0);

        BlockPos.MutableBlockPos below = pos.below().mutable();
        BlockPos outwardBelow = below.offset(outX, 0, outZ);

        if (level.getBlockState(outwardBelow).is(config.hatState.getBlock())) {
            ((HugeFungusFeatureInvoker) instance).callPlaceHatBlock(level, random, config, below, 0.0F, 1.0F, 0.0F);
        }
    }

    // Make bells taller on average
    //  Changes arg of nextInt in `int bellHeight = Math.min(pRandom.nextInt(1 + stemHeight / 3) + 5, stemHeight);`
    @ModifyArg(method = "placeHat", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0))
    private int biggerBell(int original, @Local(argsOnly = true) int stemHeight) {
        return 1 + (stemHeight * 2 / 3);
    }

    // De-obfuscated method for reference:

    //    private void placeHat(WorldGenLevel pLevel, RandomSource pRandom, HugeFungusConfiguration pConfig, BlockPos rootPos, int stemHeight, boolean pHuge) {
    //        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
    //        boolean isNetherWart = pConfig.hatState.is(Blocks.NETHER_WART_BLOCK);
    //        int bellHeight = Math.min(pRandom.nextInt(1 + stemHeight / 3) + 5, stemHeight);
    //        int bellBottom = stemHeight - bellHeight;
    //
    //        for (int y = bellBottom; y <= stemHeight; ++y) {
    //            int radius = y < stemHeight - pRandom.nextInt(3) ? 2 : 1;  // radius: 2 for most layers, 1 near top
    //            if (bellHeight > 8 && y < bellBottom + 4) {
    //                radius = 3;  // radius 3 for bottom layers of tall mushrooms
    //            }
    //            if (pHuge) {
    //                ++radius;  // extra radius for huge variant
    //            }
    //
    //            for (int x = -radius; x <= radius; ++x) {
    //                for (int z = -radius; z <= radius; ++z) {
    //                    boolean isOnEdgeX = x == -radius || x == radius;
    //                    boolean isOnEdgeZ = z == -radius || z == radius;
    //                    boolean isInterior = !isOnEdgeX && !isOnEdgeZ && y != stemHeight;
    //                    boolean isCorner = isOnEdgeX && isOnEdgeZ;
    //                    boolean isBellBottom3 = y < bellBottom + 3;
    //                    blockpos$mutableblockpos.setWithOffset(rootPos, x, y, z);
    //                    if (isReplaceable(pLevel, blockpos$mutableblockpos, pConfig, false)) {
    //                        if (pConfig.planted && !pLevel.getBlockState(blockpos$mutableblockpos.below()).isAir()) {
    //                            pLevel.destroyBlock(blockpos$mutableblockpos, true);
    //                        }
    //
    //                        if (isBellBottom3) {
    //                            if (!isInterior) {
    //                                this.placeHatDropBlock(pLevel, pRandom, blockpos$mutableblockpos, pConfig.hatState, isNetherWart);
    //                            }
    //                        } else if (isInterior) {
    //                            this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutableblockpos, 0.1F, 0.2F, isNetherWart ? 0.1F : 0.0F);
    //                        } else if (isCorner) {
    //                            this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutableblockpos, 0.01F, 0.7F, isNetherWart ? 0.083F : 0.0F);
    //                        } else {
    //                            this.placeHatBlock(pLevel, pRandom, pConfig, blockpos$mutableblockpos, 5.0E-4F, 0.98F, isNetherWart ? 0.07F : 0.0F);
    //                        }
    //                    }
    //                }
    //            }
    //        }
    //
    //    }
}
