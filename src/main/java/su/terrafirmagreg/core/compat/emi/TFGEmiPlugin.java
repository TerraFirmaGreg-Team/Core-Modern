package su.terrafirmagreg.core.compat.emi;

import java.util.Arrays;

import com.gregtechceu.gtceu.common.data.GTItems;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

import su.terrafirmagreg.core.TFGCore;

@EmiEntrypoint
public class TFGEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory ORE_VEIN_INFO = new EmiRecipeCategory(TFGCore.id("ore_vein_info"),
            EmiStack.of(GTItems.PROSPECTOR_HV));

    @Override
    public void register(EmiRegistry emiRegistry) {

        emiRegistry.addCategory(ORE_VEIN_INFO);
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_HV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LuV));
        Arrays.stream(ExportedOreVeinInfo.RECIPES).forEach(emiRegistry::addRecipe);

    }
}
