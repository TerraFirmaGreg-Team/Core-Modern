package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiMachineUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;

/**
 * Dynamo Hatch with battery slots. The Generators consider the Part as a normal Dynamo Hatch and fills ins internal
 * buffer while every tick the buffer is filled or emptied to cover the deficit using the batteries.
 */

public class BatteryDynamoHatchPartMachine extends EnergyHatchPartMachine
        implements IMuiMachine, IEnergyInfoProvider {

    public static final long AMPS_PER_BATTERY = 2L;

    private final int inventorySize;

    private long chargedThisSec, dischargedThisSec, lastChargedPerSec, lastDischargedPerSec;

    @Getter
    @SaveField
    protected final CustomItemStackHandler batteryInventory;

    private final ConditionalSubscriptionHandler balanceSubscription;

    public BatteryDynamoHatchPartMachine(BlockEntityCreationInfo info, int tier, int inventorySize) {
        super(info, tier, IO.OUT, inventorySize);
        this.inventorySize = inventorySize;
        this.balanceSubscription = new ConditionalSubscriptionHandler(this, this::balanceEnergy, this::hasBatteries);

        this.batteryInventory = new CustomItemStackHandler(inventorySize) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        this.batteryInventory.setFilter(this::isValidBattery);
        this.batteryInventory.setOnContentsChanged(balanceSubscription::updateSubscription);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        balanceSubscription.initialize(getLevel());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        balanceSubscription.unsubscribe();
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        batteryInventory.dropInventoryInWorld(getLevel(), getBlockPos());
    }

    /**
     * Same as EnergyHatchPartMachine for output hatch so it can accept 4A whatever the amount of
     * batteries but the output is limited to 1A per battery like GTm Battery Buffer.
     */
    @Override
    protected NotifiableEnergyContainer createEnergyContainer() {
        NotifiableEnergyContainer container = new NotifiableEnergyContainer(GTValues.V[tier] * 64L * amperage,
                0L, 0L, GTValues.V[tier], amperage) {

            // Copy of NotifiableEnergyContainer#serverTick
            @Override
            public void serverTick() {
                if (getMachine().getLevel().isClientSide)
                    return;
                if (getEnergyStored() >= getOutputVoltage() && getOutputVoltage() > 0 && getOutputAmperage() > 0) {
                    long outputVoltage = getOutputVoltage();
                    long outputAmperes = Math.min(getEnergyStored() / outputVoltage, getOutputAmperage());
                    outputAmperes = Math.min(outputAmperes, getNonEmptyBatteries().size());
                    if (outputAmperes == 0)
                        return;
                    long amperesUsed = 0;
                    for (Direction side : GTUtil.DIRECTIONS) {
                        if (!outputsEnergy(side))
                            continue;
                        var oppositeSide = side.getOpposite();
                        var energyContainer = GTCapabilityHelper.getEnergyContainer(getLevel(),
                                getBlockPos().relative(side), oppositeSide);
                        if (energyContainer != null && energyContainer.inputsEnergy(oppositeSide)) {
                            amperesUsed += energyContainer.acceptEnergyFromNetwork(oppositeSide, outputVoltage,
                                    outputAmperes - amperesUsed);
                            if (amperesUsed >= outputAmperes)
                                break;
                        }
                    }
                    if (amperesUsed > 0) {
                        setEnergyStored(getEnergyStored() - amperesUsed * outputVoltage);
                    }
                }
            }

            @Override
            public void updateTick() {
                if (getMachine().getOffsetTimer() % 20 == 0) {
                    lastChargedPerSec = chargedThisSec;
                    lastDischargedPerSec = dischargedThisSec;
                    chargedThisSec = 0;
                    dischargedThisSec = 0;
                }
                super.updateTick();
            }
        };
        container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        return container;
    }

    // Same as BatteryBufferMachine#getNonFullBatteries (GT items only)
    private List<IElectricItem> getNonFullBatteries() {
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            IElectricItem battery = GTCapabilityHelper.getElectricItem(batteryInventory.getStackInSlot(i));
            if (battery != null && battery.getCharge() < battery.getMaxCharge())
                batteries.add(battery);
        }
        return batteries;
    }

    // Same as BatteryBufferMachine#getNonEmptyBatteries: also gives the output amperage, 1A per battery with charge.
    private List<IElectricItem> getNonEmptyBatteries() {
        List<IElectricItem> batteries = new ArrayList<>();
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            IElectricItem battery = GTCapabilityHelper.getElectricItem(batteryInventory.getStackInSlot(i));
            if (battery != null && battery.canProvideChargeExternally() && battery.getCharge() > 0)
                batteries.add(battery);
        }
        return batteries;
    }

    /**
     * Battery logic - This is how we transfer energy from the buffer to the batteries depending on the amount of
     * energy in the Part buffer.
     */

    private void balanceEnergy() {
        long stored = energyContainer.getEnergyStored();
        long target = energyContainer.getEnergyCapacity() / 2;

        if (stored > target) {
            long charged = chargeBatteries(stored - target);
            if (charged > 0) {
                energyContainer.removeEnergy(charged);
                chargedThisSec += charged;
            }
        } else if (stored < target) {
            long discharged = dischargeBatteries(target - stored);
            if (discharged > 0) {
                energyContainer.addEnergy(discharged);
                dischargedThisSec += discharged;
            }
        }
    }

    // Same as EnergyBatteryTrait#acceptEnergyFromNetwork: energy split evenly between the non-full batteries,
    // each one capped at AMPS_PER_BATTERY of its own tier.
    private long chargeBatteries(long energy) {
        List<IElectricItem> batteries = getNonFullBatteries();
        if (batteries.isEmpty())
            return 0;

        long distributed = energy / batteries.size();
        long charged = 0;
        for (IElectricItem battery : batteries) {
            charged += battery.charge(
                    Math.min(distributed, GTValues.V[battery.getTier()] * AMPS_PER_BATTERY), tier, true, false);
        }

        if (charged > 0)
            markAsChanged();
        return charged;
    }

    // Same as EnergyBatteryTrait#removeEnergy: energy split evenly between the non-empty batteries,
    // each one limited by its own transfer limit (1A of its tier).
    private long dischargeBatteries(long energy) {
        List<IElectricItem> batteries = getNonEmptyBatteries();
        if (batteries.isEmpty())
            return 0;

        long distributed = energy / batteries.size();
        long discharged = 0;
        for (IElectricItem battery : batteries) {
            discharged += battery.discharge(distributed, tier, false, true, false);
        }

        if (discharged > 0)
            markAsChanged();
        return discharged;
    }

    private boolean isValidBattery(ItemStack stack) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(stack);
        return item != null && item.canProvideChargeExternally() && item.getTier() <= tier;
    }

    private boolean hasBatteries() {
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            if (!batteryInventory.getStackInSlot(i).isEmpty())
                return true;
        }
        return false;
    }

    private long getBatteryCharge() {
        long total = 0;
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            IElectricItem battery = GTCapabilityHelper.getElectricItem(batteryInventory.getStackInSlot(i));
            if (battery != null)
                total += battery.getCharge();
        }
        return total;
    }

    private long getBatteryCapacity() {
        long total = 0;
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            IElectricItem battery = GTCapabilityHelper.getElectricItem(batteryInventory.getStackInSlot(i));
            if (battery != null)
                total += battery.getMaxCharge();
        }
        return total;
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return new EnergyInfo(
                BigInteger.valueOf(getBatteryCapacity() + energyContainer.getEnergyCapacity()),
                BigInteger.valueOf(getBatteryCharge() + energyContainer.getEnergyStored()));
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return false;
    }

    @Override
    public long getInputPerSec() {
        return Math.max(0, energyContainer.getInputPerSec() - lastDischargedPerSec);
    }

    @Override
    public long getOutputPerSec() {
        return Math.max(0, energyContainer.getOutputPerSec() - lastChargedPerSec);
    }

    // Gui borrowed from GTm Battery Buffer, update it if they change it because I guess it could look better.

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings) {
        String[] matrix = inventorySize == 8 ? new String[] { "BBBB", "BBBB" } : GTMuiMachineUtil.createSquareMatrix(inventorySize, 'B');

        DoubleSyncValue chargePercentage = syncManager.getOrCreateSyncHandler("batteryCharge",
                DoubleSyncValue.class, () -> new DoubleSyncValue(this::getChargePercentage));

        var flow = Flow.row().width(MachineUIPanel.DEFAULT_CONTENT_WIDTH).height(90);

        flow.child(new ProgressWidget()
                .texture(GTGuiTextures.PROGRESS_BAR_BOILER_EMPTY_STEEL,
                        GTGuiTextures.PROGRESS_BAR_BOILER_HEAT, ProgressDrawable.Direction.UP)
                .value(chargePercentage)
                .marginLeft(5)
                .size(18, 60)
                .tooltipDynamic(this::addChargeTooltip))
                .tooltipAutoUpdate(true)
                .child(GTMuiMachineUtil.createSlotGroupFromInventory(
                        batteryInventory, "batteries",
                        inventorySize, 'B',
                        slot -> slot.background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY),
                        syncManager,
                        matrix)
                        .center());

        mainWidget.child(flow);
    }

    private double getChargePercentage() {
        long capacity = getBatteryCapacity();
        return capacity == 0 ? 0 : (double) getBatteryCharge() / capacity;
    }

    private void addChargeTooltip(RichTooltip tooltip) {
        tooltip.addLine(Component.literal("%s / %s EU".formatted(
                FormattingUtil.formatNumberReadable(getBatteryCharge()),
                FormattingUtil.formatNumberReadable(getBatteryCapacity()))));
    }
}
