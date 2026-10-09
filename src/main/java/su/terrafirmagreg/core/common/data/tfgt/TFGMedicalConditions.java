package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import com.gregtechceu.gtceu.common.data.GTMedicalConditions;

import net.minecraft.world.effect.MobEffects;

import su.terrafirmagreg.core.TFGCore;

public class TFGMedicalConditions {

    public static final Symptom GLOWING = Symptom.ofEffect("symptom.tfg.glowing", 5, 1.f, 1.f, MobEffects.GLOWING);

    public static final MedicalCondition RADIOACTIVE = GTMedicalConditions.register(TFGCore.id("radioactive"), 0x00ff00, 2000,
            MedicalCondition.IdleProgressionType.NONE, 0, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF),
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS),
            new Symptom.ConfiguredSymptom(GLOWING));
}
