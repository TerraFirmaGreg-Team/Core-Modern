package su.terrafirmagreg.core.common.data.effects;

import com.lumintorious.tfcambiental.capability.TemperatureCapability;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import su.terrafirmagreg.core.common.data.TFGEffects;

public class TemperatureChangeEffect extends MobEffect {

    // How much the temperature is changed each effect trigger
    private static float deltaTemp = 2;

    // How many ticks per effect trigger
    private static final int defaultTime = 20;

    //Max temp so people don't die
    private static final float maxTemp = 25;

    public TemperatureChangeEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        TemperatureCapability tempCap = livingEntity.getCapability(TemperatureCapability.CAPABILITY)
                .orElse(TemperatureCapability.DEFAULT);

        if (this == TFGEffects.COOLING.get()) {
            tempCap.setTemperature(tempCap.getTemperature() - deltaTemp * (amplifier + 1));
        } else if (this == TFGEffects.WARMING.get()) {
            if (tempCap.getTemperature() <= maxTemp) tempCap.setTemperature(tempCap.getTemperature() + deltaTemp * (amplifier + 1));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplitude) {
        if (this == TFGEffects.COOLING.get() || this == TFGEffects.WARMING.get()) {
            return duration % defaultTime == 0;
        } else {
            return false;
        }
    }

}
