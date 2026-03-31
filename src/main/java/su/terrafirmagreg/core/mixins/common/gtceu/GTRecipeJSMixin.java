package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.core.mixins.IngredientAccessor;
import com.gregtechceu.gtceu.core.mixins.TagValueAccessor;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;

import net.minecraft.world.item.crafting.Ingredient;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;

/**
 * Validates that item/fluid tag ingredients actually resolve to something, error if not.
 * Remove this mixin with the next release of GT (after 7.5.2)
 */
@Mixin(value = GTRecipeSchema.GTRecipeJS.class, remap = false)
public abstract class GTRecipeJSMixin extends RecipeJS {

    @Inject(method = "validateItems(Ljava/lang/String;[Ldev/latvian/mods/kubejs/item/InputItem;)V", at = @At("TAIL"))
    private void tfg$validateItemTags(String type, InputItem[] items, CallbackInfo ci) {
        for (var stack : items) {
            if (stack == null || stack.isEmpty())
                continue;
            if (stack.ingredient.getItems().length == 0) {
                String tagInfo = "";
                var values = ((IngredientAccessor) stack.ingredient).getValues();
                if (values.length == 1 && values[0] instanceof Ingredient.TagValue tagValue) {
                    tagInfo = " (empty or unknown tag: #" + ((TagValueAccessor) tagValue).getTag().location() + ")";
                }
                throw new RecipeExceptionJS(
                        String.format("Invalid or empty %s item (recipe ID: %s)%s", type, id, tagInfo));
            }
        }
    }

    @Inject(method = "validateFluids(Ljava/lang/String;[Lcom/gregtechceu/gtceu/integration/kjs/recipe/components/GTRecipeComponents$FluidIngredientJS;)V", at = @At("TAIL"))
    private void tfg$validateFluidTags(String type, GTRecipeComponents.FluidIngredientJS[] fluids, CallbackInfo ci) {
        for (var fluid : fluids) {
            if (fluid == null || fluid.ingredient() == null || fluid.ingredient().getStacks() == null)
                continue;
            if (fluid.ingredient().getStacks().length == 0) {
                String tagInfo = "";
                var values = fluid.ingredient().values;
                if (values.length == 1 && values[0] instanceof FluidIngredient.TagValue tagValue) {
                    tagInfo = " (empty or unknown tag: #" + tagValue.tag().location() + ")";
                }
                throw new RecipeExceptionJS(
                        String.format("Invalid or empty %s fluid (recipe ID: %s)%s", type, id, tagInfo));
            }
        }
    }
}
