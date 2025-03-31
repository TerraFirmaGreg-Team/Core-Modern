package su.terrafirmagreg.core.mixins.common.tfcambiental;

import com.lumintorious.tfcambiental.TFCAmbientalConfig;
import com.lumintorious.tfcambiental.api.*;
import com.lumintorious.tfcambiental.capability.TemperatureCapability;
import com.lumintorious.tfcambiental.modifier.TempModifierStorage;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.compat.tfcambiental.TFCAmbientalCompat;

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

	//@SuppressWarnings("UnnecessaryReturnStatement")
	@Inject(method = "evaluateModifiers", at = @At(value = "TAIL"))
	public void tfg$evaluateModifiers(CallbackInfo ci)
	{
		if (this.potency < -1)
		{
			this.potency = -1;
		}

		clearModifiers();
		EquipmentTemperatureProvider.evaluateAll(getPlayer(), this.modifiers);

		if (this.modifiers.getTotalPotency() == (TFCAmbientalCompat.FULLY_INSULATED * 4) + 1)
		{
			this.target = 18f;
		}
	}
}
