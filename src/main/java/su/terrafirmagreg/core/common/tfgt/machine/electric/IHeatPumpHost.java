package su.terrafirmagreg.core.common.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

/**
 * Implemented by GT wrappers that host a {@link HeatPumpMachine}.
 */
public interface IHeatPumpHost extends IRecipeLogicMachine {

    /**
     * The EU/t the machine can draw from its energy input (0 when no hatch / not formed).
     * Used to prioritise a "waiting for power" status over recipe failure reasons.
     */
    long getEnergyInputPerSec();

    /**
     * Whether the find-leak UI button should be visible (only when the front region is unsealed).
     */
    boolean showTraceButton();

    void setShowTraceButton(boolean show);
}
