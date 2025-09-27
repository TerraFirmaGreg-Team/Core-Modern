package su.terrafirmagreg.core.mixins.common.programmed_circuit_card;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import yuuki1293.pccard.xmod.CompetitionFixer;

@Mixin(value = CompetitionFixer.class, remap = false)
public class CompetitionFixerMixin {

    @Shadow
    public static Supplier<Boolean> existAppflux;

    static {
        existAppflux = () -> true;
    }
}
