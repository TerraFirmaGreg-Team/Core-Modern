package su.terrafirmagreg.core.mixins.common.tfcambiental;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.lumintorious.tfcambiental.api.*;
import com.lumintorious.tfcambiental.capability.TemperatureCapability;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;

import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;

/**
 * Adds support for 'fully insulated' armors that ignore tfc-ambiental. Mostly used for the lava diving suit, space
 * suits, and late game armor
 */
@Mixin(value = TemperatureCapability.class, remap = false)
public abstract class TemperatureCapabilityMixin {

    @Shadow
    public abstract Player getPlayer();

    @Shadow
    public abstract void clearModifiers();

    @Shadow
    public TempModifierStorage modifiers;

    @Shadow
    private float target;

    @Shadow
    private float potency;

    @Inject(method = "evaluateModifiers", at = @At(value = "TAIL"))
    public void tfg$evaluateModifiers(CallbackInfo ci) {
        if (this.potency < -1) {
            this.potency = -1;
        }

        clearModifiers();
        EquipmentTemperatureProvider.evaluateAll(getPlayer(), this.modifiers);

        var totalPotency = this.modifiers.getTotalPotency();

        if (totalPotency == (TFCAmbientalCompat.HEATPROOF * 4) + 1) {
            if (this.target > 29f) {
                this.target = 29f;
            }
        } else if (totalPotency == (TFCAmbientalCompat.FULLY_INSULATED * 4) + 1) {
            if (this.target > 29f) {
                this.target = 29f;
            } else if (this.target < 1f) {
                this.target = 1f;
            }
        }
    }
}
