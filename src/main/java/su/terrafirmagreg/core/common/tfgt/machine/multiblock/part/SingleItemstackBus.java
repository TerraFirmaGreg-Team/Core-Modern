package su.terrafirmagreg.core.common.tfgt.machine.multiblock.part;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.world.item.ItemStack;

/**
 * A special item bus that will not accept stacks larger than 1.
 */
public class SingleItemstackBus extends ItemBusPartMachine {

    /**
     * Instantiates a new Single itemstack bus.
     *
     * @param holder the holder
     */
    public SingleItemstackBus(BlockEntityCreationInfo info) {
        super(info, 0, IO.IN, new ObjectHolderHandler());
        circuitSlot.setEnabled(false);
    }

    // Inner handler that enforces a stacksize of 1 in 1 item slot.
    private static class ObjectHolderHandler extends NotifiableItemStackHandler {

        /**
         * Instantiates a new Object holder handler.
         */
        public ObjectHolderHandler() {
            super(1, IO.IN, IO.BOTH, size -> new CustomItemStackHandler(size) {
                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) {
                super.setStackInSlot(slot, ItemStack.EMPTY);
                return;
            }
            if (stack.getCount() > 1) {
                ItemStack single = stack.copy();
                single.setCount(1);
                super.setStackInSlot(slot, single);
            } else {
                super.setStackInSlot(slot, stack);
            }
        }
    }
}
