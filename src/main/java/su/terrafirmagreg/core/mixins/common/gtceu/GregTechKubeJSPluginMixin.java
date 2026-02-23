package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeAdditionHandler;
import com.gregtechceu.gtceu.integration.kjs.GregTechKubeJSPlugin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

/**
 * Fixes GTRecipeType.categoryMap not being cleared on /reload, causing stale EMI entries.
 * Remove this mixin with the next release of GT (after 7.5.2)
 */
@Mixin(value = GregTechKubeJSPlugin.class, remap = false)
public abstract class GregTechKubeJSPluginMixin {

    @WrapOperation(method = "injectRuntimeRecipes", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/recipe/lookup/RecipeAdditionHandler;beginStaging()V"))
    private void tfg$clearCategoryMapBeforeStaging(RecipeAdditionHandler handler, Operation<Void> original,
            @Local(name = "gtRecipeType") GTRecipeType gtRecipeType) {
        gtRecipeType.getCategoryMap().clear();
        original.call(handler);
    }
}
