package exception.terrafirmagreg.objects;

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
import exception.terrafirmagreg.compat.gregtech.TFGTagPrefixes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTItems.SHAPE_EMPTY;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static exception.terrafirmagreg.objects.TFGItems.*;

public class TFGRecipes {

    public static void init(Consumer<FinishedRecipe> consumer) {
        for (var material : GTRegistries.MATERIALS.values()) {
            if (material.hasProperty(PropertyKey.TOOL) && !material.hasProperty(PropertyKey.POLYMER)) {
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
                "shape_extruder_shovel_head",
                SHAPE_EXTRUDER_SHOVEL_HEAD.asStack(),
                "S  ", "fh ", "   ", 'S', SHAPE_EMPTY.asStack());

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
    }

    private static void processHead(TagPrefix tagPrefix, Material material, ItemEntry<Item> extruderShape, MarkerMaterial lenseColor, Consumer<FinishedRecipe> consumer) {
        var output = ChemicalHelper.get(tagPrefix, material);
        if (output.isEmpty()) return;

        if (material.hasProperty(PropertyKey.INGOT)) {
            EXTRUDER_RECIPES.recipeBuilder(tagPrefix.name + "_mold_head_to_head_" + material.getName())
                    .duration(12).EUt(32)
                    .notConsumable(extruderShape)
                    .inputItems(TagPrefix.ingot, material, (int) (tagPrefix.materialAmount() / GTValues.M))
                    .outputItems(output)
                    .save(consumer);
        } else if (material.hasProperty(PropertyKey.GEM)) {
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
