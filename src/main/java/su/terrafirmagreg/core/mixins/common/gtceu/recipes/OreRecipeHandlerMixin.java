package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

@Mixin(value = OreRecipeHandler.class, remap = false)
public abstract class OreRecipeHandlerMixin {

    /**
     * Перезаписываем весь метод, потому что в декомпилированном виде там просто жесть,
     * поэтому проще будет полностью его заменить, нужен для установки генераторов рецептов
     * для некоторых руд и фикса краша из-за переработки несуществующих обычных руд GTCEu.
     * */
    @Inject(method = "init", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void tfg$init(Consumer<FinishedRecipe> provider, CallbackInfo ci) {
        for (TagPrefix orePrefix : ORES.keySet()) {
            if (ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes || ORES.get(orePrefix).shouldDropAsItem()) {
                orePrefix.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processOre);
                orePrefix.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processOreForgeHammer);
            }
        }
        crushed.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedOre);
        crushedPurified.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedPurified);
        crushedRefined.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedCentrifuged);
        dustImpure.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processDirtyDust);
        dustPure.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processPureDust);

        ci.cancel();
    }
}
