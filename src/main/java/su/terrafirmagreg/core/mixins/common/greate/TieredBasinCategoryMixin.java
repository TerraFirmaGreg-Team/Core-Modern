package su.terrafirmagreg.core.mixins.common.greate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;

import net.minecraft.world.item.ItemStack;

import electrolyte.greate.compat.jei.category.GreateRecipeCategory;
import electrolyte.greate.compat.jei.category.TieredBasinCategory;
import electrolyte.greate.content.processing.basin.TieredBasinRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

/**
 * Mixin to swap Blaze Burner and Blaze Cake catalyst renders for the gas burner.
 * Charcoal forge is also valid, but it cannot be rendered as an item.
 */
@Mixin(value = TieredBasinCategory.class, remap = false)
public abstract class TieredBasinCategoryMixin extends GreateRecipeCategory<TieredBasinRecipe> {

    public TieredBasinCategoryMixin(Info<TieredBasinRecipe> info) {
        super(info);
    }

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lelectrolyte/greate/content/processing/basin/TieredBasinRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At(value = "INVOKE", target = "Lelectrolyte/greate/content/processing/basin/TieredBasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"), cancellable = true, remap = false)
    public void tfg$setRecipe(IRecipeLayoutBuilder builder, TieredBasinRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        HeatCondition requiredHeat = recipe.getRequiredHeat();

        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            builder
                    .addSlot(RecipeIngredientRole.CATALYST, 153, 81)
                    .addItemStack(TFGBlocks.GAS_BURNER.asStack());
        }

        ItemStack circuitStack = getCircuitStack(recipe);
        if (!circuitStack.isEmpty() && this.getBackground() != null) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, this.getBackground().getWidth() / 2 - 17, 13)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStack(circuitStack);
        }

        ci.cancel();
    }
}
