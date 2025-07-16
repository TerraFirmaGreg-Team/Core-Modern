package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import org.jetbrains.annotations.NotNull;

public class RailgunAmmoLoaderMachine extends ItemBusPartMachine {
    public RailgunAmmoLoaderMachine(IMachineBlockEntity holder) {
        super(holder, 0, IO.IN);
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {}
}
