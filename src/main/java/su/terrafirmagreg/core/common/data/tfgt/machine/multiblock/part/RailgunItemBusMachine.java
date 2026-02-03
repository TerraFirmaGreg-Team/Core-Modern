package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CircuitFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRedstoneSignalMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;

import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;

public class RailgunItemBusMachine extends ItemBusPartMachine implements IRedstoneSignalMachine {
    public RailgunItemBusMachine(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createInventory(Object @NotNull... args) {
        return new NotifiableItemStackHandler(this, getInventorySize(), io)
                .setFilter(v -> !v.getTags().toList().contains(TFGTags.Items.CannotLaunchInRailgun)
                        && v.getItem().canFitInsideContainerItems());
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createCircuitItemHandler(Object @NotNull... args) {
        return new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE)
                .setFilter(IntCircuitBehaviour::isIntegratedCircuit);
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

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getHolder().pos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = TFGMachines.RAILGUN_ITEM_LOADER_OUT[this.getTier()];
        } else if (io == IO.OUT) {
            newDefinition = TFGMachines.RAILGUN_ITEM_LOADER_IN[this.getTier()];
        }

        if (newDefinition == null) return false;
        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof IMachineBlockEntity newHolder) {
            if (newHolder.getMetaMachine() instanceof ItemBusPartMachine newMachine) {
                // We don't set the circuit or distinct busses, since
                // that doesn't make sense on an output bus.
                // Furthermore, existing inventory items
                // and conveyors will drop to the floor on block override.
                newMachine.setFrontFacing(this.getFrontFacing());
                newMachine.setUpwardsFacing(this.getUpwardsFacing());
                newMachine.setPaintingColor(this.getPaintingColor());
            }
        }
        return true;
    }
}
