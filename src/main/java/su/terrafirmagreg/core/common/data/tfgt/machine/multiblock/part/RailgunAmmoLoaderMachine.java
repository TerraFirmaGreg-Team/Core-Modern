package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import su.terrafirmagreg.core.common.data.TFGItems;

public class RailgunAmmoLoaderMachine extends ItemBusPartMachine {
    public RailgunAmmoLoaderMachine(BlockEntityCreationInfo info) {
        super(info, 0, IO.IN);
        getInventory().setFilter((s) -> s.is(TFGItems.RAILGUN_AMMO_SHELL.get()));
    }
}
