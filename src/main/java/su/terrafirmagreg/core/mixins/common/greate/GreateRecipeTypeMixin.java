package su.terrafirmagreg.core.mixins.common.greate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.resources.ResourceLocation;

import electrolyte.greate.compat.gtceu.api.recipe.GreateRecipeType;

import su.terrafirmagreg.core.TFGCore;

@Mixin(value = GreateRecipeType.class, remap = false)
public class GreateRecipeTypeMixin {

    @ModifyArg(method = "toGTrecipe", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;circuitMeta(I)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;"))
    private int tfg$clampStonecuttingCircuit(int circuit, @Local(name = "id") ResourceLocation id) {
		if (circuit > 32) {
			TFGCore.LOGGER.error("Stonecutting input has more than 32 outputs: {}", id);
		}
        return Math.min(circuit, 32);
    }
}
