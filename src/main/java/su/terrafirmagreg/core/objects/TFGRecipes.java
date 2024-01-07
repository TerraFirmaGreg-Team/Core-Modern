package su.terrafirmagreg.core.objects;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import su.terrafirmagreg.core.compat.gtceu.TFGTagPrefixes;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dustTiny;
import static com.gregtechceu.gtceu.common.data.GTItems.SHAPE_EMPTY;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static com.gregtechceu.gtceu.data.recipe.generated.RecyclingRecipeHandler.processCrushing;
import static su.terrafirmagreg.core.compat.gtceu.TFGMaterials.*;
import static su.terrafirmagreg.core.objects.TFGItems.*;

public class TFGRecipes {

    public static void init(Consumer<FinishedRecipe> consumer)
    {
        stoneTypeDustsDecomposition(consumer);
        extruderShapeHeads(consumer);
        toolHeadRecycling(consumer);

        FLUID_SOLIDFICATION_RECIPES.recipeBuilder("latex_heating").duration(480).EUt(VA[LV])
                .inputItems(dust, Sulfur)
                .inputFluids(Latex.getFluid(1000))
                .outputItems(GTItems.STICKY_RESIN)
                .save(consumer);
    }

    private static void stoneTypeDustsDecomposition(Consumer<FinishedRecipe> consumer)
    {
        // Gabbro
        CENTRIFUGE_RECIPES.recipeBuilder("gabbro_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Gabbro)
                .chancedOutput(dustTiny, Titanium, 6700, 700)
                .chancedOutput(dustTiny, Iron, 3700, 700)
                .chancedOutput(dustTiny, MetalMixture, 1700, 700)
                .save(consumer);

        // Shale
        CENTRIFUGE_RECIPES.recipeBuilder("shale_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Shale)
                .chancedOutput(dustTiny, Sodium, 7500, 500)
                .chancedOutput(dustTiny, MetalMixture, 1500, 500)
                .outputFluids(Oxygen.getFluid(16))
                .save(consumer);

        // Claystone
        CENTRIFUGE_RECIPES.recipeBuilder("claystone_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Claystone)
                .chancedOutput(dustTiny, Aluminium, 6700, 700)
                .chancedOutput(dustTiny, Silicon, 6700, 700)
                .chancedOutput(dustTiny, Hematite, 6700, 700)
                .outputFluids(Oxygen.getFluid(5))
                .save(consumer);

        // Limestone
        CENTRIFUGE_RECIPES.recipeBuilder("limestone_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Limestone)
                .chancedOutput(dustTiny, Calcium, 8700, 700)
                .chancedOutput(dustTiny, MetalMixture, 1700, 700)
                .outputFluids(Oxygen.getFluid(36))
                .save(consumer);

        // Conglomerate
        CENTRIFUGE_RECIPES.recipeBuilder("conglomerate_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Conglomerate)
                .chancedOutput(dustTiny, Hematite, 6700, 700)
                .chancedOutput(dustTiny, Silicon, 4700, 700)
                .chancedOutput(dustTiny, TricalciumPhosphate, 3700, 700)
                .outputFluids(Oxygen.getFluid(5))
                .save(consumer);

        // Dolomite
        CENTRIFUGE_RECIPES.recipeBuilder("dolomite_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Dolomite)
                .chancedOutput(dustTiny, Magnesium, 6700, 700)
                .chancedOutput(dustTiny, Calcium, 5700, 700)
                .chancedOutput(dustTiny, MetalMixture, 3700, 700)
                .outputFluids(Oxygen.getFluid(16))
                .save(consumer);

        // Chert
        CENTRIFUGE_RECIPES.recipeBuilder("chert_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Chert)
                .chancedOutput(dustTiny, Silicon, 6700, 700)
                .chancedOutput(dustTiny, MetalMixture, 5700, 700)
                .outputFluids(Oxygen.getFluid(24))
                .save(consumer);

        // Chalk
        CENTRIFUGE_RECIPES.recipeBuilder("chalk_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Chalk)
                .chancedOutput(dustTiny, Calcium, 6700, 700)
                .chancedOutput(dustTiny, Carbon, 3700, 700)
                .chancedOutput(dustTiny, MetalMixture, 1700, 700)
                .outputFluids(Oxygen.getFluid(12))
                .save(consumer);

        // Rhyolite
        CENTRIFUGE_RECIPES.recipeBuilder("rhyolite_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Rhyolite)
                .chancedOutput(dustTiny, SiliconDioxide, 8700, 700)
                .chancedOutput(dustTiny, MetalMixture, 800, 700)
                .save(consumer);

        // Dacite
        CENTRIFUGE_RECIPES.recipeBuilder("dacite_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Dacite)
                .chancedOutput(dustTiny, Sodium, 6700, 700)
                .chancedOutput(dustTiny, Calcium, 5700, 700)
                .chancedOutput(dustTiny, SiliconDioxide, 4700, 700)
                .chancedOutput(dustTiny, Aluminium, 3700, 700)
                .chancedOutput(dustTiny, MetalMixture, 150, 700)
                .outputFluids(Oxygen.getFluid(12))
                .save(consumer);

        // Slate
        CENTRIFUGE_RECIPES.recipeBuilder("slate_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Slate)
                .chancedOutput(dustTiny, MetalMixture, 780, 480)
                .outputFluids(Oxygen.getFluid(6))
                .save(consumer);

        // Phyllite
        CENTRIFUGE_RECIPES.recipeBuilder("phyllite_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Phyllite)
                .chancedOutput(dustTiny, Quartzite, 5700, 700)
                .chancedOutput(dustTiny, CalciumChloride, 1700, 700)
                .outputFluids(Oxygen.getFluid(2))
                .save(consumer);

        // Schist
        CENTRIFUGE_RECIPES.recipeBuilder("schist_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Schist)
                .chancedOutput(dustTiny, Mica, 6700, 700)
                .chancedOutput(dustTiny, Talc, 5700, 700)
                .chancedOutput(dustTiny, Graphite, 4700, 700)
                .chancedOutput(dustTiny, MetalMixture, 780, 700)
                .outputFluids(Oxygen.getFluid(12))
                .save(consumer);

        // Gneiss
        CENTRIFUGE_RECIPES.recipeBuilder("gneiss_dust_separation").duration(480).EUt(VA[MV])
                .inputItems(dust, Gneiss)
                .chancedOutput(dustTiny, Quartzite, 6700, 700)
                .chancedOutput(dustTiny, Biotite, 3700, 700)
                .outputFluids(Oxygen.getFluid(2))
                .save(consumer);
    }

    private static void toolHeadRecycling(Consumer<FinishedRecipe> consumer)
    {
        TFGTagPrefixes.toolHeadMiningHammer.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadSword.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadPickaxe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadShovel.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadAxe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadHoe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadScythe.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadFile.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadHammer.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadSaw.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadKnife.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
        TFGTagPrefixes.toolHeadButcheryKnife.executeHandler(PropertyKey.DUST, (tagPrefix, material, property) -> processCrushing(tagPrefix, material, property, consumer));
    }

    private static void extruderShapeHeads(Consumer<FinishedRecipe> consumer)
    {
        for (var material : GTRegistries.MATERIALS.values())
        {
            if (material.hasProperty(PropertyKey.TOOL) && !material.hasProperty(PropertyKey.POLYMER))
            {
                processHead(TFGTagPrefixes.toolHeadMiningHammer, material, SHAPE_EXTRUDER_MINING_HAMMER_HEAD, MarkerMaterials.Color.Blue, consumer);
                processHead(TFGTagPrefixes.toolHeadSword, material, SHAPE_EXTRUDER_SWORD_HEAD, MarkerMaterials.Color.Black, consumer);
                processHead(TFGTagPrefixes.toolHeadPickaxe, material, SHAPE_EXTRUDER_PICKAXE_HEAD, MarkerMaterials.Color.Cyan, consumer);
                processHead(TFGTagPrefixes.toolHeadShovel, material, SHAPE_EXTRUDER_SHOVEL_HEAD, MarkerMaterials.Color.Brown, consumer);
                processHead(TFGTagPrefixes.toolHeadAxe, material, SHAPE_EXTRUDER_AXE_HEAD, MarkerMaterials.Color.Gray, consumer);
                processHead(TFGTagPrefixes.toolHeadHoe, material, SHAPE_EXTRUDER_HOE_HEAD, MarkerMaterials.Color.Green, consumer);
                processHead(TFGTagPrefixes.toolHeadScythe, material, SHAPE_EXTRUDER_SCYTHE_HEAD, MarkerMaterials.Color.LightBlue, consumer);
                processHead(TFGTagPrefixes.toolHeadFile, material, SHAPE_EXTRUDER_FILE_HEAD, MarkerMaterials.Color.LightGray, consumer);
                processHead(TFGTagPrefixes.toolHeadHammer, material, SHAPE_EXTRUDER_HAMMER_HEAD, MarkerMaterials.Color.Lime, consumer);
                processHead(TFGTagPrefixes.toolHeadSaw, material, SHAPE_EXTRUDER_SAW_HEAD, MarkerMaterials.Color.Magenta, consumer);
                processHead(TFGTagPrefixes.toolHeadKnife, material, SHAPE_EXTRUDER_KNIFE_HEAD, MarkerMaterials.Color.Purple, consumer);
                processHead(TFGTagPrefixes.toolHeadButcheryKnife, material, SHAPE_EXTRUDER_BUTCHERY_KNIFE_HEAD, MarkerMaterials.Color.Red, consumer);
            }
        }

        // Doubling the molds
        for (var shapeExtruder : TFGItems.EXTRUDER_SHAPES) {
            if (shapeExtruder == null) continue;
            FORMING_PRESS_RECIPES.recipeBuilder("copy_shape_" + shapeExtruder.get())
                    .duration(120).EUt(22)
                    .notConsumable(shapeExtruder)
                    .inputItems(SHAPE_EMPTY)
                    .outputItems(shapeExtruder)
                    .save(consumer);
        }

        // Workbench recipes
        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_mining_hammer_head",
                SHAPE_EXTRUDER_MINING_HAMMER_HEAD.asStack(),
                "Sfh", "   ", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_sword_head",
                SHAPE_EXTRUDER_SWORD_HEAD.asStack(),
                "Shf", "   ", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_pickaxe_head",
                SHAPE_EXTRUDER_PICKAXE_HEAD.asStack(),
                "S  ", "hf ", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_axe_head",
                SHAPE_EXTRUDER_AXE_HEAD.asStack(),
                "S  ", " fh", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_hoe_head",
                SHAPE_EXTRUDER_HOE_HEAD.asStack(),
                "S  ", " hf", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_scythe_head",
                SHAPE_EXTRUDER_SCYTHE_HEAD.asStack(),
                "S  ", "   ", "fh ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_file_head",
                SHAPE_EXTRUDER_FILE_HEAD.asStack(),
                "S  ", "   ", "hf ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_hammer_head",
                SHAPE_EXTRUDER_HAMMER_HEAD.asStack(),
                "Sf ", " h ", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_saw_head",
                SHAPE_EXTRUDER_SAW_HEAD.asStack(),
                "Sh ", " f ", "   ", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_knife_head",
                SHAPE_EXTRUDER_KNIFE_HEAD.asStack(),
                "S f", "   ", "  h", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_butchery_head_head",
                SHAPE_EXTRUDER_BUTCHERY_KNIFE_HEAD.asStack(),
                "S h", "   ", "  f", 'S', SHAPE_EMPTY.asStack());

        VanillaRecipeHelper.addStrictShapedRecipe(consumer,
                "shape_extruder_shovel_head",
                SHAPE_EXTRUDER_SHOVEL_HEAD.asStack(),
                "S  ", "f  ", "h  ", 'S', SHAPE_EMPTY.asStack());
    }

    private static void processHead(TagPrefix tagPrefix, Material material, ItemEntry<Item> extruderShape, MarkerMaterial lenseColor, Consumer<FinishedRecipe> consumer)
    {
        var output = ChemicalHelper.get(tagPrefix, material);
        if (output.isEmpty()) return;

        if (material.hasProperty(PropertyKey.INGOT))
        {
            EXTRUDER_RECIPES.recipeBuilder(tagPrefix.name + "_mold_head_to_head_" + material.getName())
                    .duration(12).EUt(32)
                    .notConsumable(extruderShape)
                    .inputItems(TagPrefix.ingot, material, (int) (tagPrefix.materialAmount() / GTValues.M))
                    .outputItems(output)
                    .save(consumer);
        }
        else if (material.hasProperty(PropertyKey.GEM))
        {
            var lense = GTItems.GLASS_LENSES.get(lenseColor);
            if (lense == null) return;

            LASER_ENGRAVER_RECIPES.recipeBuilder(lenseColor.getName() + "_mold_head_to_head_" + material.getName())
                    .duration(12).EUt(32)
                    .notConsumable(lense)
                    .inputItems(TagPrefix.gem, material, (int) (tagPrefix.materialAmount() / GTValues.M))
                    .outputItems(output)
                    .save(consumer);
        }
    }

}
