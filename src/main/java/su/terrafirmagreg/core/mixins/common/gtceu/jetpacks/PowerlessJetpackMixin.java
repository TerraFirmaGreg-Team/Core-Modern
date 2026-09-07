package su.terrafirmagreg.core.mixins.common.gtceu.jetpacks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.world.item.ItemStack;

@Mixin(value = PowerlessJetpack.class, remap = false)
public class PowerlessJetpackMixin {

    @Shadow
    private int burnTimer;

    // Fix bug with negative burn timers
    @Inject(method = "drainEnergy", at = @At("HEAD"), remap = false)
    private void tfg$drainEnergy(ItemStack stack, int amount, CallbackInfo ci) {
        if (burnTimer < 0)
            burnTimer = 0;
    }
}
