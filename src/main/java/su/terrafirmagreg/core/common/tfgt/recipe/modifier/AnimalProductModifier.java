package su.terrafirmagreg.core.common.tfgt.recipe.modifier;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.world.entity.Entity;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.PastoralEngineMachine;
import su.terrafirmagreg.core.common.tfgt.recipe.condition.AnimalPresentCondition;

public class AnimalProductModifier {

    public static final RecipeModifier INSTANCE = AnimalProductModifier::modify;

    private static @NotNull ModifierFunction modify(
            MetaMachine machine, @NotNull GTRecipe recipe) {

        var level = machine.getLevel();
        if (level == null)
            return ModifierFunction.NULL;

        // Grab condition AnimalPresentCondition from recipe
        AnimalPresentCondition condition = null;
        for (var c : recipe.conditions) {
            if (c instanceof AnimalPresentCondition apc) {
                condition = apc;
                break;
            }
        }

        final AnimalPresentCondition finalCondition = condition;

        if (!(machine instanceof PastoralEngineMachine pastoral))
            return ModifierFunction.NULL;

        int readyCount = 0;
        for (Entity entity : pastoral.getCachedAnimals()) {
            if (!(entity instanceof TFCAnimalProperties animal))
                continue;
            if (animal.getAgeType() == TFCAnimalProperties.Age.OLD)
                continue;
            if (!animal.isReadyForAnimalProduct())
                continue;
            if (finalCondition == null || finalCondition.matchesEntity(entity))
                readyCount++;
        }

        if (readyCount <= 0)
            return ModifierFunction.NULL;

        int parallel = Math.min(readyCount, 16);

        // Can parallel depending of the amount of animals ( Filter to only count the same one used in the recipe )

        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallel))
                .outputModifier(ContentModifier.multiplier(parallel))
                .parallels(parallel)
                .build();
    }
}
