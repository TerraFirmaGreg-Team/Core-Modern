package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.client.utils.ClientData;
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
        cir.setReturnValue(EnvironmentSystem.getGravity(level, pos) / DimensionEnvironment.EARTH_GRAVITY);
    }

    // the zip gun works client-side so we need to give it info about the actual gravity
    @Inject(method = "getGravity(Lnet/minecraft/world/entity/Entity;)F", at = @At("HEAD"), cancellable = true)
    private void tfg$getEntityGravity(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (entity.level().isClientSide() && entity == Minecraft.getInstance().player) {
            PlanetData localData = ClientData.getLocalData();
            if (localData != null) {
                cir.setReturnValue(localData.gravity());
            }
        }
    }
}
