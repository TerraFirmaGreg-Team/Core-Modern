package su.terrafirmagreg.core.common.effect;

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

    //Min/Max temps.
    private static final float maxWarmingTemp = 25;
    private static final float minCoolingTemp = 5;
    private static final float maxBlazingTemp = 60;
    private static final float minFreezingTemp = -20;

    public TemperatureChangeEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        TemperatureCapability tempCap = livingEntity.getCapability(TemperatureCapability.CAPABILITY)
                .orElse(TemperatureCapability.DEFAULT);

        // Cooling and warming have safe temperature caps.
        if (this == TFGEffects.COOLING.get()) {
            if (tempCap.getTemperature() >= minCoolingTemp) {
                tempCap.setTemperature(tempCap.getTemperature() - deltaTemp * (amplifier + 1));
            }
        } else if (this == TFGEffects.WARMING.get()) {
            if (tempCap.getTemperature() <= maxWarmingTemp) {
                tempCap.setTemperature(tempCap.getTemperature() + deltaTemp * (amplifier + 1));
            }
        }
        // Freezing and blazing have dangerous temperature caps.
        if (this == TFGEffects.FREEZING.get()) {
            if (tempCap.getTemperature() >= minFreezingTemp) {
                tempCap.setTemperature(tempCap.getTemperature() - deltaTemp * (amplifier + 1));
            }
        } else if (this == TFGEffects.BLAZING.get()) {
            if (tempCap.getTemperature() <= maxBlazingTemp) {
                tempCap.setTemperature(tempCap.getTemperature() + deltaTemp * (amplifier + 1));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplitude) {
        if (this == TFGEffects.COOLING.get() || this == TFGEffects.WARMING.get() || this == TFGEffects.FREEZING.get() || this == TFGEffects.BLAZING.get()) {
            return duration % defaultTime == 0;
        } else {
            return false;
        }
    }

}
