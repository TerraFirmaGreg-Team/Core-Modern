package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemLoaderBusMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InterplanetaryItemLauncherMachine extends WorkableElectricMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {

    public InterplanetaryItemLauncherMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }


    private List<RailgunItemLoaderBusMachine> getLoaderItemBuses() {
        ArrayList<RailgunItemLoaderBusMachine> loaders = new ArrayList<>();
        for (IMultiPart part: getParts()) {
            if (part instanceof RailgunItemLoaderBusMachine loader) loaders.add(loader);
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
