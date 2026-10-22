package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import org.spongepowered.asm.mixin.Mixin;

/**
 * @author TomPlop
 * @reason Rotors can't have an efficiency under 100, so we lower it to 1 - GTm will rework their turbine logic one day
 */

@Mixin(value = RotorHolderPartMachine.class, remap = false)
public abstract class RotorHolderPartMachineMixin implements IRotorHolderMachine {

    @Override
    public int getTotalEfficiency() {
        int rotorEfficiency = getRotorEfficiency();
        if (rotorEfficiency == -1) return -1;

        int holderEfficiency = getHolderEfficiency();
        if (holderEfficiency == -1) return -1;

        return Math.max(1, rotorEfficiency * holderEfficiency / 100);
    }
}