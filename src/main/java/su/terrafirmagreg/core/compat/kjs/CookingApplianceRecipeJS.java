package su.terrafirmagreg.core.compat.kjs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.notenoughmail.kubejs_tfc.recipe.component.ItemProviderComponent;
import com.notenoughmail.kubejs_tfc.util.implementation.ItemStackProviderJS;

import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.common.recipes.ingredients.IngredientType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.util.MapJS;

@SuppressWarnings("unused")
public class CookingApplianceRecipeJS extends RecipeJS {

    private static final Pattern COUNT_FIRST_PATTERN = Pattern.compile("^(\\d+)\\s*x\\s*(.+)$");
    private static final Pattern COUNT_LAST_PATTERN = Pattern.compile("^(.+)\\s+(\\d+)$");

    private final List<Ingredient> itemIngredients = new ArrayList<>();
    private final List<FluidStackIngredient> fluidIngredients = new ArrayList<>();
    private final List<ItemStackProviderJS> itemOutputs = new ArrayList<>();
    private final List<JsonObject> fluidOutputs = new ArrayList<>();
    private Ingredient catalyst = Ingredient.EMPTY;
    private Ingredient tool = Ingredient.EMPTY;
    private int toolDamage = 1;
    private int duration = 100;
    private float temperature = 0.0f;

    public CookingApplianceRecipeJS itemIngredients(Object... ingredients) {
        for (Object ingredient : ingredients) {
            itemIngredient(ingredient);
        }
        return this;
    }

    public CookingApplianceRecipeJS itemInputs(Object... ingredients) {
        return itemIngredients(ingredients);
    }

    public CookingApplianceRecipeJS itemIngredient(Object ingredient) {
        if (ingredient instanceof Ingredient ing) {
            this.itemIngredients.add(ing);
        } else if (ingredient != null) {
            InputItem inputItem = readInputItem(ingredient);
            if (inputItem != null && !inputItem.isEmpty()) {
                for (int i = 0; i < Math.max(1, inputItem.count); i++) {
                    this.itemIngredients.add(inputItem.ingredient);
                }
            }
        }
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS itemInput(Object ingredient) {
        return itemIngredient(ingredient);
    }

    public CookingApplianceRecipeJS fluidIngredients(Object... fluids) {
        for (Object fluid : fluids) {
            fluidIngredient(fluid);
        }
        return this;
    }

    public CookingApplianceRecipeJS fluidInputs(Object... fluids) {
        return fluidIngredients(fluids);
    }

    public CookingApplianceRecipeJS fluidIngredient(Object fluid) {
        FluidStackIngredient fsi = GasBurnerFuelRecipeJS.parseFluidStackIngredient(fluid);
        if (fsi != null && fsi.amount() > 0 && !fsi.ingredient().entries().isEmpty()) {
            this.fluidIngredients.add(fsi);
        }
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS fluidInput(Object fluid) {
        return fluidIngredient(fluid);
    }

    public CookingApplianceRecipeJS fluidIngredient(Object fluid, int amount) {
        FluidStackIngredient fsi = GasBurnerFuelRecipeJS.parseFluidStackIngredient(fluid, amount);
        if (fsi != null && fsi.amount() > 0 && !fsi.ingredient().entries().isEmpty()) {
            this.fluidIngredients.add(fsi);
        }
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS fluidInput(Object fluid, int amount) {
        return fluidIngredient(fluid, amount);
    }

    public CookingApplianceRecipeJS catalyst(Object catalyst) {
        if (catalyst instanceof Ingredient ing) {
            this.catalyst = ing;
        } else if (catalyst != null) {
            InputItem inputItem = readInputItem(catalyst);
            if (inputItem != null && !inputItem.isEmpty()) {
                this.catalyst = inputItem.ingredient;
            }
        }
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS tool(Object tool) {
        if (tool instanceof Ingredient ing) {
            this.tool = ing;
        } else if (tool != null) {
            InputItem inputItem = readInputItem(tool);
            if (inputItem != null && !inputItem.isEmpty()) {
                this.tool = inputItem.ingredient;
            }
        }
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS tool(Object tool, int damage) {
        this.toolDamage = damage;
        return tool(tool);
    }

    public CookingApplianceRecipeJS toolDamage(int damage) {
        this.toolDamage = damage;
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS itemOutputs(Object... outputs) {
        for (Object output : outputs) {
            itemOutput(output);
        }
        return this;
    }

    public CookingApplianceRecipeJS results(Object... outputs) {
        return itemOutputs(outputs);
    }

    public CookingApplianceRecipeJS itemOutput(Object output) {
        if (output instanceof ItemStackProviderJS isp) {
            this.itemOutputs.add(isp);
        } else if (output != null) {
            this.itemOutputs.add(ItemProviderComponent.PROVIDER.read(this, output));
        }
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS result(Object output) {
        return itemOutput(output);
    }

    public CookingApplianceRecipeJS fluidOutputs(Object... fluids) {
        for (Object fluid : fluids) {
            fluidOutput(fluid);
        }
        return this;
    }

    public void fluidOutput(Object fluid) {
        JsonObject obj = parseFluidOutput(fluid, -1);
        if (obj != null) {
            this.fluidOutputs.add(obj);
        }
        updateJson();
    }

    public CookingApplianceRecipeJS fluidOutput(Object fluid, int amount) {
        JsonObject obj = parseFluidOutput(fluid, amount);
        if (obj != null) {
            this.fluidOutputs.add(obj);
        }
        updateJson();
        return this;
    }

    public static JsonObject parseFluidOutput(Object fluid, int explicitAmount) {
        if (fluid == null) {
            return null;
        }

        if (fluid instanceof FluidStack fs) {
            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fs.getFluid())).toString());
            obj.addProperty("amount", explicitAmount > 0 ? explicitAmount : fs.getAmount());
            if (fs.hasTag()) {
                obj.add("nbt", MapJS.json(fs.getTag()));
            }
            return obj;
        }

        if (fluid instanceof JsonObject obj) {
            return obj;
        }

        if (fluid instanceof CharSequence cs) {
            String str = cs.toString().trim();
            int amount = explicitAmount > 0 ? explicitAmount : 1000;
            String fluidStr = str;

            Matcher countFirst = COUNT_FIRST_PATTERN.matcher(str);
            if (countFirst.matches()) {
                if (explicitAmount <= 0) {
                    amount = Integer.parseInt(countFirst.group(1));
                }
                fluidStr = countFirst.group(2).trim();
            } else {
                Matcher countLast = COUNT_LAST_PATTERN.matcher(str);
                if (countLast.matches()) {
                    if (explicitAmount <= 0) {
                        amount = Integer.parseInt(countLast.group(2));
                    }
                    fluidStr = countLast.group(1).trim();
                }
            }

            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", fluidStr);
            obj.addProperty("amount", amount);
            return obj;
        }

        FluidStackIngredient parsed = GasBurnerFuelRecipeJS.parseFluidStackIngredient(fluid, explicitAmount);
        if (parsed != null && parsed.amount() > 0 && !parsed.ingredient().entries().isEmpty()) {
            var entry = parsed.ingredient().entries().get(0);
            if (entry instanceof IngredientType.ObjEntry<Fluid> objEntry) {
                JsonObject obj = new JsonObject();
                obj.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(objEntry.object())).toString());
                obj.addProperty("amount", parsed.amount());
                return obj;
            }
        }

        return null;
    }

    public CookingApplianceRecipeJS duration(int durationTicks) {
        this.duration = durationTicks;
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS cookingTime(int durationTicks) {
        return duration(durationTicks);
    }

    public CookingApplianceRecipeJS temperature(float minTemp) {
        this.temperature = minTemp;
        updateJson();
        return this;
    }

    public CookingApplianceRecipeJS minTemp(float minTemp) {
        return temperature(minTemp);
    }

    private void updateJson() {
        if (!itemIngredients.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (Ingredient ing : itemIngredients) {
                arr.add(ing.toJson());
            }
            json.add("item_ingredients", arr);
        }

        if (!fluidIngredients.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (FluidStackIngredient fsi : fluidIngredients) {
                arr.add(fsi.toJson());
            }
            json.add("fluid_ingredients", arr);
        }

        if (catalyst != null && !catalyst.isEmpty()) {
            json.add("catalyst", catalyst.toJson());
        }

        if (tool != null && !tool.isEmpty()) {
            json.add("tool", tool.toJson());
            json.addProperty("tool_damage", toolDamage);
        }

        if (!itemOutputs.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (ItemStackProviderJS isp : itemOutputs) {
                arr.add(isp.toJson());
            }
            json.add("item_outputs", arr);
        }

        if (!fluidOutputs.isEmpty()) {
            JsonArray arr = new JsonArray();
            for (JsonObject fo : fluidOutputs) {
                arr.add(fo);
            }
            json.add("fluid_outputs", arr);
        }

        json.addProperty("duration", duration);
        json.addProperty("temperature", temperature);
    }
}
