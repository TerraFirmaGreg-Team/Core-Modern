package su.terrafirmagreg.core.common.tfgt.recipe.modifier;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.PastoralEngineMachine;
import su.terrafirmagreg.core.common.tfgt.recipe.condition.AnimalPresentCondition;

public class AnimalProductModifier {

    public static final RecipeModifier INSTANCE = AnimalProductModifier::modify;

    private static @NotNull ModifierFunction modify(
            MetaMachine machine, @NotNull GTRecipe recipe) {

        var level = machine.getLevel();
        if (level == null)
            return ModifierFunction.NULL;

        AABB box = getSearchBox(machine);

        // Grab condition AnimalPresentCondition from recipe
        AnimalPresentCondition condition = null;
        for (var c : recipe.conditions) {
            if (c instanceof AnimalPresentCondition apc) {
                condition = apc;
                break;
            }
        }

        final AnimalPresentCondition finalCondition = condition;

        int readyCount = level.getEntities((Entity) null, box, entity -> {
            if (!(entity instanceof TFCAnimalProperties animal))
                return false;
            if (animal.getAgeType() == TFCAnimalProperties.Age.OLD)
                return false;
            if (!animal.isReadyForAnimalProduct())
                return false;

            // If no condition count everything
            if (finalCondition == null)
                return true;

            // Use filter on the condition so it parallels only the same animals
            return finalCondition.matchesEntity(entity);
        }).size();

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

    // Use for the search Box from PastoralEngineMachine

    private static AABB getSearchBox(MetaMachine machine) {
        if (machine instanceof PastoralEngineMachine pastoral) {
            return pastoral.getFormedBoundingBox();
        }
        return new AABB(machine.getPos()).inflate(2.5);
    }
}
