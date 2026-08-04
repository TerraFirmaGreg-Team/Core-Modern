package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Mob;

import earth.terrarium.adastra.api.systems.OxygenApi;

@Mixin(Mob.class)
public class MobMixin {

    // Don't set mobs on fire during the day if there's no oxygen

    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void protectMobsFromSun(CallbackInfoReturnable<Boolean> cir) {
        var mob = (Mob) (Object) this;
        if (!OxygenApi.API.hasOxygen(mob.level())) {
            cir.setReturnValue(false);
        }
    }
}
