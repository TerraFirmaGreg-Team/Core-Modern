package su.terrafirmagreg.core.utils;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.world.item.ItemStack;

public class FoodStackingHelpers {

    public static final ThreadLocal<Boolean> IN_FOOD_CHECK = ThreadLocal.withInitial(() -> false);

    // Check if two stacks have a decay state that is close enough to stack.
    // If they're not identical foods then this does nothing.
    // If they are stacked, one of the decay values will win leading to sometimes getting a little bit of freshness for free.
    public static Boolean checkFoodStacking(ItemStack a, ItemStack b) {

        // Don't recurse
        if (IN_FOOD_CHECK.get())
            return null;

        // Don't mess with empty stacks or stacks that aren't fully constructed
        if (a.isEmpty() || b.isEmpty())
            return null;

        // Check if it's food
        if (!FoodCapability.has(a) || !FoodCapability.has(b))
            return null;

        // Check if foods are compatible
        try {
            IN_FOOD_CHECK.set(true);
            if (!FoodCapability.areStacksStackableExceptCreationDate(a, b))
                return null;
        } finally {
            IN_FOOD_CHECK.set(false);
        }

        IFood foodA = FoodCapability.get(a);
        IFood foodB = FoodCapability.get(b);

        if (foodA.getCreationDate() == foodB.getCreationDate())
            return true;

        long lifetime = foodA.getRottenDate() - foodA.getCreationDate();
        long dateDiff = Math.abs(foodA.getCreationDate() - foodB.getCreationDate());

        // Check if creation dates are within 1% of total lifetime
        if (dateDiff < lifetime / 100) {
            return true;
        }

        return false; // Otherwise fall through
    }
}
