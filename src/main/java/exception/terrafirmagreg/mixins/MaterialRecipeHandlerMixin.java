package exception.terrafirmagreg.mixins;

import com.gregtechceu.gtceu.data.recipe.generated.MaterialRecipeHandler;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(value = MaterialRecipeHandler.class, remap = false)
public class MaterialRecipeHandlerMixin {

    /**
     * Генерация рецептов 2x ingot -> 1x plate
     */
    @Redirect(method = "processIngot", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/VanillaRecipeHelper;addShapedRecipe(Ljava/util/function/Consumer;Ljava/lang/String;Lnet/minecraft/world/item/ItemStack;[Ljava/lang/Object;)V", ordinal = 2), remap = false)
    private static void onPlateRecipeGeneration(Consumer<FinishedRecipe> provider, String regName, ItemStack result, Object[] recipe) {}
}
