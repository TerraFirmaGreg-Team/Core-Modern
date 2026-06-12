package com.lumintorious.tfcambiental.modifier;

import java.util.Optional;

public class TempModifier extends su.terrafirmagreg.tfcambiental.modifier.TempModifier {
    public TempModifier(String unlocalizedName, float change, float potency) {
        super(change, potency);
    }

    public TempModifier(String unlocalizedName, float change, float potency, float wetness) {
        super(change, potency, wetness);
    }

    public static Optional<su.terrafirmagreg.tfcambiental.modifier.TempModifier> defined(String unlocalizedName, float change, float potency) {
        return su.terrafirmagreg.tfcambiental.modifier.TempModifier.defined(change, potency);
    }

    public static Optional<su.terrafirmagreg.tfcambiental.modifier.TempModifier> defined(String unlocalizedName, float change, float potency, float wetness) {
        return su.terrafirmagreg.tfcambiental.modifier.TempModifier.defined(change, potency, wetness);
    }

    public static Optional<su.terrafirmagreg.tfcambiental.modifier.TempModifier> none() {
        return su.terrafirmagreg.tfcambiental.modifier.TempModifier.none();
    }
}