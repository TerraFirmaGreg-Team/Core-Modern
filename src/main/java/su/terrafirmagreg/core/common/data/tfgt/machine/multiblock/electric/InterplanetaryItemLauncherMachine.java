package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.IBatteryData;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine.PowerStationEnergyBank;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import su.terrafirmagreg.core.common.data.tfgt.InterplanetaryLogisticsNetwork.*;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;
import java.util.ArrayList;
import java.util.List;

public class InterplanetaryItemLauncherMachine extends WorkableElectricMultiblockMachine implements ILogisticsNetworkSender, IMachineLife, IFancyUIMachine, IDisplayUIMachine, IEnergyInfoProvider {

    @Persisted
    @Getter
    public PowerStationEnergyBank energyBank;

    protected TickableSubscription tickSubscription;
    private EnergyContainerList energyInputs;

    public InterplanetaryItemLauncherMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        // Create a fake power substation to stockpile energy inside the multiblock rather than in the energy hatches
        energyBank = new PowerStationEnergyBank(this, List.of(new IBatteryData() {
            @Override
            public int getTier() {
                return 3;
            }

            @Override
            public long getCapacity() {
                return GTValues.V[GTValues.HV] * 32;
            }

            @Override
            public @NotNull String getBatteryName() {
                return "";
            }
        }));
    }

    public InterplanetaryItemLauncherMachine getMachine() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel sLvl) sLvl.getServer().tell(new TickTask(0, () -> {
            updateSubscription();
            getLogisticsNetwork().loadOrCreatePart(this);
        }));
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
    public boolean isMachineInvalid() {
        return !isFormed() || isInValid();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> inputs = new ArrayList<>();

        for (IMultiPart part : getParts()) {
            for (var handler : part.getRecipeHandlers()) {
                var handlerIO = handler.getHandlerIO();
                if (handlerIO == IO.IN && handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) inputs.add(container);
            }
        }

        energyInputs = new EnergyContainerList(inputs);
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

    private void updateSubscription() {
        if (isWorkingEnabled() && isFormed()) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (tickSubscription != null) tickSubscription.unsubscribe();
        }
    }

    private void tick() {
        if (isRemote()) return;
        // Transfer energy from inputs into the multiblock
        if (isWorkingEnabled() && isFormed() && energyInputs != null) {
            long energyBanked = energyBank.fill(energyInputs.getEnergyStored());
            energyInputs.changeEnergy(-energyBanked);
        }
        updateSubscription();
    }


    @Override
    public void onLogisticsConfigurationsChanged() {

    }

    @Override
    public Component getCurrentStatusText() {
        if (!isFormed()) return Component.literal("§cMultiblock not formed");
        return null;
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return new EnergyInfo(energyBank.getCapacity(), energyBank.getStored());
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return false;
    }
}
