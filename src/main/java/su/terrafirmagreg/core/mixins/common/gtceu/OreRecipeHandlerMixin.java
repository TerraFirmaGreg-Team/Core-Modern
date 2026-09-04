package su.terrafirmagreg.core.mixins.common.gtceu;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler;

import net.minecraft.data.recipes.FinishedRecipe;

/**
 * Mixin to cancel the GT ore proc recipes so we can do our customize ones through kubejs
 */

@Mixin(value = OreRecipeHandler.class, remap = false)
public abstract class OreRecipeHandlerMixin {

    @Inject(method = "run", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$cancelVanillaOreRecipes(Consumer<FinishedRecipe> provider,
            Material material,
            CallbackInfo ci) {
        ci.cancel();
    }
}
