package su.terrafirmagreg.core.common.tfgt.machine.electric;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.network.chat.Component;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.RotorVentPartMachine;

public interface IRotorVentHost extends IRecipeLogicMachine {

    @Nullable
    default RotorVentPartMachine getVent() {
        if (self() instanceof IMultiController controller && controller.isFormed()) {
            for (IMultiPart part : controller.getParts()) {
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
