package su.terrafirmagreg.core.common.tfgt.machine.electric;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

/**
 * Implemented by GT wrappers that host a {@link SpaceHeaterMachine}.
 */
public interface ISpaceHeaterHost extends IRecipeLogicMachine {

    /**
     * The EU/t the machine can draw from its energy input (0 when no hatch / not formed).
     * Used to prioritise a "waiting for power" status over recipe failure reasons.
     */
    long getEnergyInputPerSec();

    /**
     * The highest input voltage of the energy hatch (0 when no hatch / not formed).
     * Used to derive the fixed 0.5A open-room energy draw.
     */
    long getHatchVoltage();

    /**
     * Whether the find-leak UI button should be visible (only when the front region is vented).
     */
    boolean showTraceButton();

    void setShowTraceButton(boolean show);

    /**
     * Offset from the controller to the far (back) end of the multiblock along the front-facing axis.
     * The back hazard region anchors there, past any wall the stick passes through.
     */
    int getBackOffset();
}
