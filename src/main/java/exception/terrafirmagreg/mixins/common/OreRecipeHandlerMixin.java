package exception.terrafirmagreg.mixins.common;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.IMaterialProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.builder.SmeltingRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.generated.OreRecipeHandler;
import com.gregtechceu.gtceu.utils.GTUtil;
import exception.terrafirmagreg.compat.gregtech.TFGMaterials;
import exception.terrafirmagreg.compat.gregtech.TFGTagPrefixes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.FORGE_HAMMER_RECIPES;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;

@Mixin(value = OreRecipeHandler.class, remap = false)
public abstract class OreRecipeHandlerMixin {

    @Shadow
    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return false;
    }

    @Inject(method = "init", at = @At(value = "TAIL"), remap = false)
    private static void onOreProcessGen(Consumer<FinishedRecipe> provider, CallbackInfo ci)
    {
        TFGTagPrefixes.poorRawOre.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processRawOre(tagPrefix, material, property, provider, 1));
        TagPrefix.rawOre.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processRawOre(tagPrefix, material, property, provider, 2));
        TFGTagPrefixes.richRawOre.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processRawOre(tagPrefix, material, property, provider, 3));

        TFGTagPrefixes.oreGabbro.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreShale.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreClaystone.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreLimestone.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreConglomerate.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreDolomite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreChert.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreRhyolite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreDacite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreQuartzite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreSlate.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.orePhyllite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreSchist.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreGneiss.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreMarble.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreBasalt.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreDiorite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreAndesite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreGranite.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));
        TFGTagPrefixes.oreChalk.executeHandler(PropertyKey.ORE, (tagPrefix, material, property) -> terraFirmaGreg_1_20_x$processOre(tagPrefix, material, property, provider));

    }

    @Unique
    private static void terraFirmaGreg_1_20_x$processRawOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider, int multiplier) {
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
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * multiplier);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier() * multiplier);

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
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider, "smelt_" + orePrefix.name + "_" + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }
    }

    @Unique
    private static void terraFirmaGreg_1_20_x$processOre(TagPrefix orePrefix, Material material, OreProperty property, Consumer<FinishedRecipe> provider) {
        Material byproductMaterial = GTUtil.selectItemInList(0, material, property.getOreByProducts(), Material.class);
        ItemStack ingotStack;
        ItemStack byproductStack = ChemicalHelper.get(gem, byproductMaterial);
        if (byproductStack.isEmpty()) byproductStack = ChemicalHelper.get(dust, byproductMaterial);
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
        ItemStack crushedStack = ChemicalHelper.get(crushed, material);

        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = ChemicalHelper.get(ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = ChemicalHelper.get(gem, smeltingMaterial);
        } else {
            ingotStack = ChemicalHelper.get(dust, smeltingMaterial);
        }
        int oreMultiplier = TagPrefix.ORES.get(orePrefix).isNether() ? 2 : 1;
        ingotStack.setCount(ingotStack.getCount() * oreMultiplier);

        String prefixString = orePrefix == ore ? "" : orePrefix.name + "_";
        if (!crushedStack.isEmpty()) {
            GTRecipeBuilder builder = FORGE_HAMMER_RECIPES.recipeBuilder("hammer_" + prefixString + material.getName() + "_ore_to_raw_ore")
                    .inputItems(orePrefix, material)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !gem.isIgnored(material)) {
                builder.outputItems(GTUtil.copyAmount(oreMultiplier, ChemicalHelper.get(gem, material, crushedStack.getCount())));
            } else {
                builder.outputItems(GTUtil.copyAmount(oreMultiplier, crushedStack));
            }
            builder.save(provider);

            builder = MACERATOR_RECIPES.recipeBuilder("macerate_" + prefixString + material.getName() + "_ore_to_raw_ore")
                    .inputItems(orePrefix, material)
                    .outputItems(GTUtil.copyAmount(2 * oreMultiplier, crushedStack))
                    .chancedOutput(byproductStack, 1400, 850)
                    .EUt(2)
                    .duration(400);

            builder.outputItems(dust, GTMaterials.Stone);
            builder.save(provider);
        }

        //do not try to add smelting recipes for materials which require blast furnace
        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial) && !orePrefix.isIgnored(material)) {
            float xp = Math.round(((1 + oreMultiplier * 0.5f) * 0.5f - 0.05f) * 10f) / 10f;
            VanillaRecipeHelper.addSmeltingRecipe(provider, "smelt_" + prefixString + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
            VanillaRecipeHelper.addBlastingRecipe(provider, "smelt_" + prefixString + material.getName() + "_ore_to_ingot",
                    ChemicalHelper.getTag(orePrefix, material), ingotStack, xp);
        }
    }
}
