package su.terrafirmagreg.core.mixins.common.space_gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.spacegravity.spacegravity.ZeroGravityOrientation;
import com.spacegravity.spacegravity.ZeroGravityPushData;
import com.spacegravity.spacegravity.ZeroGravityPushHelper;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.common.registry.ModItems;

/**
 * Allows a player to always "push off a surface" when holding a zip gun while in zerog.
 */
@Mixin(value = ZeroGravityPushHelper.class, remap = false)
public class ZeroGravityPushHelperMixin {

    @Inject(method = "findNearestPushSurface", at = @At("RETURN"), cancellable = true, remap = false)
    private static void tfg$zipGunThrust(Player player, ZeroGravityOrientation.OrientationData orientation, CallbackInfoReturnable<ZeroGravityPushHelper.PushSurface> cir) {
        if (cir.getReturnValue().available()) {
            return;
        }
        if (!player.getMainHandItem().is(ModItems.ZIP_GUN.get()) && !player.getOffhandItem().is(ModItems.ZIP_GUN.get())) {
            return;
        }
        cir.setReturnValue(new ZeroGravityPushHelper.PushSurface(true, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, ZeroGravityPushData.ContactLimb.NONE));
    }
}
