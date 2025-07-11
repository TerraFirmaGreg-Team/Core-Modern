package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;

import java.util.List;

public class InterplanetaryItemLauncherMachine extends WorkableElectricMultiblockMachine implements ILogisticsNetworkSender, IMachineLife, IFancyUIMachine, IDisplayUIMachine {

    public InterplanetaryItemLauncherMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public InterplanetaryItemLauncherMachine getMachine() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel sLvl) sLvl.getServer().tell(new TickTask(0, () -> getLogisticsNetwork().loadOrCreatePart(this)));
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote()) getLogisticsNetwork().unloadPart(this);
    }

    @Override
    public void onMachineRemoved() {
        if (!isRemote()) getLogisticsNetwork().destroyPart(this);
    }


    @Override
    public boolean isMachineValid() {
        return isFormed() && !isInValid();
    }

    @Override
    public List<NotifiableItemStackHandler> getInventories() {
        return List.of();
    }

    @Override
    public boolean makeInventoryDistinct(int invIndex) {
        return false;
    }

    @Override
    public boolean removeDistinctInventory(int invIndex) {
        return false;
    }

    @Override
    public void onLogisticsConfigurationsChanged() {

    }

}
