package su.terrafirmagreg.core.common.tfgt.machine.electric;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.network.chat.Component;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.part.RotorVentPartMachine;

/**
 * GTm8: IWorkableMultiController / IMultiPart no longer exist. The two abstract methods below are
 * implemented by MultiblockControllerMachine (isFormed via Lombok, getParts directly).
 */
public interface IRotorVentHost extends IRecipeLogicMachine {

    boolean isFormed();

    List<MultiblockPartMachine> getParts();

    @Nullable
    default RotorVentPartMachine getVent() {
        if (isFormed()) {
            for (MultiblockPartMachine part : getParts()) {
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
