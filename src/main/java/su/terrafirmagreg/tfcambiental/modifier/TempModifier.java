package su.terrafirmagreg.tfcambiental.modifier;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class TempModifier implements Comparable<TempModifier> {
    private String unlocalizedName;
    private float change;
    private float potency;
    private float wetness;
    private int count = 1;
    private float multiplier = 1f;

    public float getChange() {
        return change * multiplier;
    }

    public void setChange(float change) {
        this.change = change;
    }

    public float getPotency() {
        return potency * multiplier;
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

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public TempModifier(String unlocalizedName) {
        this(unlocalizedName, 0f, 0f, 0f);
    }

    public TempModifier(String unlocalizedName, float change, float potency) {
        this(unlocalizedName, change, potency, 0f);
    }

    public TempModifier(String unlocalizedName, float change, float potency, float wetness) {
        this.unlocalizedName = unlocalizedName;
        this.change = change;
        this.potency = potency;
        this.wetness = wetness;
    }

    public static Optional<TempModifier> defined(String unlocalizedName, float change, float potency) {
        return Optional.of(new TempModifier(unlocalizedName, change, potency, 0));
    }

    public static Optional<TempModifier> defined(String unlocalizedName, float change, float potency, float wetness) {
        return Optional.of(new TempModifier(unlocalizedName, change, potency, wetness));
    }

    public static Optional<TempModifier> none() {
        return Optional.empty();
    }

    @Override
    public int compareTo(@NotNull TempModifier o) {
        return Float.compare(this.change, o.change);
    }
}
