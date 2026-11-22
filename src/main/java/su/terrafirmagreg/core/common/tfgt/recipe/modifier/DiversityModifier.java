package su.terrafirmagreg.core.common.tfgt.recipe.modifier;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.MicrobialCultivatorMachine;
import su.terrafirmagreg.core.common.tfgt.machine.trait.InputHistoryTrait;

/** Scales fluid outputs by how many distinct inputs are in the recent history. */
public final class DiversityModifier implements RecipeModifier {

    @FunctionalInterface
    public interface Int2FloatFunction {
        float apply(int value);
    }

    private final Int2FloatFunction multiplier;

    private DiversityModifier(Int2FloatFunction multiplier) {
        this.multiplier = multiplier;
    }

    public static DiversityModifier of(Int2FloatFunction multiplier) {
        return new DiversityModifier(multiplier);
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        InputHistoryTrait history = machine.getTrait(InputHistoryTrait.class);
        if (history == null)
            return RecipeModifier.nullWrongType(MicrobialCultivatorMachine.class, machine);

        if (history.getActiveInput() == null)
            return ModifierFunction.IDENTITY;

        float factor = multiplier.apply(history.getUniqueCount());
        if (factor == 1f)
            return ModifierFunction.IDENTITY;

        return modified -> {
            GTRecipe scaled = modified.copy();
            scaled.outputs.replaceAll((capability, contents) -> scaleOutputContents(capability, contents, factor));
            return scaled;
        };
    }

    private static List<Content> scaleOutputContents(RecipeCapability<?> capability, List<Content> contents, float factor) {
        if (capability != FluidRecipeCapability.CAP)
            return contents;

        List<Content> scaled = new ArrayList<>(contents.size());
        for (Content content : contents) {
            int amount = ((FluidIngredient) content.content()).getAmount();

            float total = amount * factor * (content.chance() / (float) content.maxChance());
            int whole = (int) total;
            float fraction = total - whole;

            if (whole > 0)
                scaled.add(modifyContent(capability, content, whole, content.maxChance()));
            if (fraction > 0)
                scaled.add(modifyContent(capability, content, 1, Math.round(fraction * content.maxChance())));
        }
        return scaled;
    }

    private static Content modifyContent(RecipeCapability<?> capability, Content content, int amount, int chance) {
        ContentModifier modifier = new ContentModifier(0, amount);
        Object inner = ((RecipeCapability<Object>) capability).copyWithModifier(content.content(), modifier);
        return new Content(inner, chance, content.maxChance());
    }
}
