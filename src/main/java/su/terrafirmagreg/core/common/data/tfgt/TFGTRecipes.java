package su.terrafirmagreg.core.common.data.tfgt;

import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenType;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGMachines;

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
			'H', CraftingComponent.HULL,
			'P', CraftingComponent.PUMP,
			'R', CraftingComponent.ROTOR
			);

		VanillaRecipeHelper.addShapedRecipe(provider, new ResourceLocation("tfg", "electric_greenhouse"), TFGMachines.ELECTRIC_GREENHOUSE.asStack(),
				"ABA", "CDC", "BCB",
				'A', CustomTags.MV_CIRCUITS,
				'B', ChemicalHelper.get(TagPrefix.cableGtSingle, GTMaterials.Copper),
				'C', TFCItems.COMPOST.get(),
				'D', GTBlocks.CASING_STEEL_SOLID.get()
		);
	}
}
