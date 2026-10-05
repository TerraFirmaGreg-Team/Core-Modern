package su.terrafirmagreg.core.common.tfgt.recipe.condition;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dries007.tfc.common.entities.livestock.DairyAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeConditions;
import su.terrafirmagreg.core.common.entity.TFGWoolEggProducingAnimal;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.PastoralEngineMachine;

public class AnimalPresentCondition extends RecipeCondition<AnimalPresentCondition> {

    public static final Codec<AnimalPresentCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance)
            .and(Codec.STRING
                    .optionalFieldOf("animal_type", "any")
                    .forGetter(c -> c.animalType))
            .and(Codec.STRING
                    .optionalFieldOf("entity_type", "")
                    .forGetter(c -> c.entityTypeId == null ? "" : c.entityTypeId.toString()))
            .apply(instance, (isReverse, animalType, entityTypeStr) -> {
                ResourceLocation entityTypeId = entityTypeStr.isEmpty()
                        ? null
                        : ResourceLocation.tryParse(entityTypeStr);
                return new AnimalPresentCondition(isReverse, animalType, entityTypeId);
            }));

    // "dairy" | "producing" | "any"
    private final String animalType;

    // If not null then filter on specific entity see kubejs (ex: "tfc:cow")
    @Nullable
    private final ResourceLocation entityTypeId;

    public AnimalPresentCondition() {
        super(false);
        this.animalType = "any";
        this.entityTypeId = null;
    }

    public AnimalPresentCondition(boolean isReverse, String animalType,
            @Nullable ResourceLocation entityTypeId) {
        super(isReverse);
        this.animalType = animalType;
        this.entityTypeId = entityTypeId;
    }

    // Factories

    public static AnimalPresentCondition dairy() {
        return new AnimalPresentCondition(false, "dairy", null);
    }

    public static AnimalPresentCondition producing() {
        return new AnimalPresentCondition(false, "producing", null);
    }

    public static AnimalPresentCondition any() {
        return new AnimalPresentCondition(false, "any", null);
    }

    public static AnimalPresentCondition ofEntityType(String entityTypeId) {
        return new AnimalPresentCondition(false, "any",
                ResourceLocation.tryParse(entityTypeId));
    }

    // RecipeCondition

    @Override
    public RecipeConditionType<AnimalPresentCondition> getType() {
        return TFGRecipeConditions.ANIMAL_PRESENT;
    }

    @Override
    public Component getTooltips() {
        if (entityTypeId != null) {
            // Formate name for langfile in EMI
            String path = entityTypeId.getPath();
            String formatted = Arrays.stream(path.split("_"))
                    .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                    .collect(java.util.stream.Collectors.joining(" "));

            return Component.translatable(
                    "tfg.tooltip.recipe_condition.animal_present.entity",
                    formatted);
        }
        return Component.translatable(
                "tfg.tooltip.recipe_condition.animal_present." + animalType);
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe,
            @NotNull RecipeLogic recipeLogic) {
        var machine = recipeLogic.machine.self();
        if (!(machine.getLevel() instanceof ServerLevel))
            return false;
        if (!(machine instanceof PastoralEngineMachine pastoral))
            return false;

        for (Entity entity : pastoral.getCachedAnimals()) {
            if (matchesEntity(entity))
                return true;
        }
        return false;
    }

    @Override
    public AnimalPresentCondition createTemplate() {
        return new AnimalPresentCondition();
    }

    /*
    Check which animals through the condition
    Used so the Modifier only parallel the right animals
    */
    public boolean matchesEntity(Entity entity) {
        if (!(entity instanceof TFCAnimalProperties animal))
            return false;
        if (animal.getAgeType() == TFCAnimalProperties.Age.OLD)
            return false;

        if (animal instanceof TFGWoolEggProducingAnimal woolAnimal) {
            if (animalType.equals("dairy")) {
                // In case we have a machine in the future that wants eggs
                if (!woolAnimal.isReadyForAnimalProduct())
                    return false;
            } else if (!woolAnimal.hasWool()) {
                return false;
            }
        } else {
            if (!animal.isReadyForAnimalProduct())
                return false;
        }

        // Filter per entity type if specified
        if (entityTypeId != null) {
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            return entityTypeId.equals(key);
        }

        // Else Filter per category
        return switch (animalType) {
            case "dairy" -> entity instanceof DairyAnimal;
            case "producing" -> entity instanceof WoolyAnimal;
            default -> true;
        };
    }
}
