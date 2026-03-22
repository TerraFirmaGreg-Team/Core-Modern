package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.negodya1.vintageimprovements.content.kinetics.helve_hammer.HelveBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;

@Mixin(value = HelveBlockEntity.class, remap = false)
public class HelveBlockEntityMixin {
    @Shadow
    public SmartInventory outputInv;

    // spotless:off
    /**
     * @author Ujhik
     * @reason Fix helve hammer not processing output items like for example helve hammer processing iron boom into refined iron bloom but not this last into iron ingot without removing the refined ones and adding them again.
     */
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/item/SmartInventory;isEmpty()Z",
                    remap = true
            ),
            remap = false
    )
    // spotless:on
    private boolean tfg$checkBothInventories(SmartInventory instance, Operation<Boolean> original) {
        return original.call(instance) && outputInv.isEmpty();
    }
}
