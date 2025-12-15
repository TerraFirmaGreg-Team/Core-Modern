package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;

import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;


// Cancel QuarkTech helmet feeding when player is in PlayerRevive bleeding state
// This prevents food from being consumed without effect

@Mixin(value = QuarkTechSuite.class, remap = false)
public class QuarkTechSuiteMixin {

    @Inject(method = "supplyFood", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$preventQuarkFeedingWhenBleeding(IElectricItem item, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.getPersistentData().getBoolean("playerrevive:bleeding") || player.level().isClientSide) {
            cir.setReturnValue(false);
        }
    }
}