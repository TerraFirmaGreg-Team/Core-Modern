package su.terrafirmagreg.core.common.data.container;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Nullable;

import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.common.recipes.inventory.EmptyInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import lombok.Getter;

import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.recipes.SmithingPattern;
import su.terrafirmagreg.core.common.data.recipes.SmithingType;

public class SmithingTableContainer extends Container implements ISlotCallback, ButtonHandlerContainer {
    private final ItemStackHandler inventory;
    private final Inventory playerInventory;
    private final ContainerLevelAccess access;
    private final RecipeHandler recipeHandler;
    //Pattern Related
    @Getter
    private final SmithingPattern pattern;
    @Getter
    private SmithingType currentType;

    public static final int SLOT_TOT = 5;
    public static final int MAT_SLOTA = 0;
    public static final int MAT_SLOTB = 1;
    public static final int TOOL_SLOTA = 2;
    public static final int TOOL_SLOTB = 3;
    public static final int RESULT_SLOT = 4;

    public boolean activeScreen = false;

    private boolean hasConsumedIngredient;

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
        this.recipeHandler = new RecipeHandler(this);

        pattern = new SmithingPattern();
        hasConsumedIngredient = false;

    }

    public ItemStack getInputItem() {
        return inventory.getStackInSlot(MAT_SLOTA);
    }

    public ArrayList<ItemStack> getToolItems() {
        return new ArrayList<>(Arrays.asList(inventory.getStackInSlot(TOOL_SLOTA), inventory.getStackInSlot(TOOL_SLOTB)));
    }

    @Override
    public void setAndUpdateSlots(int slot) {
        ISlotCallback.super.setAndUpdateSlots(slot);
        if (slot != RESULT_SLOT) {

            ItemStack inputItemA = inventory.getStackInSlot(MAT_SLOTA);
            ItemStack inputItemB = inventory.getStackInSlot(MAT_SLOTB);
            ItemStack toolA = inventory.getStackInSlot(TOOL_SLOTA);
            ItemStack toolB = inventory.getStackInSlot(TOOL_SLOTB);

            //the Item B is optional
            if (inputItemA.isEmpty() || toolA.isEmpty() || toolB.isEmpty())
                return;

            if (!activeScreen) {
                for (SmithingType type : SmithingType.SMITHING_TYPES) {
                    Item testItem1 = type.getInputItems().get(0).getItem();
                    Item testItem2 = type.getInputItems().get(1).getItem();

                    if (!((inputItemA.is(testItem1) || inputItemB.is(testItem1)) &&
                            (inputItemA.is(testItem2) || inputItemB.is(testItem2))))
                        continue;

                    TagKey<Item> testTool1 = type.getToolTags().get(0);
                    TagKey<Item> testTool2 = type.getToolTags().get(1);

                    if ((toolA.is(testTool1) || toolB.is(testTool1)) &&
                            (toolA.is(testTool2) || toolB.is(testTool2))) {
                        activeScreen = true;
                        this.currentType = type;
                        hasConsumedIngredient = false;
                    }
                }
            }
        }
    }

    @Override
    public void onSlotTake(Player player, int slot, ItemStack stack) {
        if (slot == RESULT_SLOT) {
            resetPattern();

            if (!hasConsumedIngredient) {
                this.getInputItem().shrink(1);
                hasConsumedIngredient = true;
            }
        } else {
            resetPattern();
        }
    }

    private void resetPattern() {
        pattern.setAll(true);
        activeScreen = false;
    }

    public void setScreenState(boolean value) {
        this.activeScreen = value;
    }

    public boolean getScreenState() {
        return activeScreen;
    }

    //Button Handler
    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
        // Set the matching patterns slot to clicked
        pattern.set(buttonID, false);

        //System.out.println("Button");
        // Update the output slot based on the recipe
        final Slot slot = slots.get(RESULT_SLOT);

        if (player.level() instanceof ServerLevel level) {
            ItemStack resultStack = level.getRecipeManager().getRecipeFor(TFGRecipeTypes.SMITHING.get(), recipeHandler, level)
                    .map(recipe -> recipe.assemble(recipeHandler, level.registryAccess()))
                    .orElse(ItemStack.EMPTY);

            System.out.println(resultStack);
            slot.set(resultStack);
        }
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex) {
        return switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, MAT_SLOTA, SLOT_TOT, false);
            case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
        };
    }

    @Override
    protected void addContainerSlots() {
        super.addContainerSlots();
        addSlot(new SmithingInputSlot(this, inventory, MAT_SLOTA, 123, 25));
        addSlot(new SmithingInputSlot(this, inventory, MAT_SLOTB, 123, 46));
        addSlot(new SmithingInputSlot(this, inventory, TOOL_SLOTA, 145, 25));
        addSlot(new SmithingInputSlot(this, inventory, TOOL_SLOTB, 145, 46));
        addSlot(new SmithingInputSlot(this, inventory, RESULT_SLOT, 134, 72));
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
            case MAT_SLOTA, MAT_SLOTB -> validInput(stack);
            case TOOL_SLOTA, TOOL_SLOTB -> isTool(stack);
            default -> slot != RESULT_SLOT;
        };
    }

    private static boolean isTool(ItemStack item) {
        return item.is(TFGTags.Items.SmithingTools);
    }

    private static boolean validInput(ItemStack item) {
        return item.is(TFGTags.Items.SmithingTableInputs);
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return switch (slot) {
            case MAT_SLOTA, MAT_SLOTB -> 64;
            case TOOL_SLOTA, TOOL_SLOTB -> 1;
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

    public record RecipeHandler(SmithingTableContainer container) implements EmptyInventory {
    }
}
