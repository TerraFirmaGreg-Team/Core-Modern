package su.terrafirmagreg.core.common.data;

import net.dries007.tfc.common.capabilities.food.FoodTrait;
import su.terrafirmagreg.core.TFGCore;

public class TFGFoodTraits {
    public static final FoodTrait REFRIGERATING = FoodTrait.register(TFGCore.id("refrigerating"),
        new FoodTrait(0.125f, "tfg.tooltip.food_trait.refrigerating"));
}
