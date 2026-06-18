package su.terrafirmagreg.core.mixins.common.tfc;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Allows non-food items to pass the notRotten check.
 */
@Mixin(value = NotRottenIngredient.class, remap = false)
public class NotRottenIngredientMixin {

    /**
     * Passes test if the item has no food capability and the delegate ingredient still matches.
     */
    @Inject(method = "test(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
    private void tfg$onTest(@Nullable ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && stack != null && !stack.isEmpty() && FoodCapability.get(stack) == null) {
            Ingredient delegate = ((IDelegateIngredientAccessor) (Object) this).getDelegate();
            if (delegate == null || delegate.test(stack)) {
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * If the item has no food capability, return the stack rather than null.
     */
    @Inject(method = "testDefaultItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
    private void tfg$onTestDefaultItem(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(stack);
        }
    }
}
