package su.terrafirmagreg.core.common.tfgt.machine.trait;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.resources.ResourceLocation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TFCAnvilRecipeData {

    private ResourceLocation id;

    private SizedIngredient input;

    private int minTier;

    private ForgeRule[] rules;

    private boolean applyForgingBonus;

    private ItemStackProvider outputIsp;

}
