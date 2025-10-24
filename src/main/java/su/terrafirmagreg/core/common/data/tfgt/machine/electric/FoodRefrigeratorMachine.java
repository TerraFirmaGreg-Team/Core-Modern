package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGFoodTraits;

/**
 * Creates the GT Food Refrigerator Machine.
 * This machine has custom logic to give the \'REFRIGERATING\' trait to food items stored within it,
 * and sort and unify food stacks by their expiration date.
 */
public class FoodRefrigeratorMachine extends TieredEnergyMachine
        implements IControllable, IFancyUIMachine, IMachineLife {

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
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FoodRefrigeratorMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Persisted
    private boolean currentlyWorking;
    @Persisted
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
    @Persisted
    private boolean unifyDatesEnabled = true;

    /**
     * Instantiates a New Food Refrigerator Machine.
     *
     * @param holder IMachineBlockEntity holder.
     * @param tier   int GT tier.
     * @param args   Object args.
     */
    public FoodRefrigeratorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);

        inventorySize = INVENTORY_SIZE(tier);

        inventory = new RefrigeratedStorage(this, inventorySize);
        currentlyWorking = false;
    }

    @Override
    protected @NotNull NotifiableEnergyContainer createEnergyContainer(Object @NotNull... args) {
        return new NotifiableEnergyContainer(this, GTValues.V[tier] * 64, GTValues.V[tier], 2L, 0L, 0L);
    }

    // #region Logic

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote())
            return;
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSubscription));
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

    @Override
    public void onMachineRemoved() {
        if (!isRemote() && getLevel() instanceof ServerLevel serverLevel) {
            var pos = getPos();
            for (ItemStack drop : inventory.drainAllForDrop()) {
                if (!drop.isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), drop);
                }
            }
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

                // Initial maintenance when transitioning to working state.
                inventory.maintainNow();
                markDirty();
            }
            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (currentlyWorking) {
                inventory.changeTraitForAll(false);
                currentlyWorking = false;
                markDirty();
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

    @Persisted
    private boolean workingEnabled;

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.workingEnabled == isWorkingAllowed)
            return;
        this.workingEnabled = isWorkingAllowed;
        markDirty();
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

    @Override
    public Widget createUIWidget() {
        /*
          Energy bar aligned with the size of the inventory automatically.
          Essentially multiplies the rows of the inventory by the pixel size of slots and adjusts to fit
          Anchored to the bottom of the sort button.
         */
        int perRow = 9;
        int slots = inventory.getSlots();
        int perCol = Math.max(1, (slots + perRow - 1) / perRow);

        var template = new WidgetGroup(0, 0, 18 * perRow + 8, 18 * perCol + 8);
        template.setBackground(GuiTextures.BACKGROUND_INVERSE);

        for (int i = 0; i < slots; i++) {
            int x = i % perRow;
            int y = i / perRow;
            template.addWidget(new SlotWidget(inventory, i, 4 + x * 18, 4 + y * 18, true, true));
        }

        var editableUI = createEnergyBar();
        var energyBar = editableUI.createDefault();

        int energyBarX = 3, toggleY = 2, toggleH = 18;
        int energyBarY = toggleY + toggleH + 4;

        int gridHeight = template.getSize().height;
        int energyBarHeight = Math.max(0, gridHeight - 20);
        energyBar.setSize(energyBar.getSize().width, energyBarHeight);

        int groupWidth = Math.max(energyBar.getSize().width + template.getSize().width + 4 + 8, 172);
        int groupHeight = Math.max(template.getSize().height + 8, energyBarY + energyBar.getSize().height + 8);
        var group = new WidgetGroup(0, 0, groupWidth, groupHeight);

        energyBar.setSelfPosition(new Position(energyBarX, energyBarY));

        var size = group.getSize();
        int templateX = (size.width - energyBar.getSize().width - 4 - template.getSize().width) / 2
                + 2 + energyBar.getSize().width + 2;
        int templateY = (size.height - template.getSize().height) / 2;
        template.setSelfPosition(new Position(templateX, templateY));

        group.addWidget(energyBar);
        group.addWidget(template);

        {
            IGuiTexture overlayOn = new ResourceTexture("tfg:textures/gui/widgets/unify_dates_on.png");
            IGuiTexture overlayOff = new ResourceTexture("tfg:textures/gui/widgets/unify_dates_off.png");

            var toggle = new ToggleButtonWidget(3, 2, 18, 18,
                    this::isUnifyDatesEnabled,
                    this::setUnifyDatesEnabled) {
                private void refreshTooltip() {
                    String base = "tfg.gui.refrigerator.unify_dates";
                    setTooltipText(Component.translatable(base).getString());
                }

                {
                    IGuiTexture backDisabled = GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5);
                    IGuiTexture backEnabled = GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5);
                    setTexture(new GuiTextureGroup(backDisabled, overlayOff),
                            new GuiTextureGroup(backEnabled, overlayOn));
                    refreshTooltip();
                }

                @Override
                public void drawInForeground(net.minecraft.client.gui.@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                    refreshTooltip();
                    super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
                }
            };

            group.addWidget(toggle);
        }

        editableUI.setupUI(group, this);
        return group;
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
         * @param machine MetaMachine.
         * @param slots   int slots.
         */
        public RefrigeratedStorage(MetaMachine machine, int slots) {
            super(machine, slots, IO.IN, IO.IN);
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
            FoodRefrigeratorMachine.this.markDirty();
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
            FoodRefrigeratorMachine.this.markDirty();
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
                FoodRefrigeratorMachine.this.markDirty();
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
                FoodRefrigeratorMachine.this.markDirty();
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
                    FoodRefrigeratorMachine.this.markDirty();
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
                FoodRefrigeratorMachine.this.markDirty();
                return;
            }

            RefrigeratedStorage.super.setStackInSlot(slot, stack);
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

    // #endregion
}
