package su.terrafirmagreg.tfcambiental.modifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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

    public float getTargetTemperature(float currentTemperature, float minSafeTemp, float maxSafeTemp) {
        float badChange = 0f;
        float goodChange = 0f;
        for (var mod : list) {
            if (mod.isGood()) {
                goodChange += mod.getChange();
            } else {
                badChange += mod.getChange();
            }
        }

        float temperature = currentTemperature + badChange;

        if (goodChange > 0f) {
            goodChange = Math.min(goodChange, maxSafeTemp - temperature);
        } else if (goodChange < 0f) {
            goodChange = Math.max(goodChange, minSafeTemp - temperature);
        }

        return badChange + goodChange;
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
