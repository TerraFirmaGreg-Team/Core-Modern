package su.terrafirmagreg.core.common.data.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

// Dummy effect that only exists to let players know that they're holding onto something hazardous

public class MedicalConditionEffect extends MobEffect {

    public MedicalConditionEffect(int pColor) {
        super(MobEffectCategory.HARMFUL, pColor);
    }
}
