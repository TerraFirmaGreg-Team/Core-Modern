package su.terrafirmagreg.core.compat.gtceu;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import su.terrafirmagreg.core.TFGCore;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@GTAddon
public final class TFGGTAddon implements IGTAddon {

    @Override
    public void initializeAddon() {}

    @Override
    public GTRegistrate getRegistrate() {
        return null;
    }

    @Override
    public String addonModId() {
        return TFGCore.MOD_ID;
    }

    @Override
    public void registerTagPrefixes() {}

    @Override
    public void registerWorldgenLayers() {}

    @Override
    public void registerOreVeins() {}

    @Override
    public void registerFluidVeins() {}

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {}

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {}
}
