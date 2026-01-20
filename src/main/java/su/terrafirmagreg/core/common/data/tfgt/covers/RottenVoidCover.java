package su.terrafirmagreg.core.common.data.tfgt.covers;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.common.cover.voiding.ItemVoidingCover;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

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

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            RottenVoidCover.class, ItemVoidingCover.MANAGED_FIELD_HOLDER);

    public RottenVoidCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
