package su.terrafirmagreg.core.compat.kjs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.notenoughmail.kubejs_tfc.util.helpers.IngredientHelpers;

import net.dries007.tfc.common.recipes.ingredients.FluidIngredient;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;

import dev.latvian.mods.kubejs.recipe.RecipeJS;

@SuppressWarnings("unused")
public class GasBurnerFuelRecipeJS extends RecipeJS {

    private static final Pattern COUNT_FIRST_PATTERN = Pattern.compile("^(\\d+)\\s*x\\s*(.+)$");
    private static final Pattern COUNT_LAST_PATTERN = Pattern.compile("^(.+)\\s+(\\d+)$");

    public GasBurnerFuelRecipeJS fluid(Object fluid) {
        setValue(GasBurnerFuelRecipeSchema.FLUID, parseFluidStackIngredient(fluid));
        return this;
    }

    public GasBurnerFuelRecipeJS fluid(Object fluid, int amount) {
        setValue(GasBurnerFuelRecipeSchema.FLUID, parseFluidStackIngredient(fluid, amount));
        return this;
    }

    public GasBurnerFuelRecipeJS fluidIngredient(Object fluid) {
        return fluid(fluid);
    }

    public GasBurnerFuelRecipeJS fluidIngredient(Object fluid, int amount) {
        return fluid(fluid, amount);
    }

    public GasBurnerFuelRecipeJS duration(int durationTicks) {
        setValue(GasBurnerFuelRecipeSchema.DURATION, durationTicks);
        return this;
    }

    public GasBurnerFuelRecipeJS temperature(int temperature) {
        setValue(GasBurnerFuelRecipeSchema.TEMPERATURE, temperature);
        return this;
    }

    public static FluidStackIngredient parseFluidStackIngredient(Object from) {
        return parseFluidStackIngredient(from, -1);
    }

    public static FluidStackIngredient parseFluidStackIngredient(Object from, int explicitAmount) {
        if (from == null) {
            return FluidStackIngredient.EMPTY;
        }

        if (from instanceof FluidStackIngredient fsi) {
            if (explicitAmount > 0) {
                return new FluidStackIngredient(fsi.ingredient(), explicitAmount);
            }
            return fsi;
        }

        if (from instanceof FluidIngredient fi) {
            return new FluidStackIngredient(fi, explicitAmount > 0 ? explicitAmount : 1000);
        }

        if (from instanceof CharSequence cs) {
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

            FluidIngredient fi = IngredientHelpers.ofFluidIngredient(fluidStr);
            return new FluidStackIngredient(fi, amount);
        }

        FluidStackIngredient fsi = IngredientHelpers.ofFluidStackIngredient(from);
        if (explicitAmount > 0 && fsi != null) {
            return new FluidStackIngredient(fsi.ingredient(), explicitAmount);
        }
        return fsi;
    }
}
