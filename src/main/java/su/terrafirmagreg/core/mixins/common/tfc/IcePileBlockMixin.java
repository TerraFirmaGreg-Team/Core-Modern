package su.terrafirmagreg.core.mixins.common.tfc;

import earth.terrarium.adastra.api.planets.PlanetApi;
import net.dries007.tfc.common.blocks.IcePileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The EnvironmentHelpers mixin already handles most of the use cases of this class, except
 * TFC has its own IceBlockMixin to melt ice blocks occasionally. Instead of trying to mixin
 * a mixin, just inject into the method it's calling
 */

@Mixin(value = IcePileBlock.class, remap = false)
public abstract class IcePileBlockMixin {

	@Inject(method = "removeIcePileOrIce", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$removeIcePileOrIce(LevelAccessor level, BlockPos pos, BlockState state, CallbackInfo ci)
	{
		if (level instanceof ServerLevel serverLevel && PlanetApi.API.isExtraterrestrial(serverLevel))
		{
			ci.cancel();
		}
	}
}
