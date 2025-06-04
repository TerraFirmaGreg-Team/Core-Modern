package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.data.recipe.CraftingComponent;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import net.minecraft.data.recipes.FinishedRecipe;

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
	}
}
