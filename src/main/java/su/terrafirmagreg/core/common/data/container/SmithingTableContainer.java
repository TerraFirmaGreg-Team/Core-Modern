package su.terrafirmagreg.core.common.data.container;

import javax.annotation.Nullable;

import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.common.container.ISlotCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGTags;

public class SmithingTableContainer extends Container implements ISlotCallback, ButtonHandlerContainer {
    private final ItemStackHandler inventory;
    private final Inventory playerInventory;
    private final ContainerLevelAccess access;

    public static final int SLOT_TOT = 3;
    public static final int MAT_SLOT = 0;
    public static final int TOOL_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    public static SmithingTableContainer create(Inventory playerInventory, int windowId, ContainerLevelAccess access) {
        return new SmithingTableContainer(playerInventory, windowId, access).init(playerInventory, 19);
    }

    public SmithingTableContainer(Inventory playerInventory, int windowId) {
        this(playerInventory, windowId, ContainerLevelAccess.NULL);
    }

    public SmithingTableContainer(Inventory playerInventory, int windowId, ContainerLevelAccess access) {
        super(TFGContainers.SMITHING_TABLE.get(), windowId);
        this.playerInventory = playerInventory;
        this.access = access;
        this.inventory = new InventoryItemHandler(this, SLOT_TOT);

    }

    //Button Handler
    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
        System.out.println("button");
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex) {
        return switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, MAT_SLOT, SLOT_TOT, false);
            case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
        };
    }

    @Override
    protected void addContainerSlots() {
        super.addContainerSlots();
        addSlot(new SmithingInputSlot(this, inventory, MAT_SLOT, 128, 67));
        addSlot(new SmithingInputSlot(this, inventory, TOOL_SLOT, 149, 67));
        addSlot(new SmithingInputSlot(this, inventory, RESULT_SLOT, 128, 45));
    }

    @Override
    public void removed(Player player) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (slot != RESULT_SLOT) {
                final ItemStack stack = inventory.getStackInSlot(slot);
                giveItemStackToPlayerOrDrop(player, stack);
            }
        }
        super.removed(player);
    }

    //Valid Items in Slot Handlers
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case (MAT_SLOT) -> validInput(stack);
            case (TOOL_SLOT) -> isTool(stack);
            default -> slot != RESULT_SLOT;
        };
    }

    private static boolean isTool(ItemStack item) {
        return item.is(CustomTags.HAMMERS);
    }

    private static boolean validInput(ItemStack item) {
        return item.is(TFGTags.Items.SmithingTableInputs);
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return switch (slot) {
            case MAT_SLOT -> 8;
            case TOOL_SLOT -> 1;
            default -> 64;
        };
    }

    //Special Callback Slot
    public boolean canPickup(int slot) {
        return canPickup(inventory.getStackInSlot(slot));
    }

    public boolean canPickup(ItemStack item) {
        return true;
    }

    public static class SmithingInputSlot extends CallbackSlot {
        private final SmithingTableContainer callback;

        public SmithingInputSlot(SmithingTableContainer callback, IItemHandler inventory, int index, int x, int y) {
            super(callback, inventory, index, x, y);
            this.callback = callback;
        }

        @Override
        public boolean mayPickup(Player player) {
            return callback.canPickup(getItem()) && super.mayPickup(player);
        }
    }
}
