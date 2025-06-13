package su.terrafirmagreg.core.common.data;

import static su.terrafirmagreg.core.TFGCore.MOD_ID;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.minecraft.resources.ResourceLocation;

public class TFGFoodTraits {
    public static final FoodTrait REFRIGERATING = FoodTrait.register(new ResourceLocation(MOD_ID, "refrigerating"), new FoodTrait(0.125f, "tfg.tooltip.food_trait.refrigerating"));
}
