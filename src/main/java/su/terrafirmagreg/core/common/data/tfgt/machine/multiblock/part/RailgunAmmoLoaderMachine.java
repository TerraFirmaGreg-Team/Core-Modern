package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.TFGCore;

public class RailgunAmmoLoaderMachine extends ItemBusPartMachine {
    public RailgunAmmoLoaderMachine(IMachineBlockEntity holder) {
        super(holder, 0, IO.IN);
        getInventory().setFilter((s) -> s.is(ForgeRegistries.ITEMS.getValue(TFGCore.id("item_railgun_shell"))));
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {}
}
