package su.terrafirmagreg.core.mixins.common.gtceu.medical;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.llamalad7.mixinextras.sugar.Local;

@SuppressWarnings("StringEquality")
@Mixin(value = MedicalCondition.class, remap = false)
public class MedicalConditionMixin {

    @ModifyVariable(method = "<init>(Ljava/lang/String;IILcom/gregtechceu/gtceu/api/data/medicalcondition/MedicalCondition$IdleProgressionType;FZ[Lcom/gregtechceu/gtceu/api/data/medicalcondition/Symptom$ConfiguredSymptom;)V", at = @At("HEAD"), remap = false, argsOnly = true)
    private static MedicalCondition.IdleProgressionType tfg$makeWeakPoisonHeal1(MedicalCondition.IdleProgressionType progressionType, @Local(argsOnly = true) String name) {
        if (name == "weak_poison") {
            return MedicalCondition.IdleProgressionType.HEAL;
        } else {
            return progressionType;
        }
    }

    @ModifyVariable(method = "<init>(Ljava/lang/String;IILcom/gregtechceu/gtceu/api/data/medicalcondition/MedicalCondition$IdleProgressionType;FZ[Lcom/gregtechceu/gtceu/api/data/medicalcondition/Symptom$ConfiguredSymptom;)V", at = @At("HEAD"), remap = false, argsOnly = true)
    private static float tfg$makeWeakPoisonHeal2(float idleProgressionRate, @Local(argsOnly = true) String name) {
        if (name == "weak_poison") {
            return 2;
        } else {
            return idleProgressionRate;
        }
    }
}
