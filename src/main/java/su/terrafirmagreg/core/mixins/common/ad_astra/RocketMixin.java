package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.common.entities.vehicles.Rocket;

@Mixin(value = Rocket.class, remap = false)
public abstract class RocketMixin extends Entity {

    public RocketMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Prevents the rocket from exploding when landing due to server lag
     */

    @Inject(method = "explode", at = @At("HEAD"), remap = false, cancellable = true)
    public void tfg$explode(CallbackInfo ci) {
        if (this.getY() < 400) {
            ci.cancel();
        }
    }

}
