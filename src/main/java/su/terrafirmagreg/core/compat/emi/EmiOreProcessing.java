package su.terrafirmagreg.core.compat.emi;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;

public class EmiOreProcessing extends ModularEmiRecipe<TFGOreByProductWidget> {

    final Material material;

    public EmiOreProcessing(Material material) {
        super(() -> new TFGOreByProductWidget(material));
        this.material = material;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return TFGEmiPlugin.ORE_PROCESSING_DIAGRAM;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return material.getResourceLocation();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
