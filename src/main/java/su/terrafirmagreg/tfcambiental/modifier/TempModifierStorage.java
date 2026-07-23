package su.terrafirmagreg.tfcambiental.modifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.minecraft.util.Mth;

public class TempModifierStorage implements Iterable<TempModifier> {
    private final List<TempModifier> list = new ArrayList<>();

    public void add(TempModifier value) {
        if (value == null) {
            return;
        }
        list.add(value);
    }

    public void add(Optional<TempModifier> tempModifier) {
        tempModifier.ifPresent(list::add);
    }

    public void clear() {
        list.clear();
    }

    public float getTargetTemperature() {
        float change = 0f;
        for (var mod : list) {
            change += mod.getChange();
        }
        return change;
    }

    public float getTargetTemperature(float minSafeTemp, float maxSafeTemp) {
        float badTemperature = 0f;
        float goodChange = 0f;
        for (var mod : list) {
            if (mod.isGood()) {
                goodChange += mod.getChange();
            } else {
                badTemperature += mod.getChange();
            }
        }
        float temperature = badTemperature + goodChange;
        if (badTemperature >= minSafeTemp && badTemperature <= maxSafeTemp) {
            return Mth.clamp(temperature, minSafeTemp, maxSafeTemp);
        }
        if (badTemperature > maxSafeTemp && temperature < minSafeTemp) {
            return minSafeTemp;
        }
        if (badTemperature < minSafeTemp && temperature > maxSafeTemp) {
            return maxSafeTemp;
        }
        return temperature;
    }

    public float getTotalPotency() {
        float potency = 1f;
        for (var mod : list) {
            potency += mod.getPotency();
        }
        return potency;
    }

    public float getTargetWetness() {
        float wetness = 0f;
        for (var mod : list) {
            wetness += mod.getWetness();
        }
        return wetness;
    }

    @Override
    public Iterator<TempModifier> iterator() {
        return list.iterator();
    }
}
