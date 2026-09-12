package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.systems.OxygenApiImpl;

import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/**
 * Intercepts Ad Astra's oxygen queries to check our EnvironmentSystem first.
 * If our system reports oxygen at a position, we return true immediately,
 * skipping Ad Astra's PlanetHandler lookup.
 */
@Mixin(value = OxygenApiImpl.class, remap = false)
public class OxygenApiImplMixin {

    @Inject(method = "hasOxygen(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void tfg$hasOxygen(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof ServerLevel))
            return;
        if (EnvironmentSystem.hasOxygen(level, pos)) {
            cir.setReturnValue(true);
        }
    }
}
