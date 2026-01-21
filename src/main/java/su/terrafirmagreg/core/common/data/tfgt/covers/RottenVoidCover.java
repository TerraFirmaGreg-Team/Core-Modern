package su.terrafirmagreg.core.common.data.tfgt.covers;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.common.cover.voiding.ItemVoidingCover;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * A voiding cover for GT that only voids rotten food.
 */
public class RottenVoidCover extends ItemVoidingCover {

    public RottenVoidCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    protected void doVoidItems() {
        IItemHandler handler = getOwnItemHandler();
        if (!(handler instanceof IItemHandlerModifiable modifiable))
            return;

        ItemFilter filter = filterHandler.getFilter();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack sourceStack = handler.getStackInSlot(slot);
            if (sourceStack.isEmpty())
                continue;

            IFood food = FoodCapability.get(sourceStack);
            if (food == null || !food.isRotten())
                continue;

            if (!filter.test(sourceStack))
                continue;

            modifiable.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }
}
