package su.terrafirmagreg.core.common.tfgt.machine.electric;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.FluidStack;

/**
 * Implemented by GT wrappers that host an {@link OxygenDistributorMachine}.
 */
public interface IOxygenDistributorHost extends IRotorVentHost {

    /**
     * The EU/t the machine can draw from its energy input (0 when no hatch / not formed).
     * Used to prioritise a "waiting for power" status over recipe failure reasons.
     */
    long getEnergyInputPerSec();

    @Nullable
    FluidStack getAtmosphereInput();

    /** Whether the find-leak button should be visible. Synced to client by the wrapper via @DescSynced. */
    boolean showTraceButton();

    void setShowTraceButton(boolean show);
}
