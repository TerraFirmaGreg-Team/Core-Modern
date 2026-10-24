package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.client.screen.CopperPotScreen;
import su.terrafirmagreg.core.common.blockentity.appliances.CookingApplianceBlockEntity;
import su.terrafirmagreg.core.common.blockentity.appliances.CopperPotBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGTags;

public class CopperPotContainer extends BlockEntityContainer<CopperPotBlockEntity> implements ButtonHandlerContainer {

    public static final int BUTTON_FLUID_INPUT_START = 0;
    public static final int BUTTON_FLUID_OUTPUT_START = 10;

    private static final int SLOT_SIZE = 18;

    public static final int INPUT_SLOT_X = 26;
    public static final int INPUT_SLOT_Y = 29;
    private static final int CATALYST_SLOT_X = INPUT_SLOT_X + (SLOT_SIZE * 3) + 8;
    private static final int CATALYST_SLOT_Y = INPUT_SLOT_Y + (SLOT_SIZE * 2) - 3;
    private static final int TOOL_SLOT_X = CATALYST_SLOT_X;
    private static final int TOOL_SLOT_Y = CATALYST_SLOT_Y + SLOT_SIZE;
    private static final int OUTPUT_SLOT_X = CopperPotScreen.OUTPUT_TANKS_X + 1;
    private static final int OUTPUT_SLOT_Y = CopperPotScreen.OUTPUT_TANKS_Y - SLOT_SIZE - 2;

    public static CopperPotContainer create(CopperPotBlockEntity appliance, Inventory playerInventory, int windowId) {
        return new CopperPotContainer(appliance, windowId).init(playerInventory, 35);
    }

    public CopperPotContainer(CopperPotBlockEntity appliance, int windowId) {
        super(TFGContainers.COPPER_POT.get(), windowId, appliance);
        addDataSlots(appliance.getSyncableData());
    }

    // Handles fluid tank interactions.
    @Override
    public void onButtonPress(int buttonId, @Nullable CompoundTag extraNbt) {
        if (player == null) {
            return;
        }

        ItemStack carried = getCarried();
        if (carried.isEmpty()) {
            return;
        }

        if (buttonId >= BUTTON_FLUID_INPUT_START && buttonId < BUTTON_FLUID_INPUT_START + blockEntity.getFluidInputCount()) {
            int tankIdx = buttonId - BUTTON_FLUID_INPUT_START;
            CookingApplianceBlockEntity.interactFluid(carried, blockEntity, player, tankIdx, false, this::setCarried);
        } else if (buttonId >= BUTTON_FLUID_OUTPUT_START && buttonId < BUTTON_FLUID_OUTPUT_START + blockEntity.getFluidOutputCount()) {
            int tankIdx = buttonId - BUTTON_FLUID_OUTPUT_START;
            CookingApplianceBlockEntity.interactFluid(carried, blockEntity, player, tankIdx, true, this::setCarried);
        }
    }

    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return false;
        }

        switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> {
                if (stack.is(TFGTags.Items.APPLIANCE_CATALYSTS)) {
                    if (moveItemStackTo(stack, blockEntity.getCatalystSlot(), blockEntity.getCatalystSlot() + 1, false)) {
                        return true;
                    }
                }
                if (stack.is(TFGTags.Items.APPLIANCE_TOOLS)) {
                    if (moveItemStackTo(stack, blockEntity.getToolSlot(), blockEntity.getToolSlot() + 1, false)) {
                        return true;
                    }
                }
                if (!stack.is(TFGTags.Items.APPLIANCE_TOOLS) && !stack.is(TFGTags.Items.APPLIANCE_CATALYSTS)) {
                    return moveItemStackTo(stack, 0, blockEntity.getItemInputCount(), false);
                }
                return false;
            }
            case CONTAINER -> {
                return !moveItemStackTo(stack, containerSlots, slots.size(), false);
            }
        }
        return false;
    }

    @Override
    protected void addContainerSlots() {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            int inputCount = blockEntity.getItemInputCount();
            int outputCount = blockEntity.getItemOutputCount();

            // Item input slots.
            for (int i = 0; i < inputCount; i++) {
                int col = i % 3;
                int row = i / 3;
                addSlot(new CallbackSlot(blockEntity, handler, i, INPUT_SLOT_X + col * SLOT_SIZE, INPUT_SLOT_Y + row * SLOT_SIZE));
            }

            // Catalyst slot.
            addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getCatalystSlot(), CATALYST_SLOT_X, CATALYST_SLOT_Y));

            // Tool slot.
            addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getToolSlot(), TOOL_SLOT_X, TOOL_SLOT_Y));

            // Item output slots.
            for (int i = 0; i < outputCount; i++) {
                int col = i % outputCount;
                addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getItemOutputStartSlot() + i, OUTPUT_SLOT_X + col * SLOT_SIZE, OUTPUT_SLOT_Y));
            }
        });
    }

    public @NotNull CopperPotBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
