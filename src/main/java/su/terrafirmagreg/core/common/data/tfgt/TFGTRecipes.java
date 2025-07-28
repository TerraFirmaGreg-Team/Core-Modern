package su.terrafirmagreg.core.common.data.tfgt;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenType;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGItems;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMultiMachines;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_EXPORT_BUS;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS;

public class TFGTRecipes {

	public static void init(Consumer<FinishedRecipe> provider)
	{
		// The steam aqueous accumulator recipe is in KJS

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.AQUEOUS_ACCUMULATOR,
			"RPR", "CHC", "GGG",
			'P', GTCraftingComponents.PUMP,
			'R', GTCraftingComponents.ROTOR,
			'C', GTCraftingComponents.CABLE,
			'H', GTCraftingComponents.HULL,
			'G', GTCraftingComponents.GLASS);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.FOOD_OVEN,
			"DTD", "AHB", "COC",
			'T', FLBlocks.CURED_OVEN_TOP.get(OvenType.BRICK).get(),
			'H', GTCraftingComponents.HULL,
			'A', GTCraftingComponents.ROBOT_ARM,
			'B', GTCraftingComponents.CABLE,
			'C', GTCraftingComponents.COIL_HEATING_DOUBLE,
			'D', GTCraftingComponents.PLATE,
			// This is replaced with #tfg:metal_bars in kubejs
			'O', GTCraftingComponents.PISTON);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.FOOD_PROCESSOR,
			"BGC", "MHW", "AVP",
			'H', GTCraftingComponents.HULL,
			'B', GTCraftingComponents.CABLE,
			'A', GTCraftingComponents.CONVEYOR,
			'V', FLBlocks.VAT.get(),
			'M', GTCraftingComponents.GRINDER,
			'P', GTCraftingComponents.PUMP,
			'G', GTCraftingComponents.GLASS,
			'C', GTCraftingComponents.CIRCUIT,
			// This is replaced with Greate's Whisk in kubejs
			'W', GTCraftingComponents.PISTON);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.FOOD_REFRIGERATOR,
			"CFC", "SHS", "PRP",
			'C', GTCraftingComponents.CABLE,
			'F', GTCraftingComponents.CIRCUIT,
			'S', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Polyethylene),
			// This is replaced with Hermetic Casing in kubejs
			'H', GTCraftingComponents.HULL,
			'P', GTCraftingComponents.PUMP,
			'R', GTCraftingComponents.ROTOR);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.GAS_PRESSURIZER,
			"GIG", "RHC", "EPE",
			'H', GTCraftingComponents.HULL,
			'I', GTCraftingComponents.PISTON,
			'P', GTCraftingComponents.PUMP,
			'R', GTCraftingComponents.ROTOR,
			'C', GTCraftingComponents.CIRCUIT,
			'G', GTCraftingComponents.GLASS,
			'E', GTCraftingComponents.PIPE_NORMAL);


		for (int i = 0; i < TFGMachines.RAILGUN_ITEM_LOADER_IN.length; i++) {
			if (TFGMachines.RAILGUN_ITEM_LOADER_IN[i] != null && TFGMachines.RAILGUN_ITEM_LOADER_OUT[i] != null) {
				VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("railgun_input_bus_create_" + TFGMachines.RAILGUN_ITEM_LOADER_IN[i].getTier()),
						TFGMachines.RAILGUN_ITEM_LOADER_IN[i].asStack(),
						" d ", "rBx", " w ",
						'B', ITEM_IMPORT_BUS[i].asStack());

				VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("railgun_input_convert_" + TFGMachines.RAILGUN_ITEM_LOADER_IN[i].getTier()),
						TFGMachines.RAILGUN_ITEM_LOADER_IN[i].asStack(),
						"d", "B",
						'B', TFGMachines.RAILGUN_ITEM_LOADER_OUT[i].asStack());
				VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("railgun_output_convert_" + TFGMachines.RAILGUN_ITEM_LOADER_OUT[i].getTier()),
						TFGMachines.RAILGUN_ITEM_LOADER_OUT[i].asStack(),
						"d", "B",
						'B', TFGMachines.RAILGUN_ITEM_LOADER_IN[i].asStack());
			}
		}

		GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(TFGCore.id("railgun_ammo_basic"))
				.outputItems(TFGItems.RAILGUN_AMMO_SHELL.get(), 1)
				.EUt(GTValues.VA[GTValues.MV])
				.duration(20 * 10)
				.inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Steel, 2))
				.inputItems(ChemicalHelper.get(TagPrefix.rod, GTMaterials.SteelMagnetic, 2))
				.inputItems(ChemicalHelper.get(TagPrefix.wireFine, GTMaterials.AnnealedCopper, 2))
				.save(provider);

		GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(TFGCore.id("railgun_ammo_advanced"))
				.outputItems(TFGItems.RAILGUN_AMMO_SHELL.get(), 4)
				.EUt(GTValues.VA[GTValues.HV])
				.duration(20 * 10)
				.inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.BlackSteel, 2))
				.inputItems(ChemicalHelper.get(TagPrefix.rod, GTMaterials.SteelMagnetic, 4))
				.inputItems(ChemicalHelper.get(TagPrefix.wireFine, GTMaterials.Gold, 2))
				.save(provider);

		GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(TFGCore.id("railgun_ammo_best"))
				.outputItems(TFGItems.RAILGUN_AMMO_SHELL.get(), 8)
				.EUt(GTValues.VA[GTValues.EV])
				.duration(20 * 10)
				.inputItems(ChemicalHelper.get(TagPrefix.plateDouble, GTMaterials.Tungsten, 2))
				.inputItems(ChemicalHelper.get(TagPrefix.rod, GTMaterials.NeodymiumMagnetic, 2))
				.inputItems(ChemicalHelper.get(TagPrefix.wireFine, GTMaterials.Aluminium, 2))
				.save(provider);

		VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("railgun_ammo_holder"), TFGMachines.RAILGUN_AMMO_LOADER.asStack(),
				"RMR", "MBM", "CCC",
				'B', ITEM_IMPORT_BUS[GTValues.MV].asStack(),
				'R', GTItems.ROBOT_ARM_MV,
				'M', GTItems.ELECTRIC_MOTOR_MV,
				'C', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.AnnealedCopper));

		VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("electric_greenhouse"),
			TFGMultiMachines.ELECTRIC_GREENHOUSE.asStack(),
				"ABA", "CDC", "BCB",
				'A', CustomTags.MV_CIRCUITS,
				'B', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Copper),
				'C', TFCItems.COMPOST.get(),
				'D', GTBlocks.STEEL_HULL.get()
		);

		VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("interplanetary_monitor"), TFGMachines.INTERPLANETARY_LOGISTICS_MONITOR.asStack(),
				"CDC", "SHE", "WCW",
				'C', CustomTags.HV_CIRCUITS,
				'D', GTItems.COVER_SCREEN,
				'S', GTItems.SENSOR_HV,
				'H', GTMachines.HULL[GTValues.HV].asStack(),
				'E', GTItems.EMITTER_HV,
				'W', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Silver));

		VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("interplanetary_launcher"), TFGMultiMachines.INTERPLANETARY_ITEM_LAUNCHER.asStack(),
				"NSN", "CHC", "NEN",
				'C', CustomTags.IV_CIRCUITS,
				'S', GTItems.SENSOR_HV,
				'E', GTItems.EMITTER_HV,
				'H', GTMachines.HULL[GTValues.EV].asStack(),
				'N', ChemicalHelper.get(TagPrefix.plate, GTMaterials.HSLASteel));

		VanillaRecipeHelper.addShapedRecipe(provider, TFGCore.id("interplanetary_receiver"), TFGMultiMachines.INTERPLANETARY_ITEM_RECEIVER.asStack(),
				"CSC", "WHW", "CSC",
				'C', CustomTags.MV_CIRCUITS,
				'S', GTItems.SENSOR_MV,
				'W', ChemicalHelper.get(TagPrefix.cableGtDouble, GTMaterials.Copper),
				'H', GTMachines.HULL[GTValues.MV].asStack());

	}
}