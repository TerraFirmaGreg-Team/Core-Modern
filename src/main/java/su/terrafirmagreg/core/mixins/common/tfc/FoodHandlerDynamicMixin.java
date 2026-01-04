package su.terrafirmagreg.core.mixins.common.tfc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.minecraft.world.item.ItemStack;

@Mixin(FoodHandler.Dynamic.class)
public class FoodHandlerDynamicMixin {

    // Sort ingredients in dynamic foods before serializing. This ensures stacks with different ingredient order can stack.
    @ModifyArg(method = "serializeNBT", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/Helpers;writeItemStacksToNbt(Ljava/util/List;)Lnet/minecraft/nbt/ListTag;"), remap = false)
    private List<ItemStack> sortIngredientsBeforeSerialize(List<ItemStack> ingredients) {
        if (ingredients == null || ingredients.size() <= 1) {
            return ingredients;
        }
        List<ItemStack> sorted = new ArrayList<>(ingredients);
        sorted.sort(Comparator.comparing(ItemStack::toString));
        return sorted;
    }
}
