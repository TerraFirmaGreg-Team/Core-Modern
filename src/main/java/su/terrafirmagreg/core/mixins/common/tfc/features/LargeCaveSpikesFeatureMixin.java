package su.terrafirmagreg.core.mixins.common.tfc.features;

import net.dries007.tfc.world.feature.cave.LargeCaveSpikesFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LargeCaveSpikesFeature.class, remap = false)
public class LargeCaveSpikesFeatureMixin {

	// Stops stalagmites from generating out in the open
	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void tfg$place(WorldGenLevel level, BlockPos pos, BlockState spike, BlockState raw, Direction direction, RandomSource random, CallbackInfo ci) {
		if (level.canSeeSkyFromBelowWater(pos))
		{
			ci.cancel();
		}
	}
}
