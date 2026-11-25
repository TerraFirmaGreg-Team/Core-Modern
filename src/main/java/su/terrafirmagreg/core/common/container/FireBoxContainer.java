package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;

public class FireBoxContainer extends BlockEntityContainer<FireBoxBlockEntity> {
    public static FireBoxContainer create(FireBoxBlockEntity fireBox, Inventory playerInventory, int windowId) {
        return new FireBoxContainer(fireBox, windowId).init(playerInventory, 20);
    }

    private FireBoxContainer(FireBoxBlockEntity fireBox, int windowId) {
        super(TFGContainers.FIRE_BOX.get(), windowId, fireBox);

        addDataSlots(fireBox.getSyncableData());
    }

    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
        return switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, FireBoxBlockEntity.SLOT_FUEL_IN, FireBoxBlockEntity.SLOT_FUEL_IN + 1, false);
            case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
        };
    }

    @Override
    protected void addContainerSlots() {
        int SlotInX = 79;
        int SlotInY = 64;

        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> addSlot(
                new CallbackSlot(blockEntity, handler, FireBoxBlockEntity.SLOT_FUEL_IN, SlotInX, SlotInY)));
    }
}
