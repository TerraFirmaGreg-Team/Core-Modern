package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
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

/**
 * Dynamo Hatch with battery slots. The Generators consider the Part as a normal Dynamo Hatch and fills ins internal
 * buffer while every tick the buffer is filled or emptied to cover the deficit using the batteries.
 */

public class BatteryDynamoHatchPartMachine extends EnergyHatchPartMachine implements IMuiMachine {

    public static final long AMPS_PER_BATTERY = 2L;

    private final int inventorySize;

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
                    outputAmperes = Math.min(outputAmperes, getChargedBatteryCount());
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
        };
        container.setSideOutputCondition(s -> s == getFrontFacing() && isWorkingEnabled());
        container.setCapabilityValidator(s -> s == null || s == getFrontFacing());
        return container;
    }

    // Same rule as BatteryBufferMachine#getNonEmptyBatteries 1A out per battery that still has charge.
    private long getChargedBatteryCount() {
        long count = 0;
        for (int i = 0; i < batteryInventory.getSlots(); i++) {
            IElectricItem battery = GTCapabilityHelper.getElectricItem(batteryInventory.getStackInSlot(i));
            if (battery != null && battery.canProvideChargeExternally() && battery.getCharge() > 0)
                count++;
        }
        return count;
    }

    /**
     * Battery logic - This is how we transfer energy from the buffer to the batteries depending on the amount of
     * energy in the Part buffer.
     */
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

    private void balanceEnergy() {
        long stored = energyContainer.getEnergyStored();
        long target = energyContainer.getEnergyCapacity() / 2;

        if (stored > target) {
            long charged = transferToBatteries(stored - target, true);
            if (charged > 0)
                energyContainer.removeEnergy(charged);
        } else if (stored < target) {
            long discharged = transferToBatteries(target - stored, false);
            if (discharged > 0)
                energyContainer.addEnergy(discharged);
        }
    }

    private long transferToBatteries(long amount, boolean charge) {
        long perBattery = GTValues.V[tier] * AMPS_PER_BATTERY;
        long moved = 0;

        for (int i = 0; i < batteryInventory.getSlots() && moved < amount; i++) {
            IElectricItem battery = GTCapabilityHelper.getElectricItem(batteryInventory.getStackInSlot(i));
            if (battery == null)
                continue;

            long request = Math.min(amount - moved, perBattery);
            moved += charge ? battery.charge(request, tier, true, false) : battery.discharge(request, tier, true, true, false);
        }

        if (moved > 0)
            markAsChanged();
        return moved;
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
