package su.terrafirmagreg.core.mixins.client.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import earth.terrarium.adastra.api.systems.PlanetData;
import earth.terrarium.adastra.client.utils.ClientData;
import earth.terrarium.adastra.common.systems.GravityApiImpl;

/**
 * the zip gun works client-side so we need to give it info about the actual gravity
 */
@Mixin(value = GravityApiImpl.class, remap = false)
public class GravityApiImplClientMixin {
    @Inject(method = "getGravity(Lnet/minecraft/world/entity/Entity;)F", at = @At("HEAD"), cancellable = true)
    private void tfg$getEntityGravity(Entity entity, CallbackInfoReturnable<Float> cir) {
        if (entity == Minecraft.getInstance().player) {
            PlanetData localData = ClientData.getLocalData();
            if (localData != null) {
                cir.setReturnValue(localData.gravity());
            }
        }
    }
}
