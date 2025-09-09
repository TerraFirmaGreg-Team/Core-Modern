package su.terrafirmagreg.core.common.data.tfgt.machine.electric;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

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
        editableUI.setupUI(group, this);
        return group;
    }

    // #endregion

    // #region Refrigerated trait
    public class RefrigeratedStorage extends NotifiableItemStackHandler {

        public RefrigeratedStorage(MetaMachine machine, int slots) {
            super(machine, slots, IO.IN, IO.IN);
        }

        public void changeTraitForAll(boolean add) {
            for (int i = 0; i < storage.getSlots(); i++) {
                var stack = storage.getStackInSlot(i).copy();
                if (stack.isEmpty())
                    continue;

                if (add) {
                    FoodCapability.applyTrait(stack, TFGFoodTraits.REFRIGERATING);
                } else {
                    FoodCapability.removeTrait(stack, TFGFoodTraits.REFRIGERATING);
                }
                storage.setStackInSlot(i, stack);
            }
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty())
                return ItemStack.EMPTY;
            if (currentlyWorking)
                FoodCapability.applyTrait(stack, TFGFoodTraits.REFRIGERATING);

            var result = storage.insertItem(slot, stack, simulate);
            updateSubscription();
            FoodCapability.removeTrait(result, TFGFoodTraits.REFRIGERATING);
            return result;
        }

        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0)
                return ItemStack.EMPTY;

            var result = storage.extractItem(slot, amount, simulate);
            FoodCapability.removeTrait(result, TFGFoodTraits.REFRIGERATING);
            updateSubscription();
            return result;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (currentlyWorking)
                FoodCapability.applyTrait(stack, TFGFoodTraits.REFRIGERATING);

            FoodCapability.removeTrait(storage.getStackInSlot(slot), TFGFoodTraits.REFRIGERATING);
            storage.setStackInSlot(slot, stack);
            updateSubscription();
        }

    }

    // #endregion
}
