package su.terrafirmagreg.core.common.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

/**
 * Implemented by GT wrappers that host an {@link OxygenDistributorMachine}.
 */
public interface IOxygenDistributorHost extends IRecipeLogicMachine {

    /**
     * The EU/t the machine can draw from its energy input (0 when no hatch / not formed).
     * Used to prioritise a "waiting for power" status over recipe failure reasons.
     */
    long getEnergyInputPerSec();

    /** Whether the find-leak button should be visible. Synced to client by the wrapper via @DescSynced. */
    boolean showTraceButton();

    void setShowTraceButton(boolean show);
}
