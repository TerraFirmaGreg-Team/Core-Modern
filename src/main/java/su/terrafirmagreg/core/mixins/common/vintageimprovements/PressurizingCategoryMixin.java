package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.negodya1.vintageimprovements.compat.jei.category.PressurizingCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

/**
 * Mixin to swap Blaze Burner and Blaze Cake catalyst renders for the gas burner.
 * Charcoal forge is also valid, but it cannot be rendered as an item.
 */
@Mixin(value = PressurizingCategory.class)
public abstract class PressurizingCategoryMixin {

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"), cancellable = true, remap = false)
    public void tfg$setRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        HeatCondition requiredHeat = recipe.getRequiredHeat();

        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            builder
                    .addSlot(RecipeIngredientRole.CATALYST, 153, 81)
                    .addItemStack(TFGBlocks.GAS_BURNER.asStack());
        }

        ci.cancel();
    }
}
