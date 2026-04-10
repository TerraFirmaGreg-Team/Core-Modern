package su.terrafirmagreg.core.compat.emi;

import java.util.Arrays;
import java.util.Set;

import com.forsteri.createliquidfuel.core.BurnerStomachHandler;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;

import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextureWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.tfgt.TFGMultiMachines;
import su.terrafirmagreg.core.common.recipe.ArtisanRecipe;
import su.terrafirmagreg.core.common.recipe.repair.ItemRepairRecipe;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam.TFGLargeBoilerMachine;
import su.terrafirmagreg.core.world.new_ow_wg.WorldgenVersionData;

@EmiEntrypoint
public class TFGEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory ORE_VEIN_INFO = new EmiRecipeCategory(TFGCore.id("ore_vein_info"),
            EmiStack.of(GTItems.PROSPECTOR_HV));

    public static final EmiRecipeCategory BLAZE_BURNER = new EmiRecipeCategory(TFGCore.id("blaze_burner"),
            EmiStack.of(AllBlocks.BLAZE_BURNER.asItem()));

    public static final EmiRecipeCategory BLOCK_INTERACTION = new EmiRecipeCategory(TFGCore.id("block_interaction"),
            EmiStack.of(TFCItems.MORTAR.get()));

    public static final EmiRecipeCategory ARTISAN_TABLE = new EmiRecipeCategory(TFGCore.id("artisan_table"),
            EmiStack.of(TFGBlocks.ARTISAN_TABLE.get()));

    public static final EmiRecipeCategory LARGE_BOILER_BOOSTER = new EmiRecipeCategory(TFGCore.id("large_boiler_booster"),
            EmiStack.of(GTBlocks.FIREBOX_STEEL.asItem()));

    public static final EmiRecipeCategory ITEM_REPAIR = new EmiRecipeCategory(TFGCore.id("item_repair"),
            EmiStack.of(net.minecraft.world.item.Items.CRAFTING_TABLE));

    public static final EmiRecipeCategory FLUID_VEIN_INFO = new EmiRecipeCategory(TFGCore.id("fluid_vein_info"),
            EmiStack.of(GTItems.PROSPECTOR_HV));

    @Override
    public void register(EmiRegistry emiRegistry) {

        //Ore Veins
        emiRegistry.addCategory(ORE_VEIN_INFO);
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_HV));
        emiRegistry.addWorkstation(ORE_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LuV));
        Arrays.stream(ExportedOreVeinInfo.RECIPES)
                .filter(r -> !r.getId().getPath().equals("/nether_anthracite_emi"))
                .forEach(emiRegistry::addRecipe);

        // These two aren't normal ores so add them separately
        emiRegistry.addRecipe(new OreVeinInfoRecipe("nether_anthracite", "minecraft:the_nether",
                35, 0.8, 48, 127, 13, 4, 0, new String[] { "minecraft:deepslate" },
                new OreVeinInfoRecipe.WeightedBlock[] { new OreVeinInfoRecipe.WeightedBlock("cursecoal", 100) },
                null));
        emiRegistry.addRecipe(new OreVeinInfoRecipe("rose_quartz", "minecraft:overworld",
                80, 1, 60, 80, 10, 0, 0, new String[] { "minecraft:blue_ice", "minecraft:snow_block" },
                new OreVeinInfoRecipe.WeightedBlock[] { new OreVeinInfoRecipe.WeightedBlock("rose_quartz", 60), new OreVeinInfoRecipe.WeightedBlock("quartzite", 40) },
                new String[] { "ore_vein.tfg.rose_quartz.emi.0", "ore_vein.tfg.rose_quartz.emi.1", "ore_vein.tfg.rose_quartz.emi.2" }));

        //Blaze Burner
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

        //Block Interactions
        emiRegistry.addCategory(BLOCK_INTERACTION);
        Arrays.stream(BlockInteractionInfo.RECIPES).forEach(emiRegistry::addRecipe);

        //Large Boiler Extras
        emiRegistry.addCategory(LARGE_BOILER_BOOSTER);
        emiRegistry.addWorkstation(LARGE_BOILER_BOOSTER,
                EmiStack.of(TFGMultiMachines.LARGE_BOILER_BRONZE.getBlock().asItem()));
        emiRegistry.addWorkstation(LARGE_BOILER_BOOSTER,
                EmiStack.of(TFGMultiMachines.LARGE_STEEL_BOILER.getBlock().asItem()));
        TFGLargeBoilerMachine.getBoosters().forEach(booster -> emiRegistry.addRecipe(new LargeBoilerBoosterRecipe(booster)));

        //Artisan Table
        emiRegistry.addCategory(ARTISAN_TABLE);
        emiRegistry.addWorkstation(ARTISAN_TABLE, EmiStack.of(TFGBlocks.ARTISAN_TABLE.get().asItem()));
        for (ArtisanRecipe recipe : emiRegistry.getRecipeManager().getAllRecipesFor(TFGRecipeTypes.ARTISAN.get()).stream().toList()) {
            emiRegistry.addRecipe(new ArtisanTableEmiRecipe(recipe));
        }

        // Item Repair
        emiRegistry.addCategory(ITEM_REPAIR);
        emiRegistry.addWorkstation(ITEM_REPAIR, EmiStack.of(net.minecraft.world.item.Items.CRAFTING_TABLE));
        emiRegistry.addRecipeHandler(MenuType.CRAFTING, new ItemRepairCraftingRecipeHandler());
        for (ItemRepairRecipe recipe : emiRegistry.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(r -> r instanceof ItemRepairRecipe)
                .map(r -> (ItemRepairRecipe) r)
                .toList()) {
            emiRegistry.addRecipe(new ItemRepairEmiRecipe(recipe));
        }

        //Fluid Veins
        emiRegistry.addCategory(FLUID_VEIN_INFO);
        emiRegistry.addWorkstation(FLUID_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_HV));
        emiRegistry.addWorkstation(FLUID_VEIN_INFO, EmiStack.of(GTItems.PROSPECTOR_LuV));
        GTRegistries.BEDROCK_FLUID_DEFINITIONS.entries().stream().filter(entry -> {
            boolean legacyVein = LEGACY_FLUID_VEINS.contains(entry.getKey());
            //Effectively this
            // return onLegacyWorldgen() ? legacyVein : !legacyVein;
            return onLegacyWorldgen() == legacyVein;
        }).forEach(fluidDef -> emiRegistry.addRecipe(new FluidVeinRecipe(fluidDef)));
    }

    private static final Set<ResourceLocation> LEGACY_FLUID_VEINS = Set.of(
            TFGCore.id("old_gen_heavy_oil_deposit"),
            TFGCore.id("old_gen_light_oil_deposit"),
            TFGCore.id("old_gen_natural_gas_deposit"),
            TFGCore.id("old_gen_oil_deposit"),
            TFGCore.id("old_gen_raw_oil_deposit"));

    private static final ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID,
            "textures/gui/emi/arrow.png");

    public static int createArrowWidget(WidgetHolder holder, int offsetY, int offsetX, int length) {
        int image_height = 18;
        int image_width = 40;
        int u_start = image_width - length;

        TextureWidget widget = new TextureWidget(ARROW, offsetX, offsetY, length, image_height, u_start, 0, length, image_height - 1, image_width, image_height);
        holder.add(widget);
        return offsetX + 2 + length;
    }

    public static void createItemWidget(WidgetHolder holder, int offsetY, int offsetX, EmiIngredient stack) {
        SlotWidget widget = new SlotWidget(stack, offsetX, offsetY);
        holder.add(widget);
    }

    private boolean onLegacyWorldgen() {
        return WorldgenVersionData.OVERWORLD_VERSION != WorldgenVersionData.OVERWORLD_TFC_1_21_BACKPORT;
    }
}
