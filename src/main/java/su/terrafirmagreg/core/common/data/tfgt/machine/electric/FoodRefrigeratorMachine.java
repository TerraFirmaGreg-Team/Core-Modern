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

public class FoodRefrigeratorMachine extends TieredEnergyMachine
        implements IControllable, IFancyUIMachine, IMachineLife {

    public static int INVENTORY_SIZE(int tier) {
        return 9 * tier;
    }

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

    @Getter
    @Persisted
    private boolean unifyDatesEnabled = true;

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
        clearInventory(inventory);
    }

    public void updateSubscription() {
        if (workingEnabled && consumeEnergy(true) && !inventory.isEmpty()) {
            if (!currentlyWorking) {
                inventory.changeTraitForAll(true);
                currentlyWorking = true;
            }

            tickSubscription = subscribeServerTick(tickSubscription, this::tick);
        } else {
            if (currentlyWorking) {
                inventory.changeTraitForAll(false);
                currentlyWorking = false;
            }
            if (tickSubscription != null) {
                tickSubscription.unsubscribe();
                tickSubscription = null;
            }
        }
    }

    public void tick() {
        if (workingEnabled && !inventory.isEmpty())
            consumeEnergy(false);

        updateSubscription();
    }

    private long getEnergyAmount() {
        // 1A of LV per inventory row
        return (long) GTValues.VA[GTValues.LV] * tier;
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
        workingEnabled = isWorkingAllowed;
        updateSubscription();
    }

    public void setUnifyDatesEnabled(boolean enabled) {
        if (this.unifyDatesEnabled == enabled)
            return;
        this.unifyDatesEnabled = enabled;

        if (!isRemote() && enabled && currentlyWorking) {
            inventory.unifyFoodDates();
            inventory.combineStacks();
            inventory.compactInventory();
            inventory.onContentsChanged();
        }
        updateSubscription();
    }

    // #endregion

    // #region GUI

    @Override
    public Widget createUIWidget() {
        int perRow = 9;
        int perCol = inventorySize / 9;

        var template = new WidgetGroup(0, 0, 18 * perRow + 8, 18 * perCol + 8);
        template.setBackground(GuiTextures.BACKGROUND_INVERSE);
        int index = 0;
        for (int y = 0; y < perCol; y++) {
            for (int x = 0; x < perRow; x++) {
                template.addWidget(new SlotWidget(inventory, index++, 4 + x * 18, 4 + y * 18, true, true));
            }
        }
        var editableUI = createEnergyBar();
        var energyBar = editableUI.createDefault();
        var group = new WidgetGroup(0, 0, Math.max(energyBar.getSize().width + template.getSize().width + 4 + 8, 172),
                Math.max(template.getSize().height + 8, energyBar.getSize().height + 8));
        var size = group.getSize();
        energyBar.setSelfPosition(new Position(3, (size.height - energyBar.getSize().height) / 2));
        template.setSelfPosition(
                new Position((size.width - energyBar.getSize().width - 4 - template.getSize().width) / 2 + 2
                        + energyBar.getSize().width + 2, (size.height - template.getSize().height) / 2));
        group.addWidget(energyBar);
        group.addWidget(template);

        {
            IGuiTexture overlayOn = new ResourceTexture("tfg:textures/gui/widgets/unify_dates_on.png");
            IGuiTexture overlayOff = new ResourceTexture("tfg:textures/gui/widgets/unify_dates_off.png");

            var toggle = new ToggleButtonWidget(4, 2, 18, 18,
                    this::isUnifyDatesEnabled,
                    this::setUnifyDatesEnabled
            ) {
                private void refreshTooltip() {
                    String base = "tfg.gui.refrigerator.unify_dates";
                    setTooltipText(Component.translatable(base).getString());
                }

                {
                    IGuiTexture backDisabled = GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5);
                    IGuiTexture backEnabled = GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5);

                    setTexture(
                            new GuiTextureGroup(backDisabled, overlayOff),
                            new GuiTextureGroup(backEnabled, overlayOn)
                    );
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
    public class RefrigeratedStorage extends NotifiableItemStackHandler {

        private boolean internalEdit = false;

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
            return food != null && !food.isRotten();
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
            if (incoming == null || incoming.isRotten())
                return stack;

            ItemStack toInsert = stack.copy();
            if (currentlyWorking)
                FoodCapability.applyTrait(toInsert, TFGFoodTraits.REFRIGERATING);

            ItemStack result = storage.insertItem(slot, toInsert, simulate);

            if (!simulate) {
                unifyFoodDates();
                combineStacks();
                compactInventory();
                onContentsChanged();
                updateSubscription();
            }

            if (currentlyWorking)
                FoodCapability.removeTrait(result, TFGFoodTraits.REFRIGERATING);
            return result;
        }

        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0)
                return ItemStack.EMPTY;

            ItemStack result = storage.extractItem(slot, amount, simulate);

            FoodCapability.removeTrait(result, TFGFoodTraits.REFRIGERATING);

            if (!simulate) {
                unifyFoodDates();
                combineStacks();
                compactInventory();
                onContentsChanged();
                updateSubscription();
            }
            return result;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (!internalEdit) {
                if (stack.isEmpty()) {
                    RefrigeratedStorage.super.setStackInSlot(slot, ItemStack.EMPTY);
                    unifyFoodDates();
                    combineStacks();
                    compactInventory();
                    updateSubscription();
                    return;
                }

                IFood food = FoodCapability.get(stack);
                if (food == null || food.isRotten())
                    return;

                ItemStack toSet = stack;
                if (currentlyWorking) {
                    toSet = stack.copy();
                    FoodCapability.applyTrait(toSet, TFGFoodTraits.REFRIGERATING);
                }

                RefrigeratedStorage.super.setStackInSlot(slot, toSet);

                unifyFoodDates();
                combineStacks();
                compactInventory();
                updateSubscription();
                return;
            }

            RefrigeratedStorage.super.setStackInSlot(slot, stack);
        }
    }

    // #endregion
}
