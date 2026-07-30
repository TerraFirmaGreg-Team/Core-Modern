package su.terrafirmagreg.core.common.tfgt.machine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import su.terrafirmagreg.core.common.data.items.TFGItems;

public interface TFGMachineInstanceFactories {

    MachineInstanceFactory<ItemBusPartMachine> RAILGUN_AMMO_LOADER = (info) -> {
        var machine = new ItemBusPartMachine(info, 0, IO.IN);
        machine.getCircuitSlot().setEnabled(false);
        machine.getInventory().setFilter((s) -> s.is(TFGItems.RAILGUN_AMMO_SHELL.get()));
        return machine;
    };
}
