package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.systems.GravityApiImpl;

import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/**
 * Intercepts Ad Astra's gravity queries to check our EnvironmentSystem first.
 */
@Mixin(value = GravityApiImpl.class, remap = false)
public class GravityApiImplMixin {

    @Inject(method = "getGravity(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F", at = @At("HEAD"), cancellable = true)
    private void tfg$getGravity(Level level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!(level instanceof ServerLevel))
            return;
        if (EnvironmentSystem.hasNormalGravity(level, pos)) {
            cir.setReturnValue(1.0f);
        }
    }
}
