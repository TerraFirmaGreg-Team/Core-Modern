package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.common.recipes.AlloyRecipe;
import net.dries007.tfc.compat.jei.category.AlloyRecipeCategory;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@Mixin(AlloyRecipeCategory.class)
public abstract class AlloyRecipeCategoryMixin {
    @Redirect(method = "setRecipe", at = @At(value = "NEW", target = "net/minecraftforge/fluids/FluidStack", ordinal = 0, remap = false), remap = false)
    private FluidStack modifyInputFluidStack(Fluid fluid, int amount, @Local(argsOnly = true) AlloyRecipe recipe) {
        Material resultMaterial = ChemicalHelper.getMaterial(recipe.getResult().getFluid());
        Material inputMaterial = ChemicalHelper.getMaterial(fluid);

        for (MaterialStack component : resultMaterial.getMaterialComponents()) {
            if (component.material().equals(inputMaterial)) {
                amount = (int) component.amount();
                break;
            }
        }

        return new FluidStack(fluid, amount);
    }

    @Redirect(method = "setRecipe", at = @At(value = "NEW", target = "net/minecraftforge/fluids/FluidStack", ordinal = 1, remap = false), remap = false)
    private FluidStack modifyOutputFluidStack(Fluid fluid, int amount, @Local(argsOnly = true) AlloyRecipe recipe) {
        Material resultMaterial = ChemicalHelper.getMaterial(recipe.getResult().getFluid());
        int totalAmount = 0;

        for (MaterialStack component : resultMaterial.getMaterialComponents()) {
            totalAmount += (int) component.amount();
        }

        return new FluidStack(fluid, totalAmount);
    }
}
