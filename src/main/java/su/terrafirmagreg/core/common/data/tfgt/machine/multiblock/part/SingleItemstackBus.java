package su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.world.item.ItemStack;

/**
 * A special item bus that will not accept stacks larger than 1.
 */
public class SingleItemstackBus extends ItemBusPartMachine {

    public SingleItemstackBus(IMachineBlockEntity holder) {
        super(holder, GTValues.EV, IO.IN);
    }

    @Override
    public int getInventorySize() {
        return 1;
    }

    @Override
    protected @NotNull NotifiableItemStackHandler createInventory(Object @NotNull... args) {
        return new ObjectHolderHandler(this);
    }

    // Inner handler that enforces a stacksize of 1 in 1 item slot.
    private static class ObjectHolderHandler extends NotifiableItemStackHandler {

        public ObjectHolderHandler(MetaMachine metaTileEntity) {
            super(metaTileEntity, 1, IO.IN, IO.BOTH, size -> new CustomItemStackHandler(size) {
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
