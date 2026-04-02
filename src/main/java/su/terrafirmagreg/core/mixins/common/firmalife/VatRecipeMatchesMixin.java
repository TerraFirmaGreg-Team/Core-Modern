package su.terrafirmagreg.core.mixins.common.firmalife;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.firmalife.common.blockentities.VatBlockEntity;
import com.eerussianguy.firmalife.common.recipes.VatRecipe;

import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/**
 * Modifies VatRecipe.matches() to check for minimum fluid amount instead of exact match.
 * This allows vat recipes to work with extra fluid in the vat.
 */
@Mixin(value = VatRecipe.class, remap = false)
public abstract class VatRecipeMatchesMixin {

    @Shadow
    @Final
    private ItemStackIngredient inputItem;

    @Shadow
    @Final
    private FluidStackIngredient inputFluid;

    /**
     * Replace the matches method to check for minimum fluid amount.
     * Removes the restrictive ratio check from the original to allow excess fluid.
     * The recipe's assembleOutputs() will calculate the correct multiplier.
     */
    @Inject(method = "matches(Lcom/eerussianguy/firmalife/common/blockentities/VatBlockEntity$VatInventory;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true)
    private void tfg$matchesWithMinimumFluid(VatBlockEntity.VatInventory container, Level level, CallbackInfoReturnable<Boolean> cir) {
        // Check if item matches.
        if (!inputItem.test(container.getStackInSlot(0))) {
            cir.setReturnValue(false);
            return;
        }

        // Check if fluid type matches.
        FluidStack containerFluid = container.getFluidInTank(0);
        if (!inputFluid.ingredient().test(containerFluid.getFluid())) {
            cir.setReturnValue(false);
            return;
        }

        // Check for minimum fluid amount.
        if (containerFluid.getAmount() < inputFluid.amount()) {
            cir.setReturnValue(false);
            return;
        }

        cir.setReturnValue(true);
    }
}
