package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class InterplanetaryItemRecieverMachine extends WorkableElectricMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {

    public InterplanetaryItemRecieverMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private List<RailgunItemBusMachine> getLoaderItemBuses() {
        ArrayList<RailgunItemBusMachine> loaders = new ArrayList<>();
        for (IMultiPart part: getParts()) {
            if (part instanceof RailgunItemBusMachine loader) loaders.add(loader);
        }
        return Collections.unmodifiableList(loaders);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
    }
}
