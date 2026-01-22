package su.terrafirmagreg.core.common.data.container;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.recipes.inventory.EmptyInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.blockentity.ArtisanTableBlockEntity;
import su.terrafirmagreg.core.common.data.recipes.ArtisanPattern;
import su.terrafirmagreg.core.common.data.recipes.ArtisanType;

public class ArtisanTableContainer extends BlockEntityContainer<ArtisanTableBlockEntity> implements ButtonHandlerContainer {
    public static final int SLOT_TOT = ArtisanTableBlockEntity.SLOT_TOT;
    public static final int MAT_SLOTA = ArtisanTableBlockEntity.MAT_SLOTA;
    public static final int MAT_SLOTB = ArtisanTableBlockEntity.MAT_SLOTB;
    public static final int TOOL_SLOTA = ArtisanTableBlockEntity.TOOL_SLOTA;
    public static final int TOOL_SLOTB = ArtisanTableBlockEntity.TOOL_SLOTB;
    public static final int RESULT_SLOT = ArtisanTableBlockEntity.RESULT_SLOT;

    public static ArtisanTableContainer create(ArtisanTableBlockEntity blockEntity, Inventory playerInventory, int windowId) {
        return new ArtisanTableContainer(blockEntity, playerInventory, windowId).init(playerInventory, 19);
    }

    public ArtisanTableContainer(ArtisanTableBlockEntity blockEntity, Inventory playerInventory, int windowId) {
        super(TFGContainers.ARTISAN_TABLE.get(), windowId, blockEntity);
        this.activeScreen = blockEntity.isActiveScreen();
    }

    public ArtisanPattern getPattern() {
        return blockEntity.getPattern();
    }

    public ArtisanType getCurrentType() {
        return blockEntity.getCurrentType();
    }

    public boolean getScreenState() {
        return blockEntity.isActiveScreen();
    }

    public void setScreenState(boolean value) {
        blockEntity.setActiveScreen(value);
    }

    public ArrayList<ItemStack> getInputItems() {
        return blockEntity.getInputItems();
    }

    public ArrayList<ItemStack> getToolItems() {
        return blockEntity.getToolItems();
    }

    public boolean activeScreen = false;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.activeScreen = blockEntity.isActiveScreen();
    }

    //Button Handler
    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
        // Set the matching patterns slot to clicked
        blockEntity.getPattern().set(buttonID, false);

        // Update the output slot based on the recipe
        final Slot slot = slots.get(RESULT_SLOT);
        RecipeHandler handler = new RecipeHandler(this);
        if (player.level() instanceof ServerLevel level) {
            ItemStack resultStack = level.getRecipeManager().getRecipeFor(TFGRecipeTypes.ARTISAN.get(), handler, level)
                    .map(recipe -> recipe.assemble(handler, level.registryAccess()))
                    .orElse(ItemStack.EMPTY);

            slot.set(resultStack);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
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
        addSlot(new SmithingInputSlot(this, blockEntity, MAT_SLOTA, 123, 25));
        addSlot(new SmithingInputSlot(this, blockEntity, MAT_SLOTB, 123, 46));
        addSlot(new SmithingInputSlot(this, blockEntity, TOOL_SLOTA, 145, 25));
        addSlot(new SmithingInputSlot(this, blockEntity, TOOL_SLOTB, 145, 46));
        addSlot(new ResultSlot(this, blockEntity, RESULT_SLOT, 134, 72));
    }

    public static class ResultSlot extends CallbackSlot {
        private final ArtisanTableBlockEntity blockEntity;
        private final ArtisanTableContainer container;

        public ResultSlot(ArtisanTableContainer container, ArtisanTableBlockEntity blockEntity, int index, int x, int y) {
            super(blockEntity, blockEntity.getInventory(), index, x, y);
            this.blockEntity = blockEntity;
            this.container = container;
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack result = getItem();
            if (result.isEmpty()) {
                return false;
            }

            if (blockEntity.isHasConsumedIngredient()) {
                return true;
            }

            return blockEntity.canConsumeIngredients();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            blockEntity.damageTools(player);
            if (!blockEntity.isHasConsumedIngredient()) {
                blockEntity.consumeItems();
                blockEntity.resetPattern();
            }
            super.onTake(player, stack);

            if (blockEntity.canConsumeIngredients()) {
                blockEntity.resetPattern();
                blockEntity.checkForActiveScreen();
                blockEntity.markForSync();
                blockEntity.setChanged();
            }
        }
    }

    public static class SmithingInputSlot extends CallbackSlot {
        private final ArtisanTableBlockEntity blockEntity;

        public SmithingInputSlot(ArtisanTableContainer container, ArtisanTableBlockEntity blockEntity, int index, int x, int y) {
            super(blockEntity, blockEntity.getInventory(), index, x, y);
            this.blockEntity = blockEntity;
        }

        @Override
        public boolean mayPickup(Player player) {
            return super.mayPickup(player);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !blockEntity.isActiveScreen() && super.mayPlace(stack);
        }
    }

    public record RecipeHandler(ArtisanTableContainer container) implements EmptyInventory {
    }
}
