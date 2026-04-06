package su.terrafirmagreg.core.mixins.common.tfc.food;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodData;
import net.minecraft.nbt.CompoundTag;

import su.terrafirmagreg.core.common.capabilities.food.FoodDataExtension;

/**
 * Mixin to handle reading negative nutrients from NBT when FoodData is deserialized.
 */
@Mixin(FoodData.class)
public class FoodDataReadMixin {

    /**
     * After FoodData.read(CompoundTag) returns, read negative nutrients.
     */
    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;)Lnet/dries007/tfc/common/capabilities/food/FoodData;", at = @At("RETURN"), remap = false)
    private static void tfg$readNegativeNutrients(CompoundTag nbt, CallbackInfoReturnable<FoodData> cir) {
        FoodData data = cir.getReturnValue();
        if (data != null) {
            FoodDataExtension.readFromNbt(data, nbt);
        }
    }
}
