package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.blockentity.appliances.SkilletBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGTags;

public class SkilletContainer extends BlockEntityContainer<SkilletBlockEntity> {

    private static final int SLOT_SIZE = 18;

    public static final int INPUT_SLOT_X = 43;
    public static final int INPUT_SLOT_Y = 30;
    public static final int CATALYST_SLOT_X = INPUT_SLOT_X + (SLOT_SIZE * 3) + 9;
    public static final int CATALYST_SLOT_Y = INPUT_SLOT_Y + 1;
    private static final int TOOL_SLOT_X = CATALYST_SLOT_X;
    private static final int TOOL_SLOT_Y = CATALYST_SLOT_Y + (SLOT_SIZE * 2);
    private static final int OUTPUT_SLOT_X = CATALYST_SLOT_X + (SLOT_SIZE * 2) + 1;
    private static final int OUTPUT_SLOT_Y = CATALYST_SLOT_Y;

    public static SkilletContainer create(SkilletBlockEntity appliance, Inventory playerInventory, int windowId) {
        return new SkilletContainer(appliance, windowId).init(playerInventory, 35);
    }

    public SkilletContainer(SkilletBlockEntity appliance, int windowId) {
        super(TFGContainers.SKILLET.get(), windowId, appliance);
        addDataSlots(appliance.getSyncableData());
    }

    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
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
                int col = i % 2;
                int row = i / 2;
                if (row == 0 || row == 2) {
                    addSlot(new CallbackSlot(blockEntity, handler, i, INPUT_SLOT_X + (col * (SLOT_SIZE + 1)), INPUT_SLOT_Y + (row * (SLOT_SIZE + 1))));
                } else {
                    addSlot(new CallbackSlot(blockEntity, handler, i, INPUT_SLOT_X - 6 + (col * (SLOT_SIZE + 13)), INPUT_SLOT_Y + (row * (SLOT_SIZE + 1))));
                }
            }

            // Catalyst slot.
            addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getCatalystSlot(), CATALYST_SLOT_X, CATALYST_SLOT_Y));

            // Tool slot.
            addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getToolSlot(), TOOL_SLOT_X, TOOL_SLOT_Y));

            // Item output slots.
            for (int i = 0; i < outputCount; i++) {
                addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getItemOutputStartSlot() + i, OUTPUT_SLOT_X, OUTPUT_SLOT_Y + (i * SLOT_SIZE)));
            }
        });
    }

    public @NotNull SkilletBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
