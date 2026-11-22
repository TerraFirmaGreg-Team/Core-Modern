package su.terrafirmagreg.core.common.tfgt.machine.trait;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.feature.IRecipeLogicModifierTrait;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Makes a machine persist specific items that were used in the last N recipes. */
public class InputHistoryTrait extends MachineTrait implements IRecipeLogicModifierTrait {

    @SaveField
    private List<ItemStack> history = new ArrayList<>();

    private final int capacity;
    private final Predicate<ItemStack> filter;

    public InputHistoryTrait(int capacity, Predicate<ItemStack> filter) {
        this.capacity = capacity;
        this.filter = filter;
    }

    @Override
    public void afterWorking() {
        RecipeLogic logic = getMachine().getTrait(RecipeLogic.class);
        if (!logic.isWorking())
            return;

        for (Ingredient ingredient : logic.getConsumedInputs().getConsumedInputs(GTRecipeCapabilities.ITEM)) {
            ItemStack stack = ingredient.getItems()[0];
            if (!filter.test(stack))
                continue;

            history.add(0, stack.copy());
            if (history.size() > capacity)
                history.remove(history.size() - 1);
        }
        markAsChanged();
    }

    /** Gets the first item stack in the input busses that matches the filter. */
    public @Nullable ItemStack getActiveInput() {
        IRecipeCapabilityHolder holder = (IRecipeCapabilityHolder) getMachine();

        for (IRecipeHandler<?> handler : holder.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (!(handler instanceof NotifiableItemStackHandler inventory))
                continue;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!stack.isEmpty() && filter.test(stack))
                    return stack;
            }
        }
        return null;
    }

    /** Get the amount of distinct items in the entire history. */
    public int getUniqueCount() {
        List<ItemStack> seen = new ArrayList<>();
        ItemStack current = getActiveInput();
        if (current != null)
            seen.add(current);

        for (ItemStack stack : history) {
            boolean duplicate = false;
            for (ItemStack prior : seen) {
                if (ItemStack.isSameItemSameTags(stack, prior)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate)
                seen.add(stack);
        }
        return seen.size();
    }
}
