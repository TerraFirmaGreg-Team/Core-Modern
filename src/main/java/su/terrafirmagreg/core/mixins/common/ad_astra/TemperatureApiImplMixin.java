package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.constants.PlanetConstants;
import earth.terrarium.adastra.common.systems.TemperatureApiImpl;

import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/**
 * Intercepts Ad Astra's temperature queries to check our EnvironmentSystem first.
 * If our system reports safe temperature at a position, we return comfortable values,
 * preventing freeze/fire damage.
 */
@Mixin(value = TemperatureApiImpl.class, remap = false)
public class TemperatureApiImplMixin {

    @Inject(method = "getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)S", at = @At("HEAD"), cancellable = true)
    private void tfg$getTemperature(Level level, BlockPos pos, CallbackInfoReturnable<Short> cir) {
        if (!(level instanceof ServerLevel))
            return;
        if (EnvironmentSystem.hasTemperature(level, pos)) {
            cir.setReturnValue(PlanetConstants.COMFY_EARTH_TEMPERATURE);
        }
    }

    @Inject(method = "isLiveable", at = @At("HEAD"), cancellable = true)
    private void tfg$isLiveable(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof ServerLevel))
            return;
        if (EnvironmentSystem.hasTemperature(level, pos)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isHot", at = @At("HEAD"), cancellable = true)
    private void tfg$isHot(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof ServerLevel))
            return;
        if (EnvironmentSystem.hasTemperature(level, pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isCold", at = @At("HEAD"), cancellable = true)
    private void tfg$isCold(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof ServerLevel))
            return;
        if (EnvironmentSystem.hasTemperature(level, pos)) {
            cir.setReturnValue(false);
        }
    }
}
