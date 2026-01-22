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

    public ArtisanTableBlockEntity(BlockPos pos, BlockState state) {
        super(TFGBlockEntities.ARTISAN_TABLE.get(), pos, state, ArtisanTableBlockEntity::createInventory, NAME);
        this.pattern = new ArtisanPattern();
    }

    private static InventoryItemHandler createInventory(InventoryBlockEntity<?> entity) {
        return new InventoryItemHandler(entity, SLOT_TOT);
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    public ArrayList<ItemStack> getInputItems() {
        return new ArrayList<>(Arrays.asList(inventory.getStackInSlot(MAT_SLOTA), inventory.getStackInSlot(MAT_SLOTB)));
    }

    public ArrayList<ItemStack> getToolItems() {
        return new ArrayList<>(Arrays.asList(inventory.getStackInSlot(TOOL_SLOTA), inventory.getStackInSlot(TOOL_SLOTB)));
    }

    @Override
    public int getSlotStackLimit(int slot) {
        return switch (slot) {
            case MAT_SLOTA, MAT_SLOTB -> 64;
            case TOOL_SLOTA, TOOL_SLOTB -> 1;
            case RESULT_SLOT -> 64;
            default -> 64;
        };
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return switch (slot) {
            case MAT_SLOTA, MAT_SLOTB -> stack.is(TFGTags.Items.SmithingTableInputs);
            case TOOL_SLOTA, TOOL_SLOTB -> stack.is(TFGTags.Items.SmithingTools);
            default -> false;
        };
    }

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

    // Make this public so the container can trigger the drawing interface after output is taken
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
            Item testItem1 = type.getInputItems().get(0).getItem();
            Item testItem2 = type.getInputItems().size() > 1 ? type.getInputItems().get(1).getItem() : null;

            boolean inputMatch;
            if (testItem2 != null) {
                inputMatch = (inputItemA.is(testItem1) || inputItemB.is(testItem1)) &&
                        (inputItemA.is(testItem2) || inputItemB.is(testItem2));
            } else {
                inputMatch = inputItemA.is(testItem1) || inputItemB.is(testItem1);
            }

            if (!inputMatch)
                continue;

            TagKey<Item> testTool1 = type.getToolTags().get(0);
            TagKey<Item> testTool2 = type.getToolTags().get(1);

            if ((toolA.is(testTool1) || toolB.is(testTool1)) &&
                    (toolA.is(testTool2) || toolB.is(testTool2))) {
                activeScreen = true;
                this.currentType = type;
                hasConsumedIngredient = false;
                return;
            }
        }
    }

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
        var requiredInputs = currentType.getInputItems();
        ArrayList<ItemStack> inputStacks = new ArrayList<>();
        inputStacks.add(inputA);
        inputStacks.add(inputB);
        for (ItemStack required : requiredInputs) {
            int requiredCount = required.getCount();
            int found = 0;
            for (ItemStack input : inputStacks) {
                if (!input.isEmpty() && input.is(required.getItem())) {
                    found += input.getCount();
                }
            }
            if (found < requiredCount) {
                return false;
            }
        }
        return true;
    }

    public boolean canConsumeIngredients() {
        if (currentType == null)
            return false;

        ItemStack inputA = inventory.getStackInSlot(MAT_SLOTA);
        ItemStack inputB = inventory.getStackInSlot(MAT_SLOTB);
        ItemStack toolA = inventory.getStackInSlot(TOOL_SLOTA);
        ItemStack toolB = inventory.getStackInSlot(TOOL_SLOTB);

        return areInputsValidForCurrentType(inputA, inputB, toolA, toolB);
    }

    public void resetPattern() {
        pattern.setAll(true);
        activeScreen = false;
        hasConsumedIngredient = false;
        inventory.setStackInSlot(RESULT_SLOT, ItemStack.EMPTY);
        markForSync();
    }

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

    public void ejectInventory() {
        assert this.level != null;

        for (int i = 0; i < RESULT_SLOT; ++i) {
            ItemStack stack = Helpers.removeStack(this.inventory, i);
            if (!stack.isEmpty()) {
                Helpers.spawnItem(this.level, this.worldPosition, stack, 0.7);
            }
        }

    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inv, @NotNull Player player) {
        return ArtisanTableContainer.create(this, inv, windowId);
    }

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
