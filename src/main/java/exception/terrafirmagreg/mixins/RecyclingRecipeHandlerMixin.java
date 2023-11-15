package exception.terrafirmagreg.mixins;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.data.recipe.generated.RecyclingRecipeHandler;
import exception.terrafirmagreg.compat.gregtech.TFGTagPrefixes;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.generated.RecyclingRecipeHandler.processCrushing;

@Mixin(value = RecyclingRecipeHandler.class, remap = false)
public class RecyclingRecipeHandlerMixin {

    /**
     * Генерация раскрафтов оголовий инструментов.
     */
    @Inject(method = "init", at = @At(value = "TAIL"), remap = false)
    private static void onInit(Consumer<FinishedRecipe> provider, CallbackInfo ci) {
        TFGTagPrefixes.toolHeadMiningHammer.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadSword.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadPickaxe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadShovel.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadAxe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadHoe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadScythe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadFile.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadHammer.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadSaw.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadKnife.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
        TFGTagPrefixes.toolHeadButcheryKnife.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, provider));
    }
}
