package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

import java.util.ArrayList;
import java.util.List;

public class InterplanetaryItemReceiverMachine extends WorkableElectricMultiblockMachine implements ILogisticsNetworkReceiver, IMachineLife, IFancyUIMachine, IDisplayUIMachine {

    public InterplanetaryItemReceiverMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public InterplanetaryItemReceiverMachine getMachine() {
        return this;
    }

    @Override
    public boolean isMachineInvalid() {
        return !isFormed() || isInValid();
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
    public List<RailgunItemBusMachine> getInventories() {
        if (isMachineInvalid()) return List.of();
        List<RailgunItemBusMachine> parts = new ArrayList<>();
        for (var part: getParts()) {
            if (part instanceof RailgunItemBusMachine r) parts.add(r);
        }
        return parts;
    }

    @Override
    public boolean canAcceptItems(int inventoryIndex, List<ItemStack> stacks) {
        return false;
    }

    @Override
    public void onPackageSent(DimensionalBlockPos sentFrom, List<ItemStack> items, long sentTick) {

    }

    @Override
    public Component getCurrentStatusText() {
        if (!isFormed()) return Component.literal("§cMultiblock not formed");
        return null;
    }
}
