package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.blockentity.CopperPotBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.TFGTags;

public class CopperPotContainer extends BlockEntityContainer<CopperPotBlockEntity> {

    public static CopperPotContainer create(CopperPotBlockEntity appliance, Inventory playerInventory, int windowId) {
        return new CopperPotContainer(appliance, windowId).init(playerInventory, 20);
    }

    public CopperPotContainer(CopperPotBlockEntity appliance, int windowId) {
        super(TFGContainers.COPPER_POT.get(), windowId, appliance);
        addDataSlots(appliance.getSyncableData());
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
            int startX = 44 - 18;
            int startY = 20;
            for (int i = 0; i < inputCount; i++) {
                int col = i % 3;
                int row = i / 3;
                addSlot(new CallbackSlot(blockEntity, handler, i, startX + col * 18, startY + row * 18));
            }

            // Catalyst slot.
            addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getCatalystSlot(), startX + (18 * 3) + 8, startY + 36));

            // Tool slot.
            addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getToolSlot(), startX + (18 * 3) + 8, startY + 36 + 18));

            // Item output slots.
            int outStartX = 116;
            int outStartY = 29;
            for (int i = 0; i < outputCount; i++) {
                int col = i % 3;
                int row = i / 3;
                addSlot(new CallbackSlot(blockEntity, handler, blockEntity.getItemOutputStartSlot() + i, outStartX + col * 18, outStartY + row * 18));
            }
        });
    }

    public @NotNull CopperPotBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
