package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.systems.TemperatureApiImpl;

import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/**
 * Intercepts Ad Astra's temperature queries to check our EnvironmentSystem first.
 */
@Mixin(value = TemperatureApiImpl.class, remap = false)
public class TemperatureApiImplMixin {

    @Inject(method = "getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)S", at = @At("HEAD"), cancellable = true)
    private void tfg$getTemperature(Level level, BlockPos pos, CallbackInfoReturnable<Short> cir) {
        if (!(level instanceof ServerLevel))
            return;
        cir.setReturnValue((short) EnvironmentSystem.getTemperature(level, pos));
    }
}
