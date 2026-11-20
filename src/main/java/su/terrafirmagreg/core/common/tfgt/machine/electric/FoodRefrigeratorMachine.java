package su.terrafirmagreg.core.common.tfgt.machine.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.client.model.machine.MachineRenderState;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGFoodTraits;

/**
 * Creates the GT Food Refrigerator Machine.
 * This machine has custom logic to give the \'REFRIGERATING\' trait to food items stored within it,
 * and sort and unify food stacks by their expiration date.
 */
public class FoodRefrigeratorMachine extends TieredEnergyMachine implements IControllable, IMuiMachine {

    /**
     * Inventory size int.
     *
     * @param tier GT tier.
     * @return the tier int.
     */
    public static int INVENTORY_SIZE(int tier) {
        return 9 * (tier + (tier + 1) / 2);
    }

    /**
     * The constant MANAGED_FIELD_HOLDER.
     */

    @SaveField
    private boolean currentlyWorking;
    @SaveField
    private final RefrigeratedStorage inventory;
    private final int inventorySize;
    protected ISubscription energySubscription;
    protected TickableSubscription tickSubscription;

    /**
     * Is actively refrigerating boolean.
     *
     * @return boolean
     */
    public boolean isActivelyRefrigerating() {
        return currentlyWorking;
    }

    @Getter
    @SaveField
    private boolean unifyDatesEnabled = true;

    /**
     * Instantiates a New Food Refrigerator Machine.
     *
     * @param info   BE creation info.
     * @param tier   int GT tier.
     */
    public FoodRefrigeratorMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, new NotifiableEnergyContainer(GTValues.V[tier] * 64, GTValues.V[tier], 2L, 0L, 0L));

        inventorySize = INVENTORY_SIZE(tier);

        inventory = attachTrait(new RefrigeratedStorage(inventorySize));
        currentlyWorking = false;
    }

    // #region Logic

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote())
            return;
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, () -> {
                setActive(currentlyWorking);
                updateSubscription();
            }));
        }

        energySubscription = energyContainer.addChangedListener(this::updateSubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        if (energySubscription != null) {
            energySubscription.unsubscribe();
            energySubscription = null;
        }
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    /**
     * Update subscription.
     * <p>
     * Re-evaluates whether the machine should be actively refrigerating based on the
     * current workingEnabled flag and available energy.
     * <p>
     * If it can work and the inventory is not empty:
     * - When starting: apply the REFRIGERATING trait to all stored food, unify dates,
     *     combine stacks, compact the inventory, mark dirty and subscribe to server ticks.
     * - Ensure a tick subscription exists to consume energy each tick.
     * <p>
     * If it cannot work:
     * - If it was previously working: remove the REFRIGERATING trait from all items and mark dirty.
     * - Unsubscribe any existing tick subscription.
     * <p>
     * This runs on the server side and is triggered by energy or setting changes.
     */
    public void updateSubscription() {
        if (isRemote())
            return;
        boolean canWork = workingEnabled && consumeEnergy(true);

        if (canWork && !inventory.isEmpty()) {
            if (!currentlyWorking) {
                inventory.changeTraitForAll(true);
                currentlyWorking = true;
                setActive(true);

                // Initial maintenance when transitioning to working state.
                inventory.maintainNow();
            }
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (currentlyWorking) {
                inventory.changeTraitForAll(false);
                currentlyWorking = false;
                setActive(false);
            }
            if (tickSubscription != null) {
                tickSubscription.unsubscribe();
                tickSubscription = null;
            }
        }
    }

    /**
     * Tick.
     * <p>
     * Called each server tick while the machine has an active tick subscription.
     */
    public void tick() {
        if (workingEnabled && !inventory.isEmpty())
            consumeEnergy(false);

        updateSubscription();
    }

    private long getEnergyAmount() {
        // 1A of LV per inventory row
        return (long) GTValues.VA[GTValues.LV] * (inventorySize / 9);
    }

    private boolean consumeEnergy(boolean simulate) {
        long amount = energyContainer.getEnergyStored() - getEnergyAmount();
        if ((amount < 0 || amount > energyContainer.getEnergyCapacity()))
            return false;

        if (!simulate)
            energyContainer.removeEnergy(getEnergyAmount());

        return true;
    }

    // #endregion

    // #region Capabilities

    @SaveField
    private boolean workingEnabled = true;

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.workingEnabled == isWorkingAllowed)
            return;
        this.workingEnabled = isWorkingAllowed;
        if (!isRemote()) {
            updateSubscription();
        }
    }

    /**
     * Set Unify Dates Enabled.
     * <p>
     * Enables or disables automatic unification of food creation dates.
     * When enabled and the refrigerator is actively refrigerating on the server,
     * the machine will:
     *  - set partially-filled compatible stacks to the earliest creation date found,
     *  - compact the inventory to remove gaps,
     *  - and notify that contents changed.
     *
     * @param enabled true to enable automatic date unification, false to disable it.
     */
    public void setUnifyDatesEnabled(boolean enabled) {
        if (this.unifyDatesEnabled == enabled)
            return;
        this.unifyDatesEnabled = enabled;

        if (!isRemote() && enabled && currentlyWorking) {
            // Immediate maintenance when enabling unify while working.
            inventory.maintainNow();
        }
        updateSubscription();
    }

    // #endregion

    // #region GUI

    private static final int SLOTS_PER_ROW = 9;
    private static final UITexture UNIFY_ON = UITexture.fullImage("tfg:textures/gui/widgets/unify_dates_on.png");
    private static final UITexture UNIFY_OFF = UITexture.fullImage("tfg:textures/gui/widgets/unify_dates_off.png");

    @Override
    public MachineUIPanelBuilder getPanelBuilder(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return MachineUIPanelBuilder.panelBuilder(this)
                .rightConfigurators(flow -> flow.child(new ToggleButton()
                        .value(new BooleanSyncValue(this::isUnifyDatesEnabled, this::setUnifyDatesEnabled).allowC2S())
                        .overlay(true, UNIFY_ON)
                        .overlay(false, UNIFY_OFF)
                        .addTooltipLine(Text.lang("tfg.gui.refrigerator.unify_dates"))));
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
            UISettings settings) {
        int rows = (inventorySize + SLOTS_PER_ROW - 1) / SLOTS_PER_ROW;

        DoubleSyncValue energyFill = new DoubleSyncValue(this::getEnergyFill);
        LongSyncValue energyStored = new LongSyncValue(energyContainer::getEnergyStored);
        syncManager.syncValue("energyFill", energyFill);
        syncManager.syncValue("energyStored", energyStored);

        syncManager.registerSlotGroup("fridge_inv", SLOTS_PER_ROW);
        ParentWidget<?> slots = new ParentWidget<>();
        slots.size(18 * SLOTS_PER_ROW, 18 * rows);
        for (int i = 0; i < inventorySize; i++) {
            slots.child(new ItemSlot()
                    .slot(SyncHandlers.itemSlot(inventory, i).slotGroup("fridge_inv"))
                    .left(18 * (i % SLOTS_PER_ROW))
                    .top(18 * (i / SLOTS_PER_ROW)));
        }

        var energyBar = new ProgressWidget()
                .texture(GTGuiTextures.PROGRESS_BAR_BOILER_EMPTY_STEEL,
                        GTGuiTextures.PROGRESS_BAR_BOILER_HEAT, ProgressDrawable.Direction.UP)
                .value(energyFill)
                .size(10, 18 * rows)
                .tooltipAutoUpdate(true)
                .tooltipDynamic(r -> {
                    r.addLine(Component.literal(FormattingUtil.formatNumbers(energyStored.getLongValue()) + " / " +
                            FormattingUtil.formatNumbers(energyContainer.getEnergyCapacity()) + " EU"));
                    r.addLine(Component.literal(FormattingUtil.formatNumbers(getEnergyAmount()) + " EU/t"));
                });

        mainWidget.child(Flow.row()
                .coverChildren()
                .margin(5)
                .childPadding(4)
                .child(energyBar)
                .child(slots));
    }

    private double getEnergyFill() {
        long capacity = energyContainer.getEnergyCapacity();
        return capacity == 0 ? 0 : (double) energyContainer.getEnergyStored() / capacity;
    }

    // #endregion

    // #region Refrigerated trait
    /**
     * Refrigerated Storage.
     * <p>
     * Storage handler for the Food Refrigerator.
     * Manages the machine's inventory and enforces refrigeration behaviour:
     * - accepts only non-rotten food items,
     * - applies/removes the 'REFRIGERATING' trait while the machine is active,
     * - unifies food creation dates, combines partial stacks and compacts slots when contents change,
     * - intercepts insert/extract/set operations to maintain traits.
     */
    public class RefrigeratedStorage extends NotifiableItemStackHandler {

        private boolean internalEdit = false;
        private boolean maintenancePending = false;
        private boolean maintenanceScheduled = false;

        /**
         * Instantiates a New Refrigerated Storage.
         *
         * @param slots   int slots.
         */
        public RefrigeratedStorage(int slots) {
            super(slots, IO.IN, IO.IN);
        }

        private void setNotifying(int slot, ItemStack stack) {
            internalEdit = true;
            try {
                RefrigeratedStorage.super.setStackInSlot(slot, stack);
            } finally {
                internalEdit = false;
            }
        }

        /**
         * Schedule maintenance once for this tick.
         */
        private void scheduleMaintenance() {
            if (FoodRefrigeratorMachine.this.isRemote())
                return;
            maintenancePending = true;
            if (maintenanceScheduled)
                return;

            maintenanceScheduled = true;
            if (FoodRefrigeratorMachine.this.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::runMaintenanceIfPending));
            }
        }

        /**
         * Immediate maintenance, used on state transitions.
         */
        public void maintainNow() {
            if (FoodRefrigeratorMachine.this.isRemote())
                return;
            maintenancePending = true;
            runMaintenanceIfPending();
        }

        private void runMaintenanceIfPending() {
            if (!maintenancePending)
                return;
            maintenancePending = false;
            maintenanceScheduled = false;

            internalEdit = true;
            try {
                if (FoodRefrigeratorMachine.this.currentlyWorking) {
                    unifyFoodDates();
                    combineStacks();
                }
                compactInventory();
                onContentsChanged();
            } finally {
                internalEdit = false;
            }
            FoodRefrigeratorMachine.this.updateSubscription();
        }

        /**
         * Change trait for All.
         *
         * @param add boolean add.
         */
        public void changeTraitForAll(boolean add) {
            for (int i = 0; i < storage.getSlots(); i++) {
                ItemStack stack = storage.getStackInSlot(i);
                if (stack.isEmpty())
                    continue;

                ItemStack copy = stack.copy();
                if (add) {
                    FoodCapability.applyTrait(copy, TFGFoodTraits.REFRIGERATING);
                } else {
                    FoodCapability.removeTrait(copy, TFGFoodTraits.REFRIGERATING);
                }
                setNotifying(i, copy);
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            IFood food = FoodCapability.get(stack);
            return food != null;
        }

        private void unifyFoodDates() {
            if (FoodRefrigeratorMachine.this.isRemote())
                return;

            if (!FoodRefrigeratorMachine.this.currentlyWorking)
                return;
            if (!FoodRefrigeratorMachine.this.unifyDatesEnabled)
                return;

            final int slots = storage.getSlots();
            final boolean[] processed = new boolean[slots];

            for (int i = 0; i < slots; i++) {
                if (processed[i])
                    continue;

                final ItemStack base = storage.getStackInSlot(i);
                final IFood baseFood = base.isEmpty() ? null : FoodCapability.get(base);
                if (base.isEmpty() || baseFood == null || baseFood.isRotten()) {
                    processed[i] = true;
                    continue;
                }

                final int[] group = new int[slots];
                int gSize = 0;
                boolean hasNonFull = false;
                long minDate = Long.MAX_VALUE;

                for (int j = i; j < slots; j++) {
                    if (processed[j])
                        continue;
                    final ItemStack other = storage.getStackInSlot(j);
                    final IFood otherFood = other.isEmpty() ? null : FoodCapability.get(other);
                    if (other.isEmpty() || otherFood == null || otherFood.isRotten())
                        continue;
                    if (!FoodCapability.areStacksStackableExceptCreationDate(other, base))
                        continue;

                    group[gSize++] = j;

                    if (other.getCount() < other.getMaxStackSize()) {
                        hasNonFull = true;
                        long date = otherFood.getCreationDate();
                        if (date < minDate)
                            minDate = date;
                    }
                }

                if (!hasNonFull || minDate == Long.MAX_VALUE) {
                    for (int k = 0; k < gSize; k++)
                        processed[group[k]] = true;
                    continue;
                }

                for (int k = 0; k < gSize; k++) {
                    int idx = group[k];
                    ItemStack st = storage.getStackInSlot(idx);
                    if (st.isEmpty()) {
                        processed[idx] = true;
                        continue;
                    }
                    if (st.getCount() < st.getMaxStackSize()) {
                        ItemStack copy = st.copy();
                        IFood food = FoodCapability.get(copy);
                        if (food != null)
                            food.setCreationDate(minDate);
                        setNotifying(idx, copy);
                    }
                    processed[idx] = true;
                }
            }
        }

        private void combineStacks() {
            if (FoodRefrigeratorMachine.this.isRemote())
                return;

            if (!FoodRefrigeratorMachine.this.currentlyWorking)
                return;

            final int slots = storage.getSlots();
            final boolean[] processed = new boolean[slots];

            for (int i = 0; i < slots; i++) {
                if (processed[i])
                    continue;

                final ItemStack base = storage.getStackInSlot(i);
                final IFood baseFood = base.isEmpty() ? null : FoodCapability.get(base);
                if (base.isEmpty() || baseFood == null || baseFood.isRotten()) {
                    processed[i] = true;
                    continue;
                }

                final int[] group = new int[slots];
                int gSize = 0;

                for (int j = i; j < slots; j++) {
                    if (processed[j])
                        continue;
                    final ItemStack other = storage.getStackInSlot(j);
                    final IFood otherFood = other.isEmpty() ? null : FoodCapability.get(other);
                    if (other.isEmpty() || otherFood == null || otherFood.isRotten())
                        continue;
                    if (!FoodCapability.areStacksStackableExceptCreationDate(other, base))
                        continue;

                    if (!FoodRefrigeratorMachine.this.unifyDatesEnabled) {
                        if (otherFood.getCreationDate() != baseFood.getCreationDate())
                            continue;
                    }

                    group[gSize++] = j;
                }

                int total = 0;
                int maxSize = 0;
                ItemStack template = ItemStack.EMPTY;

                for (int k = 0; k < gSize; k++) {
                    ItemStack st = storage.getStackInSlot(group[k]);
                    if (st.isEmpty())
                        continue;
                    if (st.getCount() < st.getMaxStackSize()) {
                        total += st.getCount();
                        maxSize = st.getMaxStackSize();
                        if (template.isEmpty())
                            template = st.copy();
                    }
                }

                if (template.isEmpty() || total <= 1) {
                    for (int k = 0; k < gSize; k++)
                        processed[group[k]] = true;
                    continue;
                }

                int remaining = total;
                for (int k = 0; k < gSize; k++) {
                    int idx = group[k];
                    ItemStack st = storage.getStackInSlot(idx);
                    if (st.isEmpty() || st.getCount() == st.getMaxStackSize())
                        continue;

                    if (remaining <= 0) {
                        setNotifying(idx, ItemStack.EMPTY);
                        continue;
                    }

                    int put = Math.min(maxSize, remaining);
                    ItemStack filled = template.copy();
                    filled.setCount(put);
                    setNotifying(idx, filled);
                    remaining -= put;
                }

                for (int k = 0; k < gSize; k++)
                    processed[group[k]] = true;
            }
        }

        private void compactInventory() {
            if (FoodRefrigeratorMachine.this.isRemote())
                return;

            final int slots = storage.getSlots();
            int nextFree = 0;
            for (int i = 0; i < slots; i++) {
                ItemStack cur = storage.getStackInSlot(i);
                if (cur.isEmpty())
                    continue;
                if (i != nextFree) {
                    setNotifying(nextFree, cur.copy());
                    setNotifying(i, ItemStack.EMPTY);
                }
                nextFree++;
            }
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty())
                return ItemStack.EMPTY;

            IFood incoming = FoodCapability.get(stack);
            if (incoming == null)
                return stack;

            ItemStack toInsert = stack.copy();
            if (currentlyWorking)
                FoodCapability.applyTrait(toInsert, TFGFoodTraits.REFRIGERATING);

            ItemStack result = storage.insertItem(slot, toInsert, simulate);

            if (currentlyWorking)
                FoodCapability.removeTrait(result, TFGFoodTraits.REFRIGERATING);

            if (!simulate) {
                // Defer reordering until end of tick.
                scheduleMaintenance();
            }
            return result;
        }

        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0)
                return ItemStack.EMPTY;

            ItemStack result = storage.extractItem(slot, amount, simulate);
            FoodCapability.removeTrait(result, TFGFoodTraits.REFRIGERATING);

            IFood food = FoodCapability.get(result);
            if (food != null) {
                long orig = food.getCreationDate();
                long rounded = FoodCapability.getRoundedCreationDate(orig);
                food.setCreationDate(Math.min(orig, rounded));
            }

            if (!simulate) {
                // Defer reordering until end of tick.
                scheduleMaintenance();
            }
            return result;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (!internalEdit) {
                if (stack.isEmpty()) {
                    RefrigeratedStorage.super.setStackInSlot(slot, ItemStack.EMPTY);
                    // Defer reordering until end of tick.
                    scheduleMaintenance();
                    return;
                }

                IFood food = FoodCapability.get(stack);
                if (food == null)
                    return;

                ItemStack toSet = stack;
                if (currentlyWorking) {
                    toSet = stack.copy();
                    FoodCapability.applyTrait(toSet, TFGFoodTraits.REFRIGERATING);
                }

                RefrigeratedStorage.super.setStackInSlot(slot, toSet);
                // Defer reordering until end of tick.
                scheduleMaintenance();
                return;
            }

            RefrigeratedStorage.super.setStackInSlot(slot, stack);
        }

        @Override
        public void onMachineDestroyed() {
            if (!(FoodRefrigeratorMachine.this.getLevel() instanceof ServerLevel serverLevel))
                return;
            var pos = FoodRefrigeratorMachine.this.getBlockPos();
            for (ItemStack drop : drainAllForDrop()) {
                Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
        }

        public java.util.List<ItemStack> drainAllForDrop() {
            java.util.List<ItemStack> drops = new java.util.ArrayList<>();
            final int slots = storage.getSlots();
            for (int i = 0; i < slots; i++) {
                ItemStack st = storage.getStackInSlot(i);
                if (st.isEmpty())
                    continue;

                ItemStack copy = st.copy();
                FoodCapability.removeTrait(copy, TFGFoodTraits.REFRIGERATING);

                // Round expiration date.
                IFood food = FoodCapability.get(copy);
                if (food != null) {
                    long orig = food.getCreationDate();
                    long rounded = FoodCapability.getRoundedCreationDate(orig);
                    food.setCreationDate(Math.min(orig, rounded));
                }
                drops.add(copy);
            }

            // Clear slots without triggering unification.
            internalEdit = true;
            try {
                for (int i = 0; i < slots; i++) {
                    RefrigeratedStorage.super.setStackInSlot(i, ItemStack.EMPTY);
                }
            } finally {
                internalEdit = false;
            }
            return drops;
        }
    }

    private void setActive(boolean active) {
        MachineRenderState renderState = getRenderState();
        if (renderState == null)
            return;
        if (renderState.hasProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS)) {
            setRenderState(renderState.setValue(
                    GTMachineModelProperties.RECIPE_LOGIC_STATUS,
                    active ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
        }
    }

    // #endregion
}
