package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;

import java.util.List;



public class InterplanetaryItemRecieverMachine extends WorkableElectricMultiblockMachine implements ILogisticsNetworkReciever, IMachineLife, IFancyUIMachine, IDisplayUIMachine {

    public InterplanetaryItemRecieverMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public InterplanetaryItemRecieverMachine getMachine() {
        return this;
    }

    @Override
    public boolean isMachineValid() {
        return isFormed() && !isInValid();
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
    public boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks) {
        return false;
    }

    @Override
    public boolean isRecieverReady() {
        return false;
    }

    @Override
    public void onPackageSent(ItemTransitPackage itemPackage) {

    }
}
