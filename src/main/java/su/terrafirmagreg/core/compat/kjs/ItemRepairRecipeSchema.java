package su.terrafirmagreg.core.compat.kjs;

import net.minecraft.resources.ResourceLocation;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.StringComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/** KubeJS schema for tfg:item_repair; pattern and key live on ItemRepairRecipeJS. */
public interface ItemRepairRecipeSchema {

    RecipeKey<String> ID = StringComponent.ID.key("id").defaultOptional();

    RecipeKey<Float> REPAIR_PERCENTAGE = NumberComponent.FLOAT.key("repairPercentage").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(
            ItemRepairRecipeJS.class,
            ItemRepairRecipeJS::new,
            ID,
            REPAIR_PERCENTAGE)
            .constructor((recipe, schemaType, keys, from) -> recipe.id(ResourceLocation.parse(from.getValue(recipe, ID))), ID)
            .constructor((recipe, schemaType, keys, from) -> {
            });
}
