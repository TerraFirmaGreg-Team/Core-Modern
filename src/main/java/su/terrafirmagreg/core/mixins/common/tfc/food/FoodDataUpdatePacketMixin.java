package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.network.FoodDataUpdatePacket;

import su.terrafirmagreg.core.common.food.nutrient.TFGNutrients;

/**
 * Mixin to fix network packet serialization for nutrition data.
 * The packet uses Nutrient.TOTAL for array size, but after we extend the enum,
 * this causes buffer underflow because the server sends only 5 floats but client expects 7.
 */
@Mixin(FoodDataUpdatePacket.class)
public class FoodDataUpdatePacketMixin {

    /**
     * Redirect array creation in readNutrients to use POSITIVE_COUNT instead of Nutrient.TOTAL.
     */
    @Redirect(method = "readNutrients", at = @At(value = "FIELD", target = "Lnet/dries007/tfc/common/capabilities/food/Nutrient;TOTAL:I", opcode = Opcodes.GETSTATIC), remap = false)
    private static int tfg$usePositiveCountForRead() {
        return TFGNutrients.POSITIVE_COUNT;
    }
}
