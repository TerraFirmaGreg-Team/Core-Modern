package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import su.terrafirmagreg.core.common.data.TFGItems;

public class RailgunAmmoLoaderMachine extends ItemBusPartMachine {
    public RailgunAmmoLoaderMachine(IMachineBlockEntity holder) {
        super(holder, 0, IO.IN);
        getInventory().setFilter((s) -> s.is(TFGItems.RAILGUN_AMMO_SHELL.get()));
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
    }
}
