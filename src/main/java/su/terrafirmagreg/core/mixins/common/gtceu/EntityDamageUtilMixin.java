package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.utils.EntityDamageUtil;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = EntityDamageUtil.class, remap = false)
public class EntityDamageUtilMixin {

    // applyTemperatureDamage() only seems to be used by fluid pipes, so we can check if the player has protection
    // and then cancel the damage if so

    @Inject(method = "applyTemperatureDamage", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/utils/EntityDamageUtil;applyHeatDamage(Lnet/minecraft/world/entity/LivingEntity;I)V"), remap = false, cancellable = true)
    private static void tfg$protectFromHeatDamage(LivingEntity entity, int temperature, float multiplier, int maximum, CallbackInfo ci) {
        if (entity.getItemBySlot(EquipmentSlot.FEET).is(TFGTags.Items.HotProtectionEquipment)) {
            ci.cancel();
        }
    }

    @Inject(method = "applyTemperatureDamage", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/utils/EntityDamageUtil;applyFrostDamage(Lnet/minecraft/world/entity/LivingEntity;I)V"), remap = false, cancellable = true)
    private static void tfg$protectFromColdDamage(LivingEntity entity, int temperature, float multiplier, int maximum, CallbackInfo ci) {
        if (entity.getItemBySlot(EquipmentSlot.FEET).is(TFGTags.Items.ColdProtectionEquipment)) {
            ci.cancel();
        }
    }
}
