package su.terrafirmagreg.core.common.data.container;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.recipes.inventory.EmptyInventory;
import net.minecraft.core.particles.ParticleTypes;
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

/**
 * Container for the Artisan Table block entity.
 */
public class ArtisanTableContainer extends BlockEntityContainer<ArtisanTableBlockEntity> implements ButtonHandlerContainer {
    public static final int SLOT_TOT = ArtisanTableBlockEntity.SLOT_TOT;
    public static final int MAT_SLOTA = ArtisanTableBlockEntity.MAT_SLOTA;
    public static final int MAT_SLOTB = ArtisanTableBlockEntity.MAT_SLOTB;
    public static final int TOOL_SLOTA = ArtisanTableBlockEntity.TOOL_SLOTA;
    public static final int TOOL_SLOTB = ArtisanTableBlockEntity.TOOL_SLOTB;
    public static final int RESULT_SLOT = ArtisanTableBlockEntity.RESULT_SLOT;
    // Sets the gap between vertical sections of the GUI.
    public static final int SCREEN_SPACING = 5;

    /**
     * Initializes a new ArtisanTableContainer.
     * @param blockEntity The artisan table block entity.
     * @param playerInventory The player's inventory.
     * @param windowId The window ID.
     * @return The initialized ArtisanTableContainer.
     */
    public static ArtisanTableContainer create(ArtisanTableBlockEntity blockEntity, Inventory playerInventory, int windowId) {
        return new ArtisanTableContainer(blockEntity, playerInventory, windowId).init(playerInventory, 19 + SCREEN_SPACING + SCREEN_SPACING);
    }

    /**
     * Constructs a new ArtisanTableContainer.
     * @param blockEntity The artisan table block entity.
     * @param playerInventory The player's inventory.
     * @param windowId The window ID.
     */
    public ArtisanTableContainer(ArtisanTableBlockEntity blockEntity, Inventory playerInventory, int windowId) {
        super(TFGContainers.ARTISAN_TABLE.get(), windowId, blockEntity);
        this.activeScreen = blockEntity.isActiveScreen();
    }

    /**
     * @return The artisan pattern.
     */
    public ArtisanPattern getPattern() {
        return blockEntity.getPattern();
    }

    /**
     * @return The artisan type.
     */
    public ArtisanType getCurrentType() {
        return blockEntity.getCurrentType();
    }

    /**
     * @return True if the artisan table is in the active screen state.
     */
    public boolean getScreenState() {
        return blockEntity.isActiveScreen();
    }

    /**
     * @param value The new screen state.
     */
    public void setScreenState(boolean value) {
        blockEntity.setActiveScreen(value);
    }

    /**
     * @return The list of input item stacks.
     */
    public ArrayList<ItemStack> getInputItems() {
        return blockEntity.getInputItems();
    }

    /**
     * @return The list of tool item stacks.
     */
    public ArrayList<ItemStack> getToolItems() {
        return blockEntity.getToolItems();
    }

    public boolean activeScreen = false;

    /**
     * Broadcasts changes to the container and updates the active screen state.
     */
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.activeScreen = blockEntity.isActiveScreen();
    }

    /**
     * Handles button presses in the GUI.
     * Updates the artisan pattern and output slot.
     * @param buttonID The ID of the pressed button.
     * @param extraNBT Additional data.
     */
    @Override
    public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
        // Set the matching patterns slot to clicked
        blockEntity.getPattern().set(buttonID, false);

        // Update the output slot based on the recipe
        final Slot slot = slots.get(RESULT_SLOT);
        RecipeHandler handler = new RecipeHandler(this);
        assert player != null;
        if (player.level() instanceof ServerLevel level) {
            ItemStack resultStack = level.getRecipeManager().getRecipeFor(TFGRecipeTypes.ARTISAN.get(), handler, level)
                    .map(recipe -> recipe.assemble(handler, level.registryAccess()))
                    .orElse(ItemStack.EMPTY);

            slot.set(resultStack);

            var pos = blockEntity.getBlockPos();
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;
            level.sendParticles(ParticleTypes.CRIT, x, y, z, 3, 0.5, 0.3, 0.5, 0.3);

        }
    }

    /**
     * Called when the container is closed.
     * @param player The player closing the container.
     */
    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
    }

    /**
     * Handles moving item stacks between slots.
     * @param stack The item stack to move.
     * @param slotIndex The index of the slot.
     * @return True if the move was successful.
     */
    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
        return switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, MAT_SLOTA, SLOT_TOT, false);
            case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
        };
    }

    /**
     * Adds the container's custom slots (inputs, tools, output).
     */
    @Override
    protected void addContainerSlots() {
        super.addContainerSlots();
        addSlot(new SmithingInputSlot(this, blockEntity, MAT_SLOTA, 123, 25 + SCREEN_SPACING));
        addSlot(new SmithingInputSlot(this, blockEntity, MAT_SLOTB, 123, 46 + SCREEN_SPACING));
        addSlot(new SmithingInputSlot(this, blockEntity, TOOL_SLOTA, 145, 25 + SCREEN_SPACING));
        addSlot(new SmithingInputSlot(this, blockEntity, TOOL_SLOTB, 145, 46 + SCREEN_SPACING));
        addSlot(new ResultSlot(blockEntity, RESULT_SLOT, 134, 72 + SCREEN_SPACING));
    }

    /**
     * Slot for the output of the artisan table.
     */
    public static class ResultSlot extends CallbackSlot {
        private final ArtisanTableBlockEntity blockEntity;

        /**
         * Constructs a ResultSlot.
         * @param blockEntity The artisan table block entity.
         * @param index The slot index.
         * @param x The x position.
         * @param y The y position.
         */
        public ResultSlot(ArtisanTableBlockEntity blockEntity, int index, int x, int y) {
            super(blockEntity, blockEntity.getInventory(), index, x, y);
            this.blockEntity = blockEntity;
        }

        /**
         * Determines if the player can pick up the output item.
         * @param player The player.
         * @return True if the item can be picked up.
         */
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

        /**
         * Stops items from entering the output slot.
         * @param stack The item stack.
         * @return False.
         */
        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        /**
         * Handles logic when the player takes the output item.
         * @param player The player.
         * @param stack The item stack taken.
         */
        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            blockEntity.damageTools(player);
            if (!blockEntity.isHasConsumedIngredient()) {
                blockEntity.consumeItems();
                blockEntity.resetPattern();
            }
            super.onTake(player, stack);

            player.level().playSound(null, blockEntity.getBlockPos(), TFCSounds.BELLOWS_BLOW.get(), player.getSoundSource(), 1, 2);

            if (player.level() instanceof ServerLevel serverLevel) {
                var pos = blockEntity.getBlockPos();
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 1.0;
                double z = pos.getZ() + 0.5;
                serverLevel.sendParticles(ParticleTypes.SCRAPE, x, y, z, 10, 0.5, 0.3, 0.5, 0.3);
            }

            if (blockEntity.canConsumeIngredients()) {
                blockEntity.resetPattern();
                blockEntity.checkForActiveScreen();
            }
        }
    }

    /**
     * Slot for artisan table inputs and tools.
     */
    public static class SmithingInputSlot extends CallbackSlot {
        private final ArtisanTableBlockEntity blockEntity;

        /**
         * Constructs a SmithingInputSlot.
         * @param container The artisan table container.
         * @param blockEntity The artisan table block entity.
         * @param index The slot index.
         * @param x The x position.
         * @param y The y position.
         */
        public SmithingInputSlot(ArtisanTableContainer container, ArtisanTableBlockEntity blockEntity, int index, int x, int y) {
            super(blockEntity, blockEntity.getInventory(), index, x, y);
            this.blockEntity = blockEntity;
        }

        /**
         * Determines if the player can pick up an item from this slot.
         * @param player The player.
         * @return True if the item can be picked up.
         */
        @Override
        public boolean mayPickup(Player player) {
            return super.mayPickup(player);
        }

        /**
         * Determines if an item can be placed in this slot.
         * Only allows placement if the artisan table is not in the active screen state.
         * @param stack The item stack.
         * @return True if the item can be placed.
         */
        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return !blockEntity.isActiveScreen() && super.mayPlace(stack);
        }
    }

    /**
     * Handler for recipe matching and crafting logic.
     * @param container The artisan table container.
     */
    public record RecipeHandler(ArtisanTableContainer container) implements EmptyInventory {
    }
}
