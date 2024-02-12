package su.terrafirmagreg.core.mixins.common.gtceu.recipes;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler.*;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefixes.poorRawOre;
import static su.terrafirmagreg.core.compat.gtceu.TFGTagPrefixes.richRawOre;

@Mixin(value = OreRecipeHandler.class, remap = false)
public class OreRecipeHandlerMixin {

    @Shadow
    private static boolean doesMaterialUseNormalFurnace(Material material) {
        throw new AssertionError();
    }

    @Inject(method = "init", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void onInit(Consumer<FinishedRecipe> provider, CallbackInfo ci) {

        for (TagPrefix ore : ORES.keySet()) {
            if (ConfigHolder.INSTANCE.worldgen.allUniqueStoneTypes || ORES.get(ore).shouldDropAsItem()) {
                ore.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processOre(tagPrefix, material, property, provider));
                ore.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processOreForgeHammer(tagPrefix, material, property, provider));
            }
        }

        poorRawOre.executeHandler(PropertyKey.ORE, ((tagPrefix, material, property) -> tfgcore$processPoorRawOre(tagPrefix, material, property, provider)));
        rawOre.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> tfgcore$processRawOre(tagPrefix, material, property, provider));
        richRawOre.executeHandler(PropertyKey.ORE, ((tagPrefix, material, property) -> tfgcore$processRichRawOre(tagPrefix, material, property, provider)));

        crushed.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processCrushedOre(tagPrefix, material, property, provider));
        crushedPurified.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processCrushedPurified(tagPrefix, material, property, provider));
        crushedRefined.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processCrushedCentrifuged(tagPrefix, material, property, provider));
        dustImpure.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processDirtyDust(tagPrefix, material, property, provider));
        dustPure.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> processPureDust(tagPrefix, material, property, provider));

        ci.cancel();
    }

    @Unique
    private static void tfgcore$processPoorRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);
        ItemStack ingotStack;
        ItemStack poorIngotStack;
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        int amountOfCrushedOre = property.getOreMultiplier();
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
            poorIngotStack = ChemicalHelper.get(nugget, smeltingMaterial, 5);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
            poorIngotStack = ChemicalHelper.get(gemChipped, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
            poorIngotStack = ChemicalHelper.get(dustTiny, smeltingMaterial, 5);
        }
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier());
        poorIngotStack.setCount(poorIngotStack.getCount() * property.getOreMultiplier());
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());

        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + orePrefix.name + "_" + material.getName() + "_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.chancedOutput(GTUtil.copyAmount(amountOfCrushedOre, ChemicalHelper.get(gem, material, crushedStack.getCount())), 7500, 950);
            } else {
                builder.chancedOutput(GTUtil.copyAmount(amountOfCrushedOre, crushedStack), 7500, 950);
            }
            builder.save(provider);

            MACERATOR_RECIPES.recipeBuilder("macerate_" + orePrefix.name + "_" + material.getName() + "_ore_to_crushed_ore")
                    .inputItems(orePrefix, material)
                    .chancedOutput(crushedStack, 7500, 950)
                    .chancedOutput(crushedStack, 5000, 750)
                    .chancedOutput(crushedStack, 2500, 500)
                    .chancedOutput(crushedStack, 1250, 250)
                    .EUt(2)
                    .duration(400)
                    .save(provider);
        }

        // do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;

            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), poorIngotStack, xp);
        }
    }

    @Unique
    private static void tfgcore$processRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
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
                    .save(provider);
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }
    }

    @Unique
    private static void tfgcore$processRichRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
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
                    .save(provider);
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + property.getOreMultiplier() * 0.33f) / 3) * 10f) / 10f;

            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }


    }

}
