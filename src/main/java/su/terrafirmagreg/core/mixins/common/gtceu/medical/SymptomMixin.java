package su.terrafirmagreg.core.mixins.common.gtceu.medical;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.common.capability.MedicalConditionTracker;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@Mixin(value = Symptom.class, remap = false)
public class SymptomMixin {

    // Replace the instant death with a long-lasting wither effect, because the instant death seems to
    // have problems with the Corpse mod

    @Inject(method = "lambda$static$0", at = @At(value = "HEAD", target = "Lcom/gregtechceu/gtceu/api/data/medicalcondition/Symptom;DEATH:Lcom/gregtechceu/gtceu/api/data/medicalcondition/Symptom;"), remap = false, cancellable = true)
    private static void tfg$deathLambda(MedicalConditionTracker medicalConditionTracker, MedicalCondition condition, Symptom.ConfiguredSymptom configuredSymptom, Symptom baseSymptom, int modifier,
            CallbackInfo ci) {
        if (modifier > 0) {
            medicalConditionTracker.getPlayer().addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * 60, 3));
            ci.cancel();
        }
    }
}
