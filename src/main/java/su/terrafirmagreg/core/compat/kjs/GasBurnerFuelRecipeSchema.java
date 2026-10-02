package su.terrafirmagreg.core.compat.kjs;

import com.google.gson.JsonElement;
import com.notenoughmail.kubejs_tfc.recipe.component.FluidIngredientComponent;

import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface GasBurnerFuelRecipeSchema {

    RecipeKey<String> ID = StringComponent.ID.key("id").defaultOptional();

    RecipeComponent<FluidStackIngredient> FLUID_STACK_INGREDIENT = new RecipeComponent<>() {
        @Override
        public String componentType() {
            return "fluid_stack_ingredient";
        }

        @Override
        public ComponentRole role() {
            return ComponentRole.INPUT;
        }

        @Override
        public Class<?> componentClass() {
            return FluidStackIngredient.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, FluidStackIngredient value) {
            return value.toJson();
        }

        @Override
        public FluidStackIngredient read(RecipeJS recipe, Object from) {
            return GasBurnerFuelRecipeJS.parseFluidStackIngredient(from);
        }

        @Override
        public boolean isInput(RecipeJS recipe, FluidStackIngredient value, ReplacementMatch match) {
            return FluidIngredientComponent.STACK_INGREDIENT.isInput(recipe, value, match);
        }

        @Override
        public String toString() {
            return componentType();
        }
    };

    RecipeKey<FluidStackIngredient> FLUID = FLUID_STACK_INGREDIENT.key("fluid").defaultOptional();
    RecipeKey<Integer> DURATION = NumberComponent.INT.key("duration").defaultOptional();
    RecipeKey<Integer> TEMPERATURE = NumberComponent.INT.key("temperature").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(
            GasBurnerFuelRecipeJS.class,
            GasBurnerFuelRecipeJS::new,
            ID,
            FLUID,
            DURATION,
            TEMPERATURE)
            .constructor(FLUID, DURATION, TEMPERATURE)
            .constructor((recipe, schemaType, keys, from) -> {
                String idVal = from.getValue(recipe, ID);
                if (idVal != null && !idVal.isEmpty()) {
                    recipe.id(ResourceLocation.tryParse(idVal));
                }
            }, ID)
            .constructor((recipe, schemaType, keys, from) -> {
            });
}
