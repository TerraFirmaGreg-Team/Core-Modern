package su.terrafirmagreg.core.common.data.effects;

import com.gregtechceu.gtceu.common.data.GTDamageTypes;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import su.terrafirmagreg.core.common.data.TFGEffects;

public class InstantDamageEffect extends MobEffect {
    public InstantDamageEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (this == TFGEffects.INSTANT_RADIATION.get()) {
            livingEntity.hurt(GTDamageTypes.RADIATION.source(livingEntity.level()), (float) 50);
        }
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration >= 1;
    }

    @Override
    public boolean isInstantenous() {
        return true;
    }

}
