package su.terrafirmagreg.core.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceLocation;

import brachy.modularui.integration.emi.EmiStackConverter;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

import su.terrafirmagreg.core.TFGCore;

public class EmiOreProcessing extends ModularUIEmiRecipe {

    final Material material;
    final TFGOreByProduct byProduct;

    public EmiOreProcessing(Material material) {
        super(id(material), () -> new TFGOreProcessingRecipeWidget(material));
        this.material = material;
        this.byProduct = new TFGOreByProduct(material);
    }

    private static ResourceLocation id(Material material) {
        ResourceLocation loc = material.getResourceLocation();
        return TFGCore.id("/ore_processing_diagram/" + loc.getNamespace() + "/" + loc.getPath());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.ORE_PROCESSING_DIAGRAM;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> ingredients = new ArrayList<>();
        byProduct.getItemInputs()
                .forEach(v -> ingredients.add(EmiStackConverter.ITEM.convertTo(v, 1, UnaryOperator.identity())));
        byProduct.getFluidInputs()
                .forEach(v -> ingredients.add(EmiStackConverter.FLUID.convertTo(v, 1, UnaryOperator.identity())));
        return ingredients;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return byProduct.getItemOutputs().stream().map(EmiStack::of).toList();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
