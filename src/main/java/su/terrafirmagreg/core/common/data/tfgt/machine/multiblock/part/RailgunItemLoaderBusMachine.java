package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

public class RailgunItemLoaderBusMachine extends MultiblockPartMachine implements IUIMachine, IMachineLife {

    @Persisted
    private NotifiableItemStackHandler filterInventory;
    @Persisted
    private NotifiableItemStackHandler itemInventory;

    public RailgunItemLoaderBusMachine(IMachineBlockEntity holder, IO mode) {
        super(holder);

        filterInventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE);
        itemInventory = new NotifiableItemStackHandler(this, 9, mode, mode);
    }

    @Override
    public Widget createUIWidget() {
        return super.createUIWidget();
    }
}
