package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;
import net.minecraft.world.effect.MobEffects;

public class TFGTMedicalConditions {

	public static final Symptom GLOWING = new Symptom("symptom.tfg.glowing", 5, 1, MobEffects.GLOWING);

    public static final MedicalCondition RADIOACTIVE = new MedicalCondition("radioactive", 0x00ff00, 2000,
            MedicalCondition.IdleProgressionType.NONE, 0, true,
            new Symptom.ConfiguredSymptom(Symptom.DEATH),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, 0.75f),
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE, 0.4f),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 0.4f),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 0.2f),
			new Symptom.ConfiguredSymptom(GLOWING, 0.05f));

}
