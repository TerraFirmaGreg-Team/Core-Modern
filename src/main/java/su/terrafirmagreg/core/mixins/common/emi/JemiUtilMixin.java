package su.terrafirmagreg.core.mixins.common.emi;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(targets = "dev.emi.emi.jemi.JemiUtil", remap = false)
public class JemiUtilMixin {

    @ModifyReturnValue(method = "getHandledMods", at = @At("RETURN"), remap = false)
    private static Set<String> tfg$correctHandledMod(Set<String> original) {
        if (original.remove("tfc")) {
            original.add("tfg");
        }
        return original;
    }
}
