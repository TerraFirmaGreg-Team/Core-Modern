package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.terrafirmagreg.core.TFGCore;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix.poorRawOre;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefix.richRawOre;

@Mixin(value = OreRecipeHandler.class, remap = false)
public abstract class OreRecipeHandlerMixin {

    @Shadow
    private static boolean doesMaterialUseNormalFurnace(Material material) {
        throw new AssertionError();
    }

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

        poorRawOre.executeHandler(provider, PropertyKey.ORE, OreRecipeHandlerMixin::tfg$processPoorRawOre);
        rawOre.executeHandler(provider, PropertyKey.ORE, OreRecipeHandlerMixin::tfg$processRawOre);
        richRawOre.executeHandler(provider, PropertyKey.ORE, OreRecipeHandlerMixin::tfg$processRichRawOre);

        crushed.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedOre);
        crushedPurified.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedPurified);
        crushedRefined.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processCrushedCentrifuged);
        dustImpure.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processDirtyDust);
        dustPure.executeHandler(provider, PropertyKey.ORE, OreRecipeHandler::processPureDust);

        ci.cancel();
    }

    /**
     * Исправление бага GTCEu.
     * Если засунуть в for использование этого метода,
     * то будет куча дубликатов рецептов из-за неверной работы .inputItems(TagPrefix, Material).
     * */
    @Redirect(method = "processOreForgeHammer", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;recipeBuilder(Ljava/lang/String;[Ljava/lang/Object;)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;"), remap = false)
    private static GTRecipeBuilder tfg$processOreForgeHammer(GTRecipeType instance, String id, Object[] append, TagPrefix orePrefix, Material material) {
        return instance.recipeBuilder(TFGCore.id(id))
                .inputItems(ChemicalHelper.get(orePrefix, material));

    }

    /**
     * Метод переработки бедных кусков, в основном скопирован с processRawOre, но с некоторыми изменениями.
     * */
    @Unique
    private static void tfg$processPoorRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        int amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(nugget, smeltingMaterial, 5);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gemChipped, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dustTiny, smeltingMaterial, 5);
        }
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier());
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + orePrefix.name + "_" + material.getName() + "_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .category(GTRecipeCategories.ORE_FORGING)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.chancedOutput(GTUtil.copyAmount(amountOfCrushedOre, ChemicalHelper.get(gem, material, crushedStack.getCount())), 7500, 950);
            } else {
                builder.chancedOutput(GTUtil.copyAmount(amountOfCrushedOre, crushedStack), 7500, 950);
            }
            builder.save(provider);

            MACERATOR_RECIPES.recipeBuilder("macerate_" + orePrefix.name + "_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .chancedOutput(crushedStack, 5000, 750)
                    .chancedOutput(crushedStack, 2500, 500)
                    .chancedOutput(crushedStack, 1250, 250)
                    .EUt(2)
                    .category(GTRecipeCategories.ORE_CRUSHING)
                    .duration(400)
                    .save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;

            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }
    }

    /**
     * Метод переработки бедных кусков, в основном скопирован с processRawOre, но с некоторыми изменениями.
     * */
    @Unique
    private static void tfg$processRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        int amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier());
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + orePrefix.name + "_" + material.getName() + "_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .category(GTRecipeCategories.ORE_FORGING)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre, ChemicalHelper.get(gem, material, crushedStack.getCount())));
            } else {
                builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre, crushedStack));
            }
            builder.save(provider);

            MACERATOR_RECIPES.recipeBuilder("macerate_" + orePrefix.name + "_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .outputItems(crushedStack)
                    .chancedOutput(crushedStack.copyWithCount(1), 5000, 750)
                    .chancedOutput(crushedStack.copyWithCount(1), 2500, 500)
                    .chancedOutput(crushedStack.copyWithCount(1), 1250, 250)
                    .EUt(2)
                    .duration(400)
                    .category(GTRecipeCategories.ORE_CRUSHING)
                    .save(provider);
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;

            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }
    }

    /**
     * Метод переработки богатых кусков, в основном скопирован с processRawOre, но с некоторыми изменениями.
     * */
    @Unique
    private static void tfg$processRichRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        ItemStack ingotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        int amountOfCrushedOre = property.getOreMultiplier() * 2;
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * 2);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier() * 2);

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + orePrefix.name + "_" + material.getName() + "_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .category(GTRecipeCategories.ORE_FORGING)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre, ChemicalHelper.get(gem, material, crushedStack.getCount())));
            } else {
                builder.outputItems(GTUtil.copyAmount(amountOfCrushedOre, crushedStack));
            }
            builder.save(provider);

            MACERATOR_RECIPES.recipeBuilder("macerate_" + orePrefix.name + "_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .outputItems(crushedStack)
                    .chancedOutput(crushedStack.copyWithCount(1), 5000, 750)
                    .chancedOutput(crushedStack.copyWithCount(1), 2500, 500)
                    .chancedOutput(crushedStack.copyWithCount(1), 1250, 250)
                    .category(GTRecipeCategories.ORE_CRUSHING)
                    .EUt(2)
                    .duration(400)
                    .save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;

            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }


    }

}
