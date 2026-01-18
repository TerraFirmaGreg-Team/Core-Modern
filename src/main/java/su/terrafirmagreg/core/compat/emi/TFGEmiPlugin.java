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
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.recipes.ArtisanRecipe;

@EmiEntrypoint
public class TFGEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory ORE_VEIN_INFO = new EmiRecipeCategory(TFGCore.id("ore_vein_info"),
            EmiStack.of(GTItems.PROSPECTOR_HV));

    public static final EmiRecipeCategory BLAZE_BURNER = new EmiRecipeCategory(TFGCore.id("blaze_burner"),
            EmiStack.of(AllBlocks.BLAZE_BURNER.asItem()));

    public static final EmiRecipeCategory BLOCK_INTERACTION = new EmiRecipeCategory(TFGCore.id("block_interaction"),
            EmiStack.of(TFCItems.MORTAR.get()));

    public static final EmiRecipeCategory ARTISAN_TABLE = new EmiRecipeCategory(TFGCore.id("artisan_table"),
            EmiStack.of(TFGBlocks.SMITHING_TABLE.get()));

    @Override
    public void register(EmiRegistry emiRegistry) {

        emiRegistry.addCategory(ORE_VEIN_INFO);
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_HV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LuV));
        Arrays.stream(ExportedOreVeinInfo.RECIPES).forEach(emiRegistry::addRecipe);

        // These two aren't normal ores so add them separately
        emiRegistry.addRecipe(new OreVeinInfoRecipe("nether_anthracite", "minecraft:the_nether",
                35, 0.8, 48, 127, 13, 4, 0, new String[] { "minecraft:deepslate" },
                new OreVeinInfoRecipe.WeightedBlock[] { new OreVeinInfoRecipe.WeightedBlock("cursecoal", 100) },
                null));
        emiRegistry.addRecipe(new OreVeinInfoRecipe("rose_quartz", "minecraft:overworld",
                80, 1, 60, 80, 10, 0, 0, new String[] { "minecraft:blue_ice", "minecraft:snow_block" },
                new OreVeinInfoRecipe.WeightedBlock[] { new OreVeinInfoRecipe.WeightedBlock("rose_quartz", 60), new OreVeinInfoRecipe.WeightedBlock("quartzite", 40) },
                new String[] { "ore_vein.tfg.rose_quartz.emi.0", "ore_vein.tfg.rose_quartz.emi.1", "ore_vein.tfg.rose_quartz.emi.2" }));

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

        emiRegistry.addCategory(ARTISAN_TABLE);
        emiRegistry.addWorkstation(ARTISAN_TABLE, EmiStack.of(TFGBlocks.SMITHING_TABLE.get().asItem()));
        for (ArtisanRecipe recipe : emiRegistry.getRecipeManager().getAllRecipesFor(TFGRecipeTypes.SMITHING.get()).stream().toList()) {
            emiRegistry.addRecipe(new ArtisanTableEmiRecipe(recipe));
        }

    }
}
