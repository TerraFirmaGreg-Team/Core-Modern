package su.terrafirmagreg.core.compat.kjs;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

public interface CopperPotRecipeSchema {

    RecipeKey<String> ID = StringComponent.ID.key("id").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(
            CopperPotRecipeJS.class,
            CopperPotRecipeJS::new,
            ID)
            .constructor((recipe, schemaType, keys, from) -> {
                String idVal = from.getValue(recipe, ID);
                if (idVal != null && !idVal.isEmpty()) {
                    recipe.id(ResourceLocation.tryParse(idVal));
                }
            }, ID)
            .constructor((recipe, schemaType, keys, from) -> {
            });
}
