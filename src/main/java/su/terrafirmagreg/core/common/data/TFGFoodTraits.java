package su.terrafirmagreg.core.common.data;

import net.dries007.tfc.common.capabilities.food.FoodTrait;
import su.terrafirmagreg.core.TFGCore;

public class TFGFoodTraits {
    public static void init() {}

    public static final FoodTrait REFRIGERATING = FoodTrait.register(TFGCore.id("refrigerating"),
        new FoodTrait(0.125f, "tfg.tooltip.food_trait.refrigerating"));

    public static final FoodTrait FREEZE_DRIED = FoodTrait.register(TFGCore.id("freeze_dried"),
            new FoodTrait(0.02f, "tfg.tooltip.foodtrait.freeze_dried"));
}
