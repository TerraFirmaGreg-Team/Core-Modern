package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import earth.terrarium.adastra.api.planets.PlanetApi;

@Mixin(value = EnvironmentHelpers.class, remap = false)
public abstract class EnvironmentHelpersMixin {

    /**
     * Stops TFC from trying to melt/freeze water on other planets, so ad astra can handle it instead
     */

    @Inject(method = "tickChunk", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$tickChunk(ServerLevel level, LevelChunk chunk, ProfilerFiller profiler, CallbackInfo ci) {
        if (PlanetApi.API.isExtraterrestrial(level) && !Level.OVERWORLD.equals(level.dimension())) {
            ci.cancel();
        }
    }
}
