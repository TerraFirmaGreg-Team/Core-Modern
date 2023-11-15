package exception.terrafirmagreg.mixins;

import com.gregtechceu.gtceu.data.recipe.generated.PartsRecipeHandler;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(value = PartsRecipeHandler.class, remap = false)
public class PartsRecipeHandlerMixin {

    /**
     * Генерация рецептов 2x plate -> 1x doublePlate
     */
    @Redirect(method = "processPlateDouble", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/VanillaRecipeHelper;addShapedRecipe(Ljava/util/function/Consumer;Ljava/lang/String;Lnet/minecraft/world/item/ItemStack;[Ljava/lang/Object;)V"), remap = false)
    private static void onPlateRecipeGeneration(Consumer<FinishedRecipe> provider, String regName, ItemStack result, Object[] recipe) {
    }

    /**
     * Генерация рецептов 2x stick -> 1x longStick
     */
    @Redirect(method = "processLongStick", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/VanillaRecipeHelper;addShapedRecipe(Ljava/util/function/Consumer;Ljava/lang/String;Lnet/minecraft/world/item/ItemStack;[Ljava/lang/Object;)V", ordinal = 3), remap = false)
    private static void onLongStickRecipeGeneration(Consumer<FinishedRecipe> provider, String regName, ItemStack result, Object[] recipe) {
    }
}
