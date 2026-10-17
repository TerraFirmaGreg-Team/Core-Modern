package su.terrafirmagreg.core.common.tfgt;

import java.util.function.Consumer;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.recipe.ToolHeadReplaceRecipe;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.tfgt.TFGCovers;
import su.terrafirmagreg.core.common.data.tfgt.TFGCraftingComponents;
import su.terrafirmagreg.core.common.data.tfgt.TFGTagPrefix;
import su.terrafirmagreg.core.common.data.tfgt.TFGToolTypes;

@GTAddon
@SuppressWarnings("unused")
public class TFGTAddon implements IGTAddon {
    @Override
    public GTRegistrate getRegistrate() {
        return TFGCore.REGISTRATE;
    }

    @Override
    public void registerCovers() {
        TFGCovers.init();
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
        TFGCraftingComponents.register();
        for (GTToolType scythe : TFGToolTypes.ELECTRIC_SCYTHES) {
            ToolHeadReplaceRecipe.setToolHeadForTool(TFGTagPrefix.toolHeadScythe, scythe);
        }
        TFGTRecipes.init(provider);
    }
}
