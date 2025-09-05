package su.terrafirmagreg.core.mixins.common.tfc;

import earth.terrarium.adastra.api.planets.PlanetApi;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = EnvironmentHelpers.class, remap = false)
public abstract class EnvironmentHelpersMixin {

	// Stops TFC from trying to melt/freeze water on other planets, so ad astra can handle it instead

	@Inject(method = "tickChunk", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler, CallbackInfo ci)
	{
		if (PlanetApi.API.isExtraterrestrial(level) && !Level.OVERWORLD.equals(level.dimension()))
		{
			ci.cancel();
		}
	}
}
