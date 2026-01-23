package su.terrafirmagreg.core.common.data.blockentity;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import lombok.Setter;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlockEntities;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.container.ArtisanTableContainer;
import su.terrafirmagreg.core.common.data.recipes.ArtisanPattern;
import su.terrafirmagreg.core.common.data.recipes.ArtisanType;

/**
 * Block entity for the Artisan Table.
 * Handles inventory, pattern, recipe types, and crafting logic.
 */
public class ArtisanTableBlockEntity extends InventoryBlockEntity<InventoryItemHandler> {

    public static final int SLOT_TOT = 5;
    public static final int MAT_SLOTA = 0;
    public static final int MAT_SLOTB = 1;
    public static final int TOOL_SLOTA = 2;
    public static final int TOOL_SLOTB = 3;
    public static final int RESULT_SLOT = 4;

    private static final Component NAME = Component.translatable(TFGCore.MOD_ID + ".block_entity.artisan_table");

    @Getter
    private final ArtisanPattern pattern;

    @Getter
    @Setter
    private ArtisanType currentType;

    @Getter
    @Setter
    private boolean activeScreen = false;

    @Getter
    @Setter
    private boolean hasConsumedIngredient = false;

    private boolean isLoading = false;

    /**
     * Constructs a new ArtisanTableBlockEntity.
     * @param pos   The block position.
     * @param state The block state.
     */
    public ArtisanTableBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.ARTISAN_TABLE.get(), pos, state, ArtisanTableBlockEntity::createInventory, NAME);
        this.pattern = new ArtisanPattern();
    }

    /**
     * Creates the inventory handler.
     * @param entity The block entity.
     * @return The inventory item handler.
     */
    private static InventoryItemHandler createInventory(InventoryBlockEntity<?> entity) {
        return new InventoryItemHandler(entity, SLOT_TOT);
    }

    /**
     * @return The inventory handler.
     */
    public IItemHandler getInventory() {
        return inventory;
    }

    /**
     * @return A list containing the input item stacks.
     */
    public ArrayList<ItemStack> getInputItems() {
        return new ArrayList<>(Arrays.asList(inventory.getStackInSlot(MAT_SLOTA), inventory.getStackInSlot(MAT_SLOTB)));
    }

    /**
     * @return A list containing the tool item stacks.
     */
    public ArrayList<ItemStack> getToolItems() {
        return new ArrayList<>(Arrays.asList(inventory.getStackInSlot(TOOL_SLOTA), inventory.getStackInSlot(TOOL_SLOTB)));
    }

    /**
     * Gets the stack limit for a given slot.
     * @param slot The slot index.
     * @return The stack limit.
     */
    @Override
    public int getSlotStackLimit(int slot) {
        return switch (slot) {
            case MAT_SLOTA, MAT_SLOTB -> 64;
            case TOOL_SLOTA, TOOL_SLOTB -> 1;
            case RESULT_SLOT -> 64;
            default -> 64;
        };
    }

    /**
     * Checks if an item is valid for a given slot.
     * @param slot  The slot index.
     * @param stack The item stack.
     * @return True if the item is valid for the slot.
     */
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case MAT_SLOTA, MAT_SLOTB -> stack.is(TFGTags.Items.SmithingTableInputs);
            case TOOL_SLOTA, TOOL_SLOTB -> stack.is(TFGTags.Items.SmithingTools);
            default -> false;
        };
    }

    /**
     * @param slot The slot index.
     */
    @Override
    public void setAndUpdateSlots(int slot) {
        super.setAndUpdateSlots(slot);
        if (slot != RESULT_SLOT && !isLoading) {
            checkForActiveScreen();
        }
        if (!isLoading) {
            markForSync();
        }
    }

    /**
     * Checks and updates whether the artisan table should display the active screen.
     * Resets the pattern if the inputs are no longer valid.
     */
    public void checkForActiveScreen() {
        ItemStack inputItemA = inventory.getStackInSlot(MAT_SLOTA);
        ItemStack inputItemB = inventory.getStackInSlot(MAT_SLOTB);
        ItemStack toolA = inventory.getStackInSlot(TOOL_SLOTA);
        ItemStack toolB = inventory.getStackInSlot(TOOL_SLOTB);

        if (activeScreen && currentType != null) {
            if (!areInputsValidForCurrentType(inputItemA, inputItemB, toolA, toolB)) {
                resetPattern();
                return;
            }
            return;
        }

        if (inputItemA.isEmpty() || toolA.isEmpty() || toolB.isEmpty()) {
            return;
        }

        for (ArtisanType type : ArtisanType.ARTISAN_TYPES.values()) {
            this.currentType = type;
            if (areInputsValidForCurrentType(inputItemA, inputItemB, toolA, toolB)) {
                activeScreen = true;
                hasConsumedIngredient = false;
                return;
            }
        }
        this.currentType = null;
    }

    /**
     * Checks if the current inputs are valid for the current recipe type.
     * @param inputA The first input item stack.
     * @param inputB The second input item stack.
     * @param toolA  The first tool item stack.
     * @param toolB  The second tool item stack.
     * @return True if the inputs are valid for the current type.
     */
    private boolean areInputsValidForCurrentType(ItemStack inputA, ItemStack inputB, ItemStack toolA, ItemStack toolB) {
        if (currentType == null)
            return false;

        TagKey<Item> testTool1 = currentType.getToolTags().get(0);
        TagKey<Item> testTool2 = currentType.getToolTags().get(1);
        boolean toolsValid = !toolA.isEmpty() && !toolB.isEmpty() &&
                ((toolA.is(testTool1) || toolB.is(testTool1)) &&
                        (toolA.is(testTool2) || toolB.is(testTool2)));
        if (!toolsValid)
            return false;
        var requiredInputs = new ArrayList<>(currentType.getInputItems());
        int[] needed = new int[requiredInputs.size()];
        for (int i = 0; i < requiredInputs.size(); i++) {
            needed[i] = requiredInputs.get(i).getCount();
        }
        ItemStack[] inputStacks = { inputA, inputB };
        for (ItemStack input : inputStacks) {
            if (input.isEmpty())
                continue;
            for (int i = 0; i < requiredInputs.size(); i++) {
                ItemStack required = requiredInputs.get(i);
                if (needed[i] > 0 && input.is(required.getItem())) {
                    int toUse = Math.min(input.getCount(), needed[i]);
                    needed[i] -= toUse;
                }
            }
        }
        for (int n : needed) {
            if (n > 0)
                return false;
        }
        return true;
    }

    /**
     * @return True if the ingredients can be consumed.
     */
    public boolean canConsumeIngredients() {
        if (currentType == null)
            return false;

        ItemStack inputA = inventory.getStackInSlot(MAT_SLOTA);
        ItemStack inputB = inventory.getStackInSlot(MAT_SLOTB);
        ItemStack toolA = inventory.getStackInSlot(TOOL_SLOTA);
        ItemStack toolB = inventory.getStackInSlot(TOOL_SLOTB);

        return areInputsValidForCurrentType(inputA, inputB, toolA, toolB);
    }

    /**
     * Resets the artisan pattern and screen state.
     */
    public void resetPattern() {
        pattern.setAll(true);
        activeScreen = false;
        hasConsumedIngredient = false;
        inventory.setStackInSlot(RESULT_SLOT, ItemStack.EMPTY);
        markForSync();
    }

    /**
     * Consumes the required input items from the inventory for the current recipe.
     */
    public void consumeItems() {
        if (currentType == null)
            return;

        ItemStack matA = inventory.getStackInSlot(MAT_SLOTA);
        ItemStack matB = inventory.getStackInSlot(MAT_SLOTB);
        ArrayList<ItemStack> ingredients = currentType.getInputItems();

        for (ItemStack stack : ingredients) {
            if (stack.is(matA.getItem())) {
                matA.shrink(stack.getCount());
            } else if (stack.is(matB.getItem())) {
                matB.shrink(stack.getCount());
            }
        }
        markForSync();
    }

    /**
     * Damages the tools used in crafting, breaking them if necessary.
     * @param player The player using the tools.
     */
    public void damageTools(@Nullable Player player) {
        ItemStack toolA = inventory.getStackInSlot(TOOL_SLOTA);
        ItemStack toolB = inventory.getStackInSlot(TOOL_SLOTB);

        if (!toolA.isEmpty() && toolA.isDamageableItem()) {
            assert player != null;
            toolA.hurtAndBreak(1, player, (p) -> inventory.setStackInSlot(TOOL_SLOTA, ItemStack.EMPTY));
        }

        if (!toolB.isEmpty() && toolB.isDamageableItem()) {
            assert player != null;
            toolB.hurtAndBreak(1, player, (p) -> inventory.setStackInSlot(TOOL_SLOTB, ItemStack.EMPTY));
        }
        markForSync();
        setChanged();
    }

    /**
     * Ejects all inventory items (except the result slot) into the world.
     * Called when the block is destroyed.
     */
    public void ejectInventory() {
        assert this.level != null;

        for (int i = 0; i < RESULT_SLOT; ++i) {
            ItemStack stack = Helpers.removeStack(this.inventory, i);
            if (!stack.isEmpty()) {
                Helpers.spawnItem(this.level, this.worldPosition, stack, 0.7);
            }
        }

    }

    /**
     * Creates the ui for the artisan table.
     * @param windowId The window ID.
     * @param inv      The player inventory.
     * @param player   The player.
     * @return The container menu.
     */
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inv, @NotNull Player player) {
        return ArtisanTableContainer.create(this, inv, windowId);
    }

    /**
     * @param tag The NBT tag.
     */
    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("patternData", pattern.getData());
        tag.putBoolean("activeScreen", activeScreen);
        tag.putBoolean("hasConsumedIngredient", hasConsumedIngredient);
        if (currentType != null) {
            tag.putString("currentType", currentType.getId().toString());
        }
    }

    /**
     * @param tag The NBT tag.
     */
    @Override
    public void loadAdditional(CompoundTag tag) {
        isLoading = true;

        if (tag.contains("patternData")) {
            long data = tag.getLong("patternData");
            for (int i = 0; i < 36; i++) {
                pattern.set(i, ((data >> i) & 0b1) == 1);
            }
        }
        activeScreen = tag.getBoolean("activeScreen");
        hasConsumedIngredient = tag.getBoolean("hasConsumedIngredient");
        if (tag.contains("currentType")) {
            String typeId = tag.getString("currentType");
            currentType = ArtisanType.ARTISAN_TYPES.get(TFGCore.id(typeId.replace(TFGCore.MOD_ID + ":", "")));
        }

        super.loadAdditional(tag);

        isLoading = false;
    }
}
