package su.terrafirmagreg.tfcambiental.modifier;

import java.util.*;
import java.util.function.Consumer;

public class TempModifierStorage implements Iterable<TempModifier> {
    private List<TempModifier> list = new LinkedList<>();

    public void add(TempModifier value) {
        if (value == null) {
            return;
        }
        list.add(value);
    }

    public void add(Optional<TempModifier> tempModifier) {
        tempModifier.ifPresent(mod -> list.add(mod));
    }

    public float getTargetTemperature() {
        float change = 1f;
        for (var mod : list) {
            change += mod.getChange();
        }
        return change;
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

    public void forEach(Consumer<? super TempModifier> func) {
        list.forEach(func);
    }

    @Override
    public Iterator<TempModifier> iterator() {
        return list.iterator();
    }
}
