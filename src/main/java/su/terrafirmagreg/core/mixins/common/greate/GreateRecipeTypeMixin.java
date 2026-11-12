package su.terrafirmagreg.core.mixins.common.greate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import electrolyte.greate.compat.gtceu.api.recipe.GreateRecipeType;

@Mixin(value = GreateRecipeType.class, remap = false)
public class GreateRecipeTypeMixin {

    @ModifyArg(method = "toGTrecipe", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;circuitMeta(I)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;"))
    private int tfg$clampStonecuttingCircuit(int circuit) {
        return Math.min(circuit, 32);
    }
}
