package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.data.recipes.FinishedRecipe;
import su.terrafirmagreg.core.TFGCore;

import java.util.function.Consumer;

@GTAddon
public class TFGTAddon implements IGTAddon {
	@Override
	public GTRegistrate getRegistrate() {
		return TFGCore.REGISTRATE;
	}

	@Override
	public void initializeAddon() {

	}

	@Override
	public String addonModId() {
		return TFGCore.MOD_ID;
	}

	@Override
	public void addRecipes(Consumer<FinishedRecipe> provider) {
		TFGTRecipes.init(provider);
	}
}
