package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRedstoneSignalMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class RailgunItemBusMachine extends ItemBusPartMachine implements IRedstoneSignalMachine {
    public RailgunItemBusMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createCircuitItemHandler(Object @NotNull ... args) {
        return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE).setFilter(IntCircuitBehaviour::isIntegratedCircuit);
    }

    // Override to always attach the circuit config panel
    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        superAttachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(circuitInventory.storage));
    }

    @Override
    public boolean canConnectRedstone(@NotNull Direction side) {
        return side != getFrontFacing();
    }
}
