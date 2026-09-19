package su.terrafirmagreg.core.mixins.client.tmrv;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

// see https://github.com/Nolij/TooManyRecipeViewers/issues/101

@Mixin(targets = "dev.nolij.toomanyrecipeviewers.impl.recipe.TMRVRecipe", remap = false)
public abstract class TMRVRecipeMixin {

    @Shadow(remap = false)
    private List<EmiIngredient> inputs;

    @Shadow(remap = false)
    private List<EmiStack> outputs;

    @ModifyReturnValue(method = "getInputs", at = @At("RETURN"))
    private List<EmiIngredient> tfg$getInputs(List<EmiIngredient> rebuilt) {
        this.inputs = rebuilt;
        return rebuilt;
    }

    @ModifyReturnValue(method = "getOutputs", at = @At("RETURN"))
    private List<EmiStack> tfg$getOutputs(List<EmiStack> rebuilt) {
        this.outputs = rebuilt;
        return rebuilt;
    }
}
