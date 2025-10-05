package su.terrafirmagreg.core.common.data.container;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.PestContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.data.TFGContainers;
import su.terrafirmagreg.core.common.data.blockentity.LargeNestBoxBlockEntity;

public class LargeNestBoxContainer extends BlockEntityContainer<LargeNestBoxBlockEntity> implements PestContainer {

    public static LargeNestBoxContainer create(LargeNestBoxBlockEntity nest, Inventory playerInventory, int windowId) {
        return new LargeNestBoxContainer(nest, playerInventory, windowId).init(playerInventory);
    }

    public LargeNestBoxContainer(LargeNestBoxBlockEntity blockEntity, Inventory playerInv, int windowId) {
        super(TFGContainers.LARGE_NEST_BOX.get(), windowId, blockEntity);
    }

    @Override
    protected boolean moveStack(ItemStack stack, int slotIndex) {
        return switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, 0, LargeNestBoxBlockEntity.SLOTS, false);
            case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
        };
    }

    @Override
    protected void addContainerSlots() {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            addSlot(new CallbackSlot(blockEntity, handler, 0, 71, 23));
            addSlot(new CallbackSlot(blockEntity, handler, 1, 89, 23));
            addSlot(new CallbackSlot(blockEntity, handler, 2, 71, 41));
            addSlot(new CallbackSlot(blockEntity, handler, 3, 89, 41));
        });
    }

}
