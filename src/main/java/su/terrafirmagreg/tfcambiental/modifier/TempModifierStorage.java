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
        float goodPositiveChange = 0f;
        float goodNegativeChange = 0f;

        for (var mod : list) {
            if (mod.isGood()) {
                if (mod.getChange() >= 0f) {
                    goodPositiveChange += mod.getChange();
                } else {
                    goodNegativeChange += mod.getChange();
                }
            } else {
                badTemperature += mod.getChange();
            }
        }

        if (badTemperature < minSafeTemp) {
            return Math.min(maxSafeTemp, badTemperature + goodPositiveChange);
        }

        if (badTemperature > maxSafeTemp) {
            return Math.max(minSafeTemp, badTemperature + goodNegativeChange);
        }

        return Mth.clamp(badTemperature + goodPositiveChange + goodNegativeChange, minSafeTemp, maxSafeTemp);
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
