package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler;
import net.minecraft.data.recipes.FinishedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.recipes.builders.tfc.TFGAdvancedShapedToolRecipeBuilder;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.generated.ToolRecipeHandler.addToolRecipe;

@Mixin(value = ToolRecipeHandler.class, remap = false)
public abstract class ToolRecipeHandlerMixin {

    /**
     * Замена ванильного камня в рецепте ступки на тфк камень.
     */
    @Redirect(method = "registerMortarRecipes", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V"), remap = false)
    private static void tfg$registerMortarRecipes$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        addToolRecipe(provider, material, GTToolType.MORTAR, false,
                " I ", "SIS", "SSS",
                'I', new UnificationEntry(material.hasProperty(PropertyKey.GEM) ? TagPrefix.gem : TagPrefix.ingot, material),
                'S', TFGTags.Items.RawRockBlocks);
    }

    /**
     * Отключение рецептов MiningHammer, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 0), remap = false)
    private static void tfg$processMiningHammer$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.MINING_HAMMER, TFGTagPrefix.toolHeadMiningHammer);
        else
            new TFGAdvancedShapedToolRecipeBuilder("mining_hammer_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadMiningHammer, material))
                    .output(ToolHelper.get(GTToolType.MINING_HAMMER, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Spade, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 1), remap = false)
    private static void processSpade$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.SPADE, TFGTagPrefix.toolHeadSpade);
        else
            new TFGAdvancedShapedToolRecipeBuilder("spade_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadSpade, material))
                    .output(ToolHelper.get(GTToolType.SPADE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Saw, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 2), remap = false)
    private static void tfg$processSaw$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.SAW, TFGTagPrefix.toolHeadSaw);
        else
            new TFGAdvancedShapedToolRecipeBuilder("saw_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadSaw, material))
                    .output(ToolHelper.get(GTToolType.SAW, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Axe, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 3), remap = false)
    private static void tfg$processAxe$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.AXE, TFGTagPrefix.toolHeadAxe);
        else
            new TFGAdvancedShapedToolRecipeBuilder("axe_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadAxe, material))
                    .output(ToolHelper.get(GTToolType.AXE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Hoe, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 4), remap = false)
    private static void tfg$processHoe$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.HOE, TFGTagPrefix.toolHeadHoe);
        else
            new TFGAdvancedShapedToolRecipeBuilder("hoe_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadHoe, material))
                    .output(ToolHelper.get(GTToolType.HOE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Pickaxe, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 5), remap = false)
    private static void tfg$processPickaxe$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.PICKAXE, TFGTagPrefix.toolHeadPickaxe);
        else
            new TFGAdvancedShapedToolRecipeBuilder("pickaxe_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadPickaxe, material))
                    .output(ToolHelper.get(GTToolType.PICKAXE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Scythe, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 6), remap = false)
    private static void tfg$processScythe$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.SCYTHE, TFGTagPrefix.toolHeadScythe);
        else
            new TFGAdvancedShapedToolRecipeBuilder("scythe_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadScythe, material))
                    .output(ToolHelper.get(GTToolType.SCYTHE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Shovel, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 7), remap = false)
    private static void tfg$processShovel$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.SHOVEL, TFGTagPrefix.toolHeadShovel);
        else
            new TFGAdvancedShapedToolRecipeBuilder("shovel_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadShovel, material))
                    .output(ToolHelper.get(GTToolType.SHOVEL, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Sword, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 8), remap = false)
    private static void tfg$processSword$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.SWORD, TFGTagPrefix.toolHeadSword);
        else
            new TFGAdvancedShapedToolRecipeBuilder("sword_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadSword, material))
                    .output(ToolHelper.get(GTToolType.SWORD, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Hammer, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 9), remap = false)
    private static void tfg$processHardHammer$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.HARD_HAMMER, TFGTagPrefix.toolHeadHammer);
        else
            new TFGAdvancedShapedToolRecipeBuilder("hammer_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadHammer, material))
                    .output(ToolHelper.get(GTToolType.HARD_HAMMER, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов File, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 10), remap = false)
    private static void tfg$processFile$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.FILE, TFGTagPrefix.toolHeadFile);
        else
            new TFGAdvancedShapedToolRecipeBuilder("file_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadFile, material))
                    .output(ToolHelper.get(GTToolType.FILE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов Knife, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 11), remap = false)
    private static void tfg$processKnife$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.KNIFE, TFGTagPrefix.toolHeadKnife);
        else
            new TFGAdvancedShapedToolRecipeBuilder("knife_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadKnife, material))
                    .output(ToolHelper.get(GTToolType.KNIFE, material))
                    .save(provider);
    }

    /**
     * Отключение рецептов ButcheryKnife, только для металлов, которые дублирует TFC.
     */
    @Redirect(method = "processTool", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/data/recipe/generated/ToolRecipeHandler;addToolRecipe(Ljava/util/function/Consumer;Lcom/gregtechceu/gtceu/api/data/chemical/material/Material;Lcom/gregtechceu/gtceu/api/item/tool/GTToolType;Z[Ljava/lang/Object;)V", ordinal = 13), remap = false)
    private static void tfg$processButcheryKnife$toolRecipeHandler$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType tool, boolean mirrored, Object[] recipe) {
        if (!material.hasFlag(TFGMaterialFlags.HAS_TFC_TOOL))
            tfg$addToolRecipe(provider, material, GTToolType.BUTCHERY_KNIFE, TFGTagPrefix.toolHeadButcheryKnife);
        else
            new TFGAdvancedShapedToolRecipeBuilder("butchery_knife_" + material.getName())
                    .input(ChemicalHelper.get(TFGTagPrefix.toolHeadButcheryKnife, material))
                    .output(ToolHelper.get(GTToolType.BUTCHERY_KNIFE, material))
                    .save(provider);
    }

    @Unique
    private static void tfg$addToolRecipe(Consumer<FinishedRecipe> provider, Material material, GTToolType toolType, TagPrefix tagPrefix) {
        addToolRecipe(provider, material, toolType, false, "H", "S", 'H', new UnificationEntry(tagPrefix, material), 'S', TFGTags.Items.WoodenSticks);
    }
}
