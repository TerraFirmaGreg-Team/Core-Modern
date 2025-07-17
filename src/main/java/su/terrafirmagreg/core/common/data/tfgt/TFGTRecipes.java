package su.terrafirmagreg.core.common.data.tfgt;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenType;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMultiMachines;

import java.util.function.Consumer;

public class TFGTRecipes {

	public static void init(Consumer<FinishedRecipe> provider)
	{
		// The steam aqueous accumulator recipe is in KJS

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.AQUEOUS_ACCUMULATOR,
			"RPR", "CHC", "GGG",
			'P', CraftingComponent.PUMP,
			'R', CraftingComponent.ROTOR,
			'C', CraftingComponent.CABLE,
			'H', CraftingComponent.HULL,
			'G', CraftingComponent.GLASS);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.FOOD_OVEN,
			"DTD", "AHB", "COC",
			'T', FLBlocks.CURED_OVEN_TOP.get(OvenType.BRICK).get(),
			'H', CraftingComponent.HULL,
			'A', CraftingComponent.ROBOT_ARM,
			'B', CraftingComponent.CABLE,
			'C', CraftingComponent.COIL_HEATING_DOUBLE,
			'D', CraftingComponent.PLATE,
			// This is replaced with #tfg:metal_bars in kubejs
			'O', CraftingComponent.PISTON);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.FOOD_PROCESSOR,
			"BGB", "MHW", "AVP",
			'H', CraftingComponent.HULL,
			'B', CraftingComponent.CABLE,
			'A', CraftingComponent.CONVEYOR,
			'V', FLBlocks.VAT.get(),
			'M', CraftingComponent.GRINDER,
			'P', CraftingComponent.PUMP,
			'G', CraftingComponent.GLASS,
			// This is replaced with Greate's Whisk in kubejs
			'W', CraftingComponent.PISTON);

		MetaTileEntityLoader.registerMachineRecipe(provider, TFGMachines.FOOD_REFRIGERATOR,
			"CFC", "SHS", "PRP",
			'C', CraftingComponent.CABLE,
			'F', CraftingComponent.CIRCUIT,
			'S', ChemicalHelper.get(TagPrefix.plate, GTMaterials.Polyethylene),
			// This is replaced with Hermetic Casing in kubejs
			'H', CraftingComponent.HULL,
			'P', CraftingComponent.PUMP,
			'R', CraftingComponent.ROTOR);

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