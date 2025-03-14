package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.data.recipe.generated.RecyclingRecipeHandler;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix.*;

@Mixin(value = RecyclingRecipeHandler.class, remap = false)
public abstract class RecyclingRecipeHandlerMixin {

    /**
     * В основном исправляет прикол, из-за которого не разбираются оголовья из GTM.
     * */
//    @Inject(method = "init", at = @At("TAIL"), remap = false)
//    private static void init(Consumer<FinishedRecipe> provider, CallbackInfo ci) {
//        toolHeadMiningHammer.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadSword.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadPickaxe.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadShovel.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadAxe.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadHoe.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadScythe.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadFile.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadHammer.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadSaw.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadKnife.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadButcheryKnife.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadSpade.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadPropick.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadJavelin.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadChisel.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadMace.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//
//        /* Оголовья грега, которые почему-то не имеют декрафта */
//        toolHeadBuzzSaw.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadScrewdriver.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadDrill.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadChainsaw.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//        toolHeadWrench.executeHandler(provider, PropertyKey.DUST, RecyclingRecipeHandler::processCrushing);
//    }

}
