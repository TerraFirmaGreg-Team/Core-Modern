package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefixes;
import su.terrafirmagreg.core.objects.TFGTags;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler.addToolRecipe;
import static su.terrafirmagreg.core.objects.TFGTags.AllWoodenSticks;

@Mixin(value = ToolRecipeHandler.class, remap = false)
public class ToolRecipeHandlerMixin {

    /**
     * Отключение генерации рецептов под инструменты из кремния.
     */
    @Redirect(method = "registerCustomToolRecipes", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;registerFlintToolRecipes(Ljava/util/function/Consumer;)V"), remap = false)
    private static void redirectRegisterFlintToolRecipes(Consumer<FinishedRecipe> provider) {
    }

    /**
     * Отключение рецепта miningHammer
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 0), remap = false)
    private static void processMiningHammer(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }



    /**
     * Отключение рецепта saw
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 1), remap = false)
    private static void processSaw(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта axe
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 2), remap = false)
    private static void processAxe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта hoe
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 3), remap = false)
    private static void processHoe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта pickaxe
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 4), remap = false)
    private static void processPickaxe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта scythe
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 5), remap = false)
    private static void processScythe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта shovel
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 6), remap = false)
    private static void processShovel(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта sword
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 7), remap = false)
    private static void processSword(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта hammer
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 8), remap = false)
    private static void processHammer(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта file
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 9), remap = false)
    private static void processFile(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта knife
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 10), remap = false)
    private static void processKnife(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Отключение рецепта buthcheryKnife
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 13), remap = false)
    private static void processButcheryKnife(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
    }

    /**
     * Фикс рецептов mortar
     */
    @Redirect(method = "registerMortarRecipes", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V"), remap = false)
    private static void registerMortarRecipes(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        addToolRecipe(provider, material, GTToolType.MORTAR, false,
                " I ", "SIS", "SSS",
                'I', new UnificationEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', TFGTags.AllRockRawBlocks);
    }
}
