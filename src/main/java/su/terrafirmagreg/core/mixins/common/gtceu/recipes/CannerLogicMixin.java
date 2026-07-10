package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.machine.trait.customlogic.CannerLogic;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraftforge.fluids.FluidStack;

@Mixin(value = CannerLogic.class, remap = false)
public abstract class CannerLogicMixin {

    // inputFluids(FluidStack) converts to a forge:tag that most of the time doesn't exist with our custom fluid
    // Use an exact ingredient instead
    // Remove when fixed upstream but should crash anyway

    @Redirect(method = "createCustomRecipe", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;inputFluids(Lnet/minecraftforge/fluids/FluidStack;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;"), require = 3, remap = false)
    public GTRecipeBuilder tfg$exactFluidIngredient(GTRecipeBuilder builder, FluidStack stack) {
        return builder.input(FluidRecipeCapability.CAP, FluidIngredient.of(stack));
    }
}
