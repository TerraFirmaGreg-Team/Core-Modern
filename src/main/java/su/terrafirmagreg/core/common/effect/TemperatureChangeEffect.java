package su.terrafirmagreg.core.common.effect;

import com.lumintorious.tfcambiental.capability.TemperatureCapability;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class TemperatureChangeEffect extends MobEffect {

    private static final float deltaTemp = 2;
    private static final int defaultTime = 20;

    private final float targetTemperature;
    private final boolean isHeating;

    /**
     * Constructor for TemperatureChangeEffect.
     * @param pCategory The category of the effect.
     * @param pColor The color of the effect.
     * @param targetTemperature The target temperature for the effect.
     * @param isHeating Whether the effect is heating or cooling.
     */
    public TemperatureChangeEffect(MobEffectCategory pCategory, int pColor, float targetTemperature, boolean isHeating) {
        super(pCategory, pColor);
        this.targetTemperature = targetTemperature;
        this.isHeating = isHeating;
    }

    /**
     * Applies the effect tick to the living entity.
     * @param livingEntity The entity to apply the effect to.
     * @param amplifier The amplifier level of the effect.
     */
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        TemperatureCapability tempCap = livingEntity.getCapability(TemperatureCapability.CAPABILITY)
                .orElse(TemperatureCapability.DEFAULT);

        float currentTemp = tempCap.getTemperature();
        float change = deltaTemp * (amplifier + 1);

        if (isHeating) {
            // If heating check max temp.
            if (currentTemp < targetTemperature) {
                tempCap.setTemperature(Math.min(currentTemp + change, targetTemperature));
            }
        } else {
            // If cooling check min temp.
            if (currentTemp > targetTemperature) {
                tempCap.setTemperature(Math.max(currentTemp - change, targetTemperature));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplitude) {
        return duration % defaultTime == 0;
    }
}
