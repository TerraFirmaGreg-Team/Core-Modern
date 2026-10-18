package su.terrafirmagreg.core.common.tfgt.machine.electric;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;

import net.minecraft.network.chat.Component;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.RotorVentPartMachine;

public interface IRotorVentHost extends IWorkableMultiController {

    @Nullable
    default RotorVentPartMachine getVent() {
        if (isFormed()) {
            for (IMultiPart part : getParts()) {
                if (part instanceof RotorVentPartMachine vent)
                    return vent;
            }
        }
        return null;
    }

    default double getEnergyCostMultiplier() {
        var vent = getVent();
        return vent != null ? vent.getEnergyCostMultiplier() : 1.0;
    }

    default double getConsumptionMultiplier() {
        var vent = getVent();
        return vent != null ? vent.getConsumptionMultiplier() : 1.0;
    }

    @Nullable
    default Component getVentProblem() {
        var vent = getVent();
        return vent != null ? vent.getProblem() : null;
    }
}
