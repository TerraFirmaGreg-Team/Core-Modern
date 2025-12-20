package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.Comparator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Mixin(FoodHandler.Dynamic.class)
public class FoodHandlerDynamicMixin {

    // Sort ingredients in dynamic foods before serializing. This ensures stacks with different ingredient order can stack.
    @Inject(method = "serializeNBT", at = @At("HEAD"), remap = false)
    private void sortIngredientsBeforeSerialize(CallbackInfoReturnable<CompoundTag> cir) {
        List<ItemStack> ingredients = ((FoodHandler.Dynamic) (Object) this).getIngredients();
        if (ingredients != null && ingredients.size() > 1) {
            ingredients.sort(Comparator.comparing(ItemStack::toString));
        }
    }
}
