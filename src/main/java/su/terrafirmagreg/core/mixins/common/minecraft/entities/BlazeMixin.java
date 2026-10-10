package su.terrafirmagreg.core.mixins.common.minecraft.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;

@Mixin(value = Blaze.class)
public class BlazeMixin {

    // Change blaze's stupid loud annoying breathing noise

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    protected void tfg$getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        cir.setReturnValue(SoundEvents.FIRE_AMBIENT);
    }

    // Acid rain still counts as rain apparently

    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    public void tfg$isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "createAttributes", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$createAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.setReturnValue(Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.23F)
                .add(Attributes.FOLLOW_RANGE, 48.0F)
                .add(Attributes.ATTACK_DAMAGE, 20.0F)
                .add(Attributes.ARMOR, 10.0F));
    }
}
