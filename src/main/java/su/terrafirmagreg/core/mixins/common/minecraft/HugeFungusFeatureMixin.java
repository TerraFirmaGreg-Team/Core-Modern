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
        original.call(instance, level, random, config, pos, decorChance, hatChance, vineChance);

        // Only for edges (0.98F) and corners (0.7F), skip interior (0.2F)
        if (hatChance < 0.5F)
            return;

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
}
