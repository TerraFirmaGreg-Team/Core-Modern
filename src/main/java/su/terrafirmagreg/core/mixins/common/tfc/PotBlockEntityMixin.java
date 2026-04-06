package su.terrafirmagreg.core.mixins.common.tfc;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.recipes.JamPotRecipe;
import net.dries007.tfc.common.recipes.PotRecipe;
import net.dries007.tfc.common.recipes.SoupPotRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * Makes pot recipes consume only the required fluid amount and preserve remainder,
 * Unless the recipe produces a fluid output or is a jam/soup recipe.
 */
@Mixin(value = PotBlockEntity.class, remap = false)
public abstract class PotBlockEntityMixin {

    @Shadow
    @Nullable
    private PotRecipe cachedRecipe;

    @Unique
    private FluidStack tfg$savedInputFluid = FluidStack.EMPTY;

    @Unique
    private int tfg$requiredAmount = 0;

    @Unique
    private boolean tfg$isSoupOrJam = false;

    /**
     * Save the input fluid state before the recipe clears it.
     * Also check if this is a soup or jam recipe.
     */
    @Redirect(method = "handleCooking", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;setFluid(Lnet/minecraftforge/fluids/FluidStack;)V"))
    private void tfg$saveInputFluidBeforeClear(FluidTank tank, FluidStack empty) {
        assert cachedRecipe != null : "cachedRecipe should not be null when handleCooking completes a recipe";

        tfg$savedInputFluid = tank.getFluid().copy();
        tfg$requiredAmount = cachedRecipe.getFluidIngredient().amount();
        tfg$isSoupOrJam = cachedRecipe instanceof SoupPotRecipe || cachedRecipe instanceof JamPotRecipe;

        tank.setFluid(FluidStack.EMPTY);
    }

    /**
     * After output.onFinish() is called, check if it added fluid.
     * If not, restore the remainder from our saved state.
     * Soup and Jam recipes always void remainder.
     */
    @Inject(method = "handleCooking", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/common/recipes/PotRecipe$Output;onFinish(Lnet/dries007/tfc/common/blockentities/PotBlockEntity$PotInventory;)V", shift = At.Shift.AFTER))
    private void tfg$restoreRemainderIfNoOutputFluid(CallbackInfo ci) {
        if (tfg$isSoupOrJam) {
            tfg$savedInputFluid = FluidStack.EMPTY;
            tfg$requiredAmount = 0;
            tfg$isSoupOrJam = false;
            return;
        }

        PotBlockEntity self = (PotBlockEntity) (Object) this;
        PotBlockEntity.PotInventory inventory = (PotBlockEntity.PotInventory) self.getCapability(
                net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).orElseThrow(() -> new IllegalStateException("Pot should have inventory"));

        FluidStack currentFluid = inventory.getFluidInTank(0);

        if (!currentFluid.isEmpty()) {
            tfg$savedInputFluid = FluidStack.EMPTY;
            tfg$isSoupOrJam = false;
            return;
        }

        // No output fluid, restore remainder from saved input.
        if (!tfg$savedInputFluid.isEmpty()) {
            int remainingAmount = Math.max(0, tfg$savedInputFluid.getAmount() - tfg$requiredAmount);

            if (remainingAmount > 0) {
                FluidStack remainingFluid = new FluidStack(tfg$savedInputFluid.getFluid(), remainingAmount);
                inventory.getFluidHandler().fill(remainingFluid, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        // Clear saved state.
        tfg$savedInputFluid = FluidStack.EMPTY;
        tfg$requiredAmount = 0;
        tfg$isSoupOrJam = false;
    }
}
