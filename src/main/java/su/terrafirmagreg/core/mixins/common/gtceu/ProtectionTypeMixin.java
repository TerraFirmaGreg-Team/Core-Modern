package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.ProtectionType;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

@Mixin(value = ProtectionType.class, remap = false)
public abstract class ProtectionTypeMixin {

    //Change mask curio slot from "head" to "face"
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Set;of(Ljava/lang/Object;)Ljava/util/Set;", ordinal = 0))
    private static Set<String> tfg$modifyMaskCurioSlot(Set<String> original) {
        return Set.of("face");
    }
}
