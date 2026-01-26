package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.common.utils.ModUtils;

@Mixin(value = ModUtils.class, remap = false)
public class ModUtilsMixin {

    @Inject(method = "land", at = @At(value = "INVOKE", target = "earth/terrarium/adastra/common/entities/vehicles/Lander.inventory ()Learth/terrarium/adastra/common/container/VehicleContainer;"), cancellable = true)
    private static void tfg$land(ServerPlayer player, ServerLevel targetLevel, Vec3 pos, CallbackInfo ci) {
        Entity vehicle = player.getVehicle();
        assert vehicle != null;

        if (vehicle.getControllingPassenger() != player)
            ci.cancel();
    }
}
