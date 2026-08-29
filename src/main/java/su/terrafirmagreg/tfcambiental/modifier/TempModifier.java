package su.terrafirmagreg.tfcambiental.modifier;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class TempModifier implements Comparable<TempModifier> {
    private float change;
    private float potency;
    private float wetness;
    private boolean good;

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

    public boolean isGood() {
        return good;
    }

    public void setGood(boolean good) {
        this.good = good;
    }

    public TempModifier(float change, float potency) {
        this(change, potency, 0f);
    }

    public TempModifier(float change, float potency, float wetness) {
        this.change = change;
        this.potency = potency;
        this.wetness = wetness;
        this.good = false;
    }

    public TempModifier(float change, float potency, boolean good) {
        this(change, potency, 0f, good);
    }

    public TempModifier(float change, float potency, float wetness, boolean good) {
        this.change = change;
        this.potency = potency;
        this.wetness = wetness;
        this.good = good;
    }

    public static Optional<TempModifier> defined(float change, float potency) {
        return Optional.of(new TempModifier(change, potency, false));
    }

    public static Optional<TempModifier> defined(float change, float potency, float wetness) {
        return Optional.of(new TempModifier(change, potency, wetness, false));
    }

    public static Optional<TempModifier> defined(float change, float potency, boolean good) {
        return Optional.of(new TempModifier(change, potency, good));
    }

    public static Optional<TempModifier> defined(float change, float potency, float wetness, boolean good) {
        return Optional.of(new TempModifier(change, potency, wetness, good));
    }

    public static Optional<TempModifier> none() {
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull TempModifier o) {
        return Float.compare(this.change, o.change);
    }
}
