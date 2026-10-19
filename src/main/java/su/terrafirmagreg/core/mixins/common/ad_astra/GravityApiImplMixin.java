package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.systems.GravityApiImpl;

import su.terrafirmagreg.core.common.environment.DimensionEnvironment;
import su.terrafirmagreg.core.common.environment.EnvironmentSystem;

/**
 * Intercepts Ad Astra's gravity queries to check our EnvironmentSystem first.
 */
@Mixin(value = GravityApiImpl.class, remap = false)
public class GravityApiImplMixin {

    @Inject(method = "getGravity(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F", at = @At("HEAD"), cancellable = true)
    private void tfg$getGravity(Level level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (!DimensionEnvironment.isRegistered(level.dimension()))
            return;
        cir.setReturnValue(EnvironmentSystem.getGravity(level, pos) / DimensionEnvironment.EARTH_GRAVITY);
    }
}
