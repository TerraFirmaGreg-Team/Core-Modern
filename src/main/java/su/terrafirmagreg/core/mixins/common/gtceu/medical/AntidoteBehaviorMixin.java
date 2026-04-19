package su.terrafirmagreg.core.mixins.common.gtceu.medical;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.api.capability.IMedicalConditionTracker;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.common.item.AntidoteBehavior;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(AntidoteBehavior.class)
public abstract class AntidoteBehaviorMixin {
    @Shadow(remap = false)
    private int removePercent;

    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/capability/IMedicalConditionTracker;heal(Lcom/gregtechceu/gtceu/api/data/medicalcondition/MedicalCondition;I)V"), remap = false)
    private void tfg$replaceHealLogic(IMedicalConditionTracker instance, MedicalCondition medicalCondition, int i, Operation<Void> original, @Local(name = "time") float time,
            @Local(name = "condition") MedicalCondition condition, @Local(name = "tracker") IMedicalConditionTracker tracker) {
        float timeToRemove = (float) Math.ceil(time * ((float) this.removePercent / 100.0F));
        if (timeToRemove > time) {
            tracker.removeMedicalCondition(condition);
        } else {
            tracker.heal(condition, (int) timeToRemove);
        }
    }
}
