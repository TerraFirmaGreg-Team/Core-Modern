package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import net.minecraft.network.chat.Component;
import su.terrafirmagreg.core.common.data.tfgt.TFGTItems;

public class RailgunItemLoaderBusMachine extends MultiblockPartMachine implements IUIMachine, IMachineLife {

    @Persisted
    private NotifiableItemStackHandler filterInventory;
    @Persisted
    private NotifiableItemStackHandler itemInventory;

    public RailgunItemLoaderBusMachine(IMachineBlockEntity holder, IO mode) {
        super(holder);

        filterInventory = new NotifiableItemStackHandler(this, 1, IO.IN, IO.NONE).setFilter((stack) -> stack.is(TFGTItems.INTERPLANETARY_LINK.asItem()) && (stack.getCount() == 1));
        itemInventory = new NotifiableItemStackHandler(this, 9, mode, mode);
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(filterInventory);
        clearInventory(itemInventory);
    }

    @Override
    public Widget createUIWidget() {
        if (!isFormed()) {
            var group = new WidgetGroup(0, 0, 40, 20);
            var label = new LabelWidget();
            label.setComponent(Component.translatable("tfg.machine.railgun_loader.parent_not_formed"));
            group.addWidget(label);

            return group;
        }
        var group = new WidgetGroup(0, 0, 90, 150);

        return group;
    }
}
