package su.terrafirmagreg.core.common.data;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.recipes.ArtisanRecipe;

public class TFGRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TFGCore.MOD_ID);

    public static final RegistryObject<ArtisanRecipe.Serializer> ARTISAN = RECIPE_SERIALIZERS.register("artisan", ArtisanRecipe.Serializer::new);
}
