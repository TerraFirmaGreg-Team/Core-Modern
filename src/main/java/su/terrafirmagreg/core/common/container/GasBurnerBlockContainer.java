package su.terrafirmagreg.core.common.container;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.blockentity.GasBurnerBlockEntity;
import su.terrafirmagreg.core.common.data.TFGContainers;

public class GasBurnerBlockContainer extends BlockEntityContainer<GasBurnerBlockEntity> {
    public static GasBurnerBlockContainer create(GasBurnerBlockEntity burner, Inventory playerInventory, int windowId) {
        return new GasBurnerBlockContainer(burner, windowId).init(playerInventory, 20);
    }

    private GasBurnerBlockContainer(GasBurnerBlockEntity burner, int windowId) {
        super(TFGContainers.GAS_BURNER.get(), windowId, burner);

        addDataSlots(burner.getSyncableData());
    }

    @Override
    protected boolean moveStack(@NotNull ItemStack stack, int slotIndex) {
        return switch (typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !moveItemStackTo(stack, GasBurnerBlockEntity.SLOT_FLUID_CONTAINER_IN, GasBurnerBlockEntity.SLOT_FLUID_CONTAINER_IN + 1, false);
            case CONTAINER -> !moveItemStackTo(stack, containerSlots, slots.size(), false);
        };
    }

    @Override
    protected void addContainerSlots() {
        blockEntity.getCapability(Capabilities.ITEM).ifPresent(handler -> {
            addSlot(new CallbackSlot(blockEntity, handler, GasBurnerBlockEntity.SLOT_FLUID_CONTAINER_IN, 35, 20));
            addSlot(new CallbackSlot(blockEntity, handler, GasBurnerBlockEntity.SLOT_FLUID_CONTAINER_OUT, 35, 54));
        });
    }
}
