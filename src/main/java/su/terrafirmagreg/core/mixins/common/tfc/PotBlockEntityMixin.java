package su.terrafirmagreg.core.mixins.common.tfc;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.recipes.PotRecipe;
import net.minecraftforge.fluids.FluidStack;

/**
 * Makes pot recipes consume only the required fluid amount instead of all fluid in the pot.
 */
@Mixin(value = PotBlockEntity.class, remap = false)
public abstract class PotBlockEntityMixin {

    @Shadow
    @Nullable
    private PotRecipe cachedRecipe;

    /**
     * Intercepts the tank.setFluid(FluidStack.EMPTY) call in handleCooking()
     * and instead drains only the required amount specified by the recipe.
     */
    @Redirect(method = "handleCooking", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;setFluid(Lnet/minecraftforge/fluids/FluidStack;)V"))
    private void tfg$consumeOnlyRequiredFluid(net.minecraftforge.fluids.capability.templates.FluidTank tank, FluidStack empty) {

        assert cachedRecipe != null;
        var fluidIngredient = cachedRecipe.getFluidIngredient();
        int requiredAmount = fluidIngredient.amount();
        FluidStack currentFluid = tank.getFluid();
        int remainingAmount = Math.max(0, currentFluid.getAmount() - requiredAmount);

        if (remainingAmount > 0) {
            FluidStack remainingFluid = new FluidStack(currentFluid.getFluid(), remainingAmount);
            tank.setFluid(remainingFluid);
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
    }
}
