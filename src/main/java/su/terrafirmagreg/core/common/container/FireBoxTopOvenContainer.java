package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.blockentity.appliances.FireBoxTopOvenBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;

public class FireBoxTopOvenContainer extends BlockEntityContainer<FireBoxTopOvenBlockEntity> {

    public static FireBoxTopOvenContainer create(FireBoxTopOvenBlockEntity oven, Inventory playerInv, int windowId) {
        return new FireBoxTopOvenContainer(oven, windowId).init(playerInv, 20);
    }

    public FireBoxTopOvenContainer(FireBoxTopOvenBlockEntity oven, int windowId) {
        super(TFGContainers.FIRE_BOX_TOP_OVEN.get(), windowId, oven);
        addDataSlots(oven.getSyncableData());
    }

    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
        switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> {
                return !moveItemStackTo(stack, 0, 9, false);
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
            for (int i = 0; i < 9; i++) {
                addSlot(new CallbackSlot(blockEntity, handler, i, 62 + (i % 3) * 18, 17 + (i / 3) * 18));
            }
            for (int i = 0; i < 9; i++) {
                addSlot(new CallbackSlot(blockEntity, handler, 9 + i, 116 + (i % 3) * 18, 17 + (i / 3) * 18));
            }
        });
    }
}
