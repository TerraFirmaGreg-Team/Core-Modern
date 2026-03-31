package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.integration.emi.circuit.GTProgrammedCircuitCategory;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.resources.ResourceLocation;

/**
 * Prefixes synthetic EMI recipe ID with "/" to avoid warnings about non-existent recipe IDs.
 * Remove this mixin with the next release of GT (after 7.5.2)
 */
@Mixin(value = GTProgrammedCircuitCategory.GTProgrammedCircuitWrapper.class, remap = false)
public class GTProgrammedCircuitWrapperMixin {

    @ModifyReturnValue(method = "getId", at = @At("RETURN"))
    private ResourceLocation tfg$prefixSyntheticId(ResourceLocation original) {
        return original.withPrefix("/");
    }
}
