package su.terrafirmagreg.core.common.data;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.HIGH_SIFTER_OUTPUT;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static su.terrafirmagreg.core.common.data.tfgt.TFGTRecipeTypes.*;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTRecipeCategories;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;

public class TFGOreRecipeHandler {

    private TFGOreRecipeHandler() {
    }

    public static void run(@NotNull Consumer<FinishedRecipe> provider, @NotNull Material material) {
        OreProperty property = material.getProperty(PropertyKey.ORE);
        if (property == null)
            return;

        processCrushedOre(provider, property, material);
        processCrushedPurified(provider, property, material);
        processCrushedCentrifuged(provider, property, material);
        processDirtyDust(provider, property, material);
        processPureDust(provider, property, material);
    }

    private static void processMetalSmelting(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
            @NotNull TagPrefix prefix, @NotNull Material material) {
        Material smeltingResult = property.getDirectSmeltResult().isNull() ? material : property.getDirectSmeltResult();
        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ItemStack ingotStack = ChemicalHelper.get(ingot, smeltingResult);

            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult) && !prefix.isIgnored(material)) {
                String name = "smelt_" + prefix.name + "_" + material.getName() + "_to_ingot";
                TagKey<Item> tag = ChemicalHelper.getTag(prefix, material);

                VanillaRecipeHelper.addSmeltingRecipe(provider, name, tag, ingotStack, 0.5f);
                VanillaRecipeHelper.addBlastingRecipe(provider, name, tag, ingotStack, 0.5f);
            }
        }
    }

    private static void processCrushedOre(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
            @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(crushed))
            return;

        ItemStack impureDustStack = ChemicalHelper.get(dustImpure, material);
        Material byproductMaterial = property.getOreByProduct(0, material);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushed, material)
                .outputItems(impureDustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        TFG_ORE_MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_impure_dust")
                .inputItems(crushed, material)
                .outputItems(impureDustStack)
                .duration(400).EUt(2)
                .chancedOutput(ChemicalHelper.get(dust, byproductMaterial, property.getByProductMultiplier()), 1400, 0)
                .save(provider);

        ItemStack crushedPurifiedOre = GTUtil.copyFirst(
                ChemicalHelper.get(crushedPurified, material),
                ChemicalHelper.get(dust, material));
        ItemStack crushedCentrifugedOre = GTUtil.copyFirst(
                ChemicalHelper.get(crushedRefined, material),
                ChemicalHelper.get(dust, material));

        TFG_ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_fast")
                .inputItems(crushed, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .duration(8).EUt(4).save(provider);

        TFG_ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore")
                .inputItems(crushed, material)
                .inputFluids(Water.getFluid(1000))
                .circuitMeta(1)
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.dust, byproductMaterial, "1/3", 0)
                .outputItems(TagPrefix.dust, Stone)
                .save(provider);

        TFG_ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_crushed_ore_to_purified_ore_distilled")
                .inputItems(crushed, material)
                .inputFluids(DistilledWater.getFluid(100))
                .outputItems(crushedPurifiedOre)
                .chancedOutput(TagPrefix.dust, byproductMaterial, "1/3", 0)
                .outputItems(TagPrefix.dust, Stone)
                .duration(200)
                .save(provider);

        TFG_THERMAL_CENTRIFUGE_RECIPES
                .recipeBuilder("centrifuge_" + material.getName() + "_crushed_ore_to_refined_ore")
                .inputItems(crushed, material)
                .outputItems(crushedCentrifugedOre)
                .chancedOutput(TagPrefix.dust, property.getOreByProduct(1, material),
                        property.getByProductMultiplier(), "1/3", 0)
                .outputItems(TagPrefix.dust, Stone)
                .save(provider);

        if (!property.getWashedIn().first().isNull()) {
            Material washingByproduct = property.getOreByProduct(3, material);
            ObjectIntPair<Material> washedInTuple = property.getWashedIn();
            TFG_CHEMICAL_BATH_RECIPES.recipeBuilder("bathe_" + material.getName() + "_crushed_ore_to_purified_ore")
                    .inputItems(crushed, material)
                    .inputFluids(washedInTuple.first().getFluid(washedInTuple.secondInt()))
                    .outputItems(crushedPurifiedOre)
                    .chancedOutput(ChemicalHelper.get(dust, washingByproduct, property.getByProductMultiplier()),
                            7000, 0)
                    .chancedOutput(ChemicalHelper.get(dust, Stone), 4000, 0)
                    .duration(200).EUt(VA[LV])
                    //.category(GTRecipeCategories.ORE_BATHING)
                    .save(provider);
        }

        processMetalSmelting(provider, property, crushed, material);
    }

    private static void processCrushedCentrifuged(@NotNull Consumer<FinishedRecipe> provider,
            @NotNull OreProperty property, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(crushedRefined))
            return;

        ItemStack dustStack = ChemicalHelper.get(dust, material);
        ItemStack byproductStack = ChemicalHelper.get(dust, property.getOreByProduct(2, material), 1);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(crushedRefined, material)
                .outputItems(dustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        TFG_ORE_MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_refined_ore_to_dust")
                .inputItems(crushedRefined, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 0)
                .duration(400).EUt(2)
                .save(provider);

        processMetalSmelting(provider, property, crushedRefined, material);
    }

    private static void processCrushedPurified(@NotNull Consumer<FinishedRecipe> provider,
            @NotNull OreProperty property, @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(crushedPurified))
            return;

        ItemStack crushedCentrifugedStack = ChemicalHelper.get(crushedRefined, material);
        ItemStack dustStack = ChemicalHelper.get(dustPure, material);
        Material byproductMaterial = property.getOreByProduct(1, material);
        ItemStack byproductStack = ChemicalHelper.get(dust, byproductMaterial);

        FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(crushedPurified, material)
                .outputItems(dustStack)
                .duration(10).EUt(16)
                .category(GTRecipeCategories.ORE_FORGING)
                .save(provider);

        TFG_ORE_MACERATOR_RECIPES.recipeBuilder("macerate_" + material.getName() + "_crushed_ore_to_dust")
                .inputItems(crushedPurified, material)
                .outputItems(dustStack)
                .chancedOutput(byproductStack, 1400, 0)
                .duration(400).EUt(2)
                .save(provider);

        if (!crushedCentrifugedStack.isEmpty()) {
            TFG_THERMAL_CENTRIFUGE_RECIPES
                    .recipeBuilder("centrifuge_" + material.getName() + "_purified_ore_to_refined_ore")
                    .inputItems(crushedPurified, material)
                    .outputItems(crushedCentrifugedStack)
                    .chancedOutput(TagPrefix.dust, byproductMaterial, "1/3", 0)
                    .save(provider);
        }

        if (material.hasProperty(PropertyKey.GEM)) {
            ItemStack exquisiteStack = ChemicalHelper.get(gemExquisite, material);
            ItemStack flawlessStack = ChemicalHelper.get(gemFlawless, material);
            ItemStack gemStack = ChemicalHelper.get(gem, material);
            ItemStack flawedStack = ChemicalHelper.get(gemFlawed, material);
            ItemStack chippedStack = ChemicalHelper.get(gemChipped, material);

            if (material.hasFlag(HIGH_SIFTER_OUTPUT)) {
                GTRecipeBuilder builder = TFG_SIFTER_RECIPES
                        .recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
                        .inputItems(crushedPurified, material)
                        .chancedOutput(exquisiteStack, 500, 0)
                        .chancedOutput(flawlessStack, 1500, 0)
                        .chancedOutput(gemStack, 5000, 0)
                        .chancedOutput(dustStack, 2500, 0)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2000, 0);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3000, 0);

                builder.save(provider);
            } else {
                GTRecipeBuilder builder = TFG_SIFTER_RECIPES
                        .recipeBuilder("sift_" + material.getName() + "_purified_ore_to_gems")
                        .inputItems(crushedPurified, material)
                        .chancedOutput(exquisiteStack, 300, 0)
                        .chancedOutput(flawlessStack, 1000, 0)
                        .chancedOutput(gemStack, 3500, 0)
                        .chancedOutput(dustStack, 5000, 0)
                        .duration(400).EUt(16);

                if (!flawedStack.isEmpty())
                    builder.chancedOutput(flawedStack, 2500, 0);
                if (!chippedStack.isEmpty())
                    builder.chancedOutput(chippedStack, 3500, 0);

                builder.save(provider);
            }
        }

        processMetalSmelting(provider, property, crushedPurified, material);
    }

    private static void processDirtyDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
            @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dustImpure))
            return;

        ItemStack dustStack = ChemicalHelper.get(dust, material);
        Material byproduct = property.getOreByProduct(0, material);

        GTRecipeBuilder builder = TFG_ORE_CENTRIFUGE_RECIPES
                .recipeBuilder("centrifuge_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustImpure, material)
                .outputItems(dustStack)
                .duration((int) (material.getMass() * 4)).EUt(24);

        if (byproduct.hasProperty(PropertyKey.DUST)) {
            builder.chancedOutput(TagPrefix.dust, byproduct, "1/9", 0);
        } else {
            builder.outputFluids(byproduct.getFluid(L / 9));
        }

        builder.save(provider);

        TFG_ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_dirty_dust_to_dust")
                .inputItems(dustImpure, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(dustStack)
                .duration(8).EUt(4).save(provider);

        processMetalSmelting(provider, property, dustImpure, material);
    }

    private static void processPureDust(@NotNull Consumer<FinishedRecipe> provider, @NotNull OreProperty property,
            @NotNull Material material) {
        if (!material.shouldGenerateRecipesFor(dustPure))
            return;

        Material byproductMaterial = property.getOreByProduct(1, material);
        ItemStack dustStack = ChemicalHelper.get(dust, material);

        if (property.getSeparatedInto() != null && !property.getSeparatedInto().isEmpty()) {
            List<Material> separatedMaterial = property.getSeparatedInto();
            TagPrefix prefix = (separatedMaterial.get(separatedMaterial.size() - 1).getBlastTemperature() == 0 &&
                    separatedMaterial.get(separatedMaterial.size() - 1).hasProperty(PropertyKey.INGOT)) ? nugget : dust;

            ItemStack separatedStack2 = ChemicalHelper.get(prefix, separatedMaterial.get(separatedMaterial.size() - 1),
                    prefix == nugget ? 2 : 1);

            ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("separate_" + material.getName() + "_pure_dust_to_dust")
                    .inputItems(dustPure, material)
                    .outputItems(dustStack)
                    .chancedOutput(TagPrefix.dust, separatedMaterial.get(0), 1000, 0)
                    .chancedOutput(separatedStack2, prefix == TagPrefix.dust ? 500 : 2000, 0)
                    .duration(200).EUt(24)
                    .save(provider);
        }

        TFG_ORE_CENTRIFUGE_RECIPES.recipeBuilder("centrifuge_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(dustPure, material)
                .outputItems(dustStack)
                .chancedOutput(TagPrefix.dust, byproductMaterial, "1/9", 0)
                .duration(100).EUt(5)
                .save(provider);

        TFG_ORE_WASHER_RECIPES.recipeBuilder("wash_" + material.getName() + "_pure_dust_to_dust")
                .inputItems(dustPure, material)
                .circuitMeta(2)
                .inputFluids(Water.getFluid(100))
                .outputItems(dustStack)
                .duration(8).EUt(4).save(provider);

        processMetalSmelting(provider, property, dustPure, material);
    }

    private static boolean doesMaterialUseNormalFurnace(@NotNull Material material) {
        return !material.hasProperty(PropertyKey.BLAST) && !material.hasFlag(MaterialFlags.NO_ORE_SMELTING);
    }
}
