package su.terrafirmagreg.core.mixins.common.tfc.food;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.util.Helpers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;
import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

/**
 * Mixin to add negative nutrients to the food tooltip display.
 */
@Mixin(FoodCapability.class)
public class IFoodMixin {

    /**
     * Inject at the end of addTooltipInfo to add negative nutrient displays.
     */
    @Inject(method = "addTooltipInfo", at = @At("TAIL"), remap = false)
    private static void tfg$addNegativeNutrientTooltips(ItemStack stack, List<Component> text, CallbackInfo ci) {
        IFood food = FoodCapability.get(stack);
        if (food == null || food.isRotten())
            return;

        FoodData data = food.getData();

        for (Nutrient nutrient : Nutrient.values()) {
            if (TFGNutrients.isNegative(nutrient)) {
                float value = FoodDataExtension.getNegativeNutrient(data, nutrient);
                if (value > 0) {
                    text.add(Component.literal(" - ")
                            .append(Helpers.translateEnum(nutrient))
                            .append(": " + String.format("%.1f", value))
                            .withStyle(nutrient.getColor()));
                }
            }
        }
    }
}
