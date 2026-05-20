package su.terrafirmagreg.core.mixins.common.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.crafter.RecipeGridHandler;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;

@Mixin(value = RecipeGridHandler.class, remap = false)
public class RecipeGridHandlerMixin {
    @Inject(method = "isRecipeAllowed", at = @At("HEAD"), cancellable = true)
    private static void tfg$blockTFCFoodCombining(CraftingRecipe recipe, CraftingContainer inventory, CallbackInfoReturnable<Boolean> cir) {
        // Fixes dupe caused by creating stacks bigger than the max
        if (recipe.getId() != null && recipe.getId().toString().equals("tfc:food_combining")) {
            cir.setReturnValue(false);
        }
    }
}
