package su.terrafirmagreg.core.mixins.common.tfc;

import earth.terrarium.adastra.api.planets.PlanetApi;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Let ad astra handle snow/ice on other planets

@Mixin(value = EnvironmentHelpers.class, remap = false)
public abstract class EnvironmentHelpersMixin {

	@Inject(method = "tickChunk", at = @At("HEAD"), remap = false, cancellable = true)
	private static void tfg$tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler, CallbackInfo ci)
	{
		if (PlanetApi.API.isExtraterrestrial(level))
		{
			ci.cancel();
		}
	}
}
