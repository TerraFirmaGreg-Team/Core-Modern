package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;

import java.util.List;

public class InterplanetaryItemLauncherMachine extends WorkableElectricMultiblockMachine implements ILogisticsNetworkSender, IFancyUIMachine, IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(InterplanetaryItemLauncherMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    @Getter @Setter
    private String logisticsUILabel;

    @Persisted @Getter
    private List<NetworkSenderConfigEntry> sendConfigurations;

    public InterplanetaryItemLauncherMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public InterplanetaryItemLauncherMachine getMachine() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) InterplanetaryLogisticsNetwork.get().loadPart(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote()) InterplanetaryLogisticsNetwork.get().unloadPart(this);
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
