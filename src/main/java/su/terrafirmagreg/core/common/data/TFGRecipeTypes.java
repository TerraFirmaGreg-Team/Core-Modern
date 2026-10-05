package su.terrafirmagreg.core.common.data;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.recipe.ArtisanRecipe;
import su.terrafirmagreg.core.common.recipe.GasBurnerFuelRecipe;

public class TFGRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister
            .create(ForgeRegistries.RECIPE_TYPES, TFGCore.MOD_ID);

    public static final RegistryObject<RecipeType<ArtisanRecipe>> ARTISAN = register("artisan");
    public static final RegistryObject<RecipeType<GasBurnerFuelRecipe>> GAS_BURNER_FUEL = register("gas_burner_fuel");

    private static <R extends Recipe<?>> RegistryObject<RecipeType<R>> register(String name) {
        return RECIPE_TYPES.register(name, () -> new RecipeType<R>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }

}
