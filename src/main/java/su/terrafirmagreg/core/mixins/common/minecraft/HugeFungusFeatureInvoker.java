package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;

@Mixin(HugeFungusFeature.class)
public interface HugeFungusFeatureInvoker {
    @Invoker("placeHatBlock")
    void callPlaceHatBlock(LevelAccessor level, RandomSource random, HugeFungusConfiguration config, BlockPos.MutableBlockPos pos, float decorationChance, float hatChance, float weepingVineChance);
}
