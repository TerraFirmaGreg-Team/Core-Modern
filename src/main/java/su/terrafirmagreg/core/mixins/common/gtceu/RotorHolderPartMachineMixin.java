package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

@Mixin(value = RotorHolderPartMachine.class, remap = false)
public abstract class RotorHolderPartMachineMixin {

    @Shadow
    public abstract int getRotorEfficiency();

    @Shadow
    public abstract int getHolderEfficiency();

    /**
     * @author TomPlop
     * @reason Rotors can't have an efficiency under 100, so we lower it to 1
     */
    @Overwrite
    public int getTotalEfficiency() {
        int rotorEfficiency = getRotorEfficiency();
        if (rotorEfficiency == -1)
            return -1;

        int holderEfficiency = getHolderEfficiency();
        if (holderEfficiency == -1)
            return -1;

        return Math.max(1, rotorEfficiency * holderEfficiency / 100);
    }
}
