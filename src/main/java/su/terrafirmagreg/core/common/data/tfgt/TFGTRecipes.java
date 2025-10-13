package su.terrafirmagreg.core.common.data.tfgt;

import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS;

import java.util.function.Consumer;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenType;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;

import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.recipes.FinishedRecipe;

import electrolyte.greate.registry.Pumps;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGTCraftingComponents;

public class TFGTRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, true, TFGMachines.AQUEOUS_ACCUMULATOR,
                "RPR", "CHC", "GGG",
                'P', GTCraftingComponents.PUMP,
                'R', GTCraftingComponents.ROTOR,
                'C', GTCraftingComponents.CABLE,
                'H', GTCraftingComponents.HULL,
                'G', GTCraftingComponents.GLASS);

        MetaTileEntityLoader.registerMachineRecipe(provider, true, TFGMachines.FOOD_OVEN,
                "DTD", "AHB", "COC",
                'T', FLBlocks.CURED_OVEN_TOP.get(OvenType.BRICK).get(),
                'H', GTCraftingComponents.HULL,
                'A', GTCraftingComponents.ROBOT_ARM,
                'B', GTCraftingComponents.CABLE,
                'C', GTCraftingComponents.COIL_HEATING_DOUBLE,
                'D', GTCraftingComponents.PLATE,
                'O', TFCItems.WROUGHT_IRON_GRILL.get());

        MetaTileEntityLoader.registerMachineRecipe(provider, true, TFGMachines.FOOD_PROCESSOR,
                "BGC", "MHW", "AVP",
                'H', GTCraftingComponents.HULL,
                'B', GTCraftingComponents.CABLE,
                'A', GTCraftingComponents.CONVEYOR,
                'V', FLBlocks.VAT.get(),
                'M', GTCraftingComponents.GRINDER,
                'P', GTCraftingComponents.PUMP,
                'G', GTCraftingComponents.GLASS,
                'C', GTCraftingComponents.CIRCUIT,
                'W', TFGTCraftingComponents.WHISK);

        MetaTileEntityLoader.registerMachineRecipe(provider, true, TFGMachines.FOOD_REFRIGERATOR,
                "CFC", "SHS", "PRP",
                'C', GTCraftingComponents.CABLE,
                'F', GTCraftingComponents.CIRCUIT,
                'S', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Polyethylene),
                'H', TFGTCraftingComponents.HERMETIC_CASING,
                'P', GTCraftingComponents.PUMP,
                'R', GTCraftingComponents.ROTOR);

        MetaTileEntityLoader.registerMachineRecipe(provider, true, TFGMachines.GAS_PRESSURIZER,
                "GIG", "RHC", "EPE",
                'H', GTCraftingComponents.HULL,
                'I', GTCraftingComponents.PISTON,
                'P', GTCraftingComponents.PUMP,
                'R', GTCraftingComponents.ROTOR,
                'C', GTCraftingComponents.CIRCUIT,
                'G', GTCraftingComponents.GLASS,
                'E', GTCraftingComponents.PIPE_NORMAL);

        VanillaRecipeHelper.addShapedRecipe(provider, true, TFGCore.id("super_chest_ulv"),
                TFGMachines.ULV_SUPER_CHEST.asStack(),
                "ABA", "BCB", "ABA",
                'A', GTCraftingComponents.CIRCUIT.get(0),
                'B', GTCraftingComponents.PLATE.get(0),
                'C', GTMachines.BRONZE_CRATE.asStack());

        VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "super_chest_nbt_ulv",
                TFGMachines.ULV_SUPER_CHEST.asStack(), TFGMachines.ULV_SUPER_CHEST.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, TFGCore.id("hermetic_casing_ulv"),
                TFGMachines.HERMETIC_CASING_ULV.asStack(),
                "AAA", "ABA", "AAA",
                'A', GTCraftingComponents.PLATE.get(0),
                'B', ChemicalHelper.get(TagPrefix.block, GTMaterials.Rubber, 1));

        VanillaRecipeHelper.addShapedRecipe(provider, true, TFGCore.id("super_tank_ulv"),
                TFGMachines.ULV_SUPER_TANK.asStack(),
                "ABA", "BCB", "ADA",
                'A', GTCraftingComponents.CIRCUIT.get(0),
                'B', GTCraftingComponents.PLATE.get(0),
                'C', TFGTCraftingComponents.HERMETIC_CASING.get(0),
                'D', Pumps.STEEL_MECHANICAL_PUMP.get());

        VanillaRecipeHelper.addShapelessNBTClearingRecipe(provider, "super_tank_nbt_ulv",
                TFGMachines.ULV_SUPER_TANK.asStack(), TFGMachines.ULV_SUPER_TANK.asStack());

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("hv_4a_energy_output")
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[GTValues.HV])
                .inputItems(GTCraftingComponents.WIRE_QUAD.get(GTValues.HV), 2)
                .inputItems(GTCraftingComponents.PLATE.get(GTValues.HV), 2)
                .outputItems(TFGMachines.HV_ENERGY_OUTPUT_HATCH_4A, 1)
                .addMaterialInfo(true)
                .duration(5 * 20)
                .EUt(GTValues.VA[GTValues.MV])
                .save(provider);

        for (int i = 0; i < TFGMachines.RAILGUN_ITEM_LOADER_IN.length; i++) {
            if (TFGMachines.RAILGUN_ITEM_LOADER_IN[i] != null && TFGMachines.RAILGUN_ITEM_LOADER_OUT[i] != null) {
                VanillaRecipeHelper.addShapedRecipe(provider, true,
                        TFGCore.id("railgun_input_bus_create_" + TFGMachines.RAILGUN_ITEM_LOADER_IN[i].getTier()),
                        TFGMachines.RAILGUN_ITEM_LOADER_IN[i].asStack(),
                        " d ", "rBx", " w ",
                        'B', ITEM_IMPORT_BUS[i].asStack());

                VanillaRecipeHelper.addShapedRecipe(provider, true,
                        TFGCore.id("railgun_input_convert_" + TFGMachines.RAILGUN_ITEM_LOADER_IN[i].getTier()),
                        TFGMachines.RAILGUN_ITEM_LOADER_IN[i].asStack(),
                        "d", "B",
                        'B', TFGMachines.RAILGUN_ITEM_LOADER_OUT[i].asStack());

                VanillaRecipeHelper.addShapedRecipe(provider, true,
                        TFGCore.id("railgun_output_convert_" + TFGMachines.RAILGUN_ITEM_LOADER_OUT[i].getTier()),
                        TFGMachines.RAILGUN_ITEM_LOADER_OUT[i].asStack(),
                        "d", "B",
                        'B', TFGMachines.RAILGUN_ITEM_LOADER_IN[i].asStack());
            }
        }

    }
}
