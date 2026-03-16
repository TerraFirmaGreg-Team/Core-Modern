package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.integration.emi.GTEMIPlugin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;

@Mixin(value = GTEMIPlugin.class, remap = false)
public class GTEMIPluginMixin {

    /**
     * TFG doesn't use GT's potion fluid system. Prevent registration to avoid
     * 44x duplicate recipe IDs in Create's spout filling category.
     */
    @WrapWithCondition(method = "register", at = @At(value = "INVOKE", target = "Ldev/emi/emi/api/EmiRegistry;addEmiStack(Ldev/emi/emi/api/stack/EmiStack;)V", ordinal = 1))
    private boolean tfg$skipPotionFluids(EmiRegistry registry, EmiStack stack) {
        return false;
    }
}
