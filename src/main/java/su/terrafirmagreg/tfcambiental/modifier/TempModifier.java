package su.terrafirmagreg.tfcambiental.modifier;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class TempModifier implements Comparable<TempModifier> {
    private float change;
    private float potency;
    private float wetness;

    public float getChange() {
        return change;
    }

    public void setChange(float change) {
        this.change = change;
    }

    public float getPotency() {
        return potency;
    }

    public void setPotency(float potency) {
        this.potency = potency;
    }

    public float getWetness() {
        return wetness;
    }

    public void setWetness(float wetness) {
        this.wetness = wetness;
    }

    public TempModifier(float change, float potency) {
        this(change, potency, 0f);
    }

    public TempModifier(float change, float potency, float wetness) {
        this.change = change;
        this.potency = potency;
        this.wetness = wetness;
    }

    public static Optional<TempModifier> defined(float change, float potency) {
        return Optional.of(new TempModifier(change, potency, 0));
    }

    public static Optional<TempModifier> defined(float change, float potency, float wetness) {
        return Optional.of(new TempModifier(change, potency, wetness));
    }

    public static Optional<TempModifier> none() {
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull TempModifier o) {
        return Float.compare(this.change, o.change);
    }
}
