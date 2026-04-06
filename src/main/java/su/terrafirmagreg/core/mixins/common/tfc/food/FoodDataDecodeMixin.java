package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;

import su.terrafirmagreg.core.common.capabilities.food.TFGNutrients;

/**
 * Mixin to fix FoodData.decode() to only read positive nutrients.
 */
@Mixin(FoodData.class)
public class FoodDataDecodeMixin {

    /**
     * Redirect Nutrient.TOTAL field access in decode() to use POSITIVE_COUNT.
     */
    @Redirect(method = "decode", at = @At(value = "FIELD", target = "Lnet/dries007/tfc/common/capabilities/food/Nutrient;TOTAL:I", opcode = Opcodes.GETSTATIC), remap = false)
    private static int tfg$usePositiveCountForDecode() {
        return TFGNutrients.POSITIVE_COUNT;
    }

    /**
     * Redirect Nutrient.VALUES field access in decode() to return only positive nutrients.
     */
    @Redirect(method = "decode", at = @At(value = "FIELD", target = "Lnet/dries007/tfc/common/capabilities/food/Nutrient;VALUES:[Lnet/dries007/tfc/common/capabilities/food/Nutrient;", opcode = Opcodes.GETSTATIC), remap = false)
    private static Nutrient[] tfg$usePositiveValuesForDecode() {
        Nutrient[] allValues = Nutrient.values();
        Nutrient[] positiveValues = new Nutrient[TFGNutrients.POSITIVE_COUNT];
        System.arraycopy(allValues, 0, positiveValues, 0, TFGNutrients.POSITIVE_COUNT);
        return positiveValues;
    }
}
