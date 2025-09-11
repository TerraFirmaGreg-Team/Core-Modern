package su.terrafirmagreg.core.common.data.tfgt;

import java.util.function.Consumer;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.machine.TFGTCraftingComponents;

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
        TFGTCraftingComponents.register();
        TFGTRecipes.init(provider);
    }
}
