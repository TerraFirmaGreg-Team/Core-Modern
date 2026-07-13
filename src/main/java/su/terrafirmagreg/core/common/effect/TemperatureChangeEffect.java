package su.terrafirmagreg.core.common.effect;

import lombok.Getter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

@Getter
public class TemperatureChangeEffect extends MobEffect {

    private final float targetTemperature;
    private final float temperatureDelta;
    private final boolean isHeating;

    /**
     * Constructor for TemperatureChangeEffect.
     * @param pCategory The category of the effect.
     * @param pColor The color of the effect.
     * @param targetTemperature The target temperature for the effect.
     * @param isHeating Whether the effect is heating or cooling.
     * @param temperatureDelta The amount of temperature change per amplifier level.
     */
    public TemperatureChangeEffect(MobEffectCategory pCategory, int pColor, float targetTemperature, boolean isHeating, float temperatureDelta) {
        super(pCategory, pColor);
        this.targetTemperature = targetTemperature;
        this.isHeating = isHeating;
        this.temperatureDelta = temperatureDelta;
    }

}
