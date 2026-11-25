package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.PestContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

import su.terrafirmagreg.core.common.block.appliances.BakingDishBlock;
import su.terrafirmagreg.core.common.blockentity.appliances.BakingDishBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;

public class BakingDishContainer extends BlockEntityContainer<BakingDishBlockEntity> implements ButtonHandlerContainer, PestContainer {

    public static final int BUTTON_SEAL = 0;
    public static final int BUTTON_FLUID_IDX = 1;

    private static final int SLOT_SIZE = 18;

    public static final int INPUT_SLOT_X = 52;
    public static final int INPUT_SLOT_Y = 29;

    public static BakingDishContainer create(BakingDishBlockEntity appliance, Inventory playerInventory, int windowId) {
        return new BakingDishContainer(appliance, windowId).init(playerInventory, 11);
    }

    public BakingDishContainer(BakingDishBlockEntity appliance, int windowId) {
        super(TFGContainers.BAKING_DISH.get(), windowId, appliance);
    }

    @Override
    public void clicked(int slot, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slot >= 0 && slot < blockEntity.getSlots() && isSealed()) {
            return;
        }
        super.clicked(slot, button, clickType, player);
    }

    // Handles button interactions.
    @Override
    @SuppressWarnings("unchecked")
    public void onButtonPress(int buttonId, @Nullable CompoundTag extraNbt) {
        if (player == null) {
            return;
        }

        if (buttonId == BUTTON_SEAL) {
            Level level = blockEntity.getLevel();
            if (level != null) {
                BakingDishBlock.toggleSeal(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), (BlockEntityType<? extends BakingDishBlockEntity>) blockEntity.getType());
            }
        } else if (buttonId == BUTTON_FLUID_IDX) {
            ItemStack carried = getCarried();
            if (!carried.isEmpty()) {
                BakingDishBlockEntity.interactFluid(carried, blockEntity, player, this::setCarried);
            }
        }
    }

    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
        if (isSealed()) {
            return true;
        }

        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return false;
        }

        switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> {
                return moveItemStackTo(stack, 0, blockEntity.getSlots(), false);
            }
            case CONTAINER -> {
                return !moveItemStackTo(stack, containerSlots, slots.size(), false);
            }
        }
        return false;
    }

    public boolean isSealed() {
        return blockEntity.getBlockState().hasProperty(BakingDishBlock.SEALED) && blockEntity.getBlockState().getValue(BakingDishBlock.SEALED);
    }

    @Override
    protected void addContainerSlots() {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            int inputCount = blockEntity.getSlots();

            // Item input slots.
            int cols = (inputCount + 1) / 2;
            for (int i = 0; i < inputCount; i++) {
                int col = i % cols;
                int row = i / cols;
                addSlot(new CallbackSlot(blockEntity, handler, i, INPUT_SLOT_X + col * SLOT_SIZE, INPUT_SLOT_Y + row * SLOT_SIZE));
            }
        });
    }

    public @NotNull BakingDishBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
