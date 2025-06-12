package su.terrafirmagreg.core.common.data.machines;

import com.eerussianguy.firmalife.common.FLTags;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.OvenType;
import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import net.minecraft.data.recipes.FinishedRecipe;
import su.terrafirmagreg.core.common.data.TFGTags;

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
	}
}
