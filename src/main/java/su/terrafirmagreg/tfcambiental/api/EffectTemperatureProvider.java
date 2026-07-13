package su.terrafirmagreg.tfcambiental.api;

import java.util.Optional;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import su.terrafirmagreg.core.common.effect.TemperatureChangeEffect;
import su.terrafirmagreg.tfcambiental.modifier.TempModifier;
import su.terrafirmagreg.tfcambiental.modifier.TempModifierStorage;

/**
 * Interface for providing temperature modifiers based on active effects.
 */
public interface EffectTemperatureProvider {

    /**
     * Evaluates all active effects on the player and adds their temperature modifiers to the storage.
     * @param player The player to check for effects.
     * @param storage The ambiental {@link TempModifierStorage}.
     * @param currentTemp The player's current temperature.
     */
    static void evaluateAll(Player player, TempModifierStorage storage, float currentTemp) {
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            if (effectInstance.getEffect() instanceof TemperatureChangeEffect tempEffect) {
                if (tempEffect.isHeating()) {
                    if (currentTemp < tempEffect.getTargetTemperature()) {
                        storage.add(getModifier(tempEffect, effectInstance.getAmplifier()));
                    }
                } else {
                    if (currentTemp > tempEffect.getTargetTemperature()) {
                        storage.add(getModifier(tempEffect, effectInstance.getAmplifier()));
                    }
                }
            }
        }
    }

    /**
     * Gets the temperature delta for a {@link TemperatureChangeEffect} and amplifier level.
     * @param effect The TemperatureChangeEffect.
     * @param amplifier The amplifier level of the effect.
     * @return An Optional containing the temperature modifier.
     */
    static Optional<TempModifier> getModifier(TemperatureChangeEffect effect, int amplifier) {
        float delta = effect.getTemperatureDelta() * (amplifier + 1);
        float heatingMulti = effect.isHeating() ? 1 : -1;
        return TempModifier.defined(heatingMulti * delta, delta);
    }
}
