package su.terrafirmagreg.core.compat.emi;

import java.util.Arrays;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;

import net.dries007.tfc.common.items.TFCItems;
import net.minecraftforge.registries.ForgeRegistries;

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

    public static final EmiRecipeCategory BLAZE_BURNER = new EmiRecipeCategory(TFGCore.id("blaze_burner"),
            EmiStack.of(AllBlocks.BLAZE_BURNER.asItem()));

    public static final EmiRecipeCategory BLOCK_INTERACTION = new EmiRecipeCategory(TFGCore.id("block_interaction"),
            EmiStack.of(TFCItems.MORTAR.get()));

    @Override
    public void register(EmiRegistry emiRegistry) {

        emiRegistry.addCategory(ORE_VEIN_INFO);
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_HV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LuV));
        Arrays.stream(ExportedOreVeinInfo.RECIPES).forEach(emiRegistry::addRecipe);

        emiRegistry.addCategory(BLAZE_BURNER);
        emiRegistry.addWorkstation(BLAZE_BURNER, EmiStack.of(AllBlocks.BLAZE_BURNER.asItem()));
        for (var liquid_fuel : BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.entrySet()) {
            emiRegistry.addRecipe(new LiquidBlazeBurnerRecipe(liquid_fuel));
        }

        for (var normal_fuel : ForgeRegistries.ITEMS.tags().getTag(AllTags.AllItemTags.BLAZE_BURNER_FUEL_REGULAR.tag).stream().toList()) {
            emiRegistry.addRecipe(new SolidBlazeBurnerRecipe(normal_fuel, false));
        }
        for (var super_fuel : ForgeRegistries.ITEMS.tags().getTag(AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.tag).stream().toList()) {
            emiRegistry.addRecipe(new SolidBlazeBurnerRecipe(super_fuel, true));
        }

        emiRegistry.addCategory(BLOCK_INTERACTION);
        Arrays.stream(BlockInteractionInfo.RECIPES).forEach(emiRegistry::addRecipe);
    }
}
