package su.terrafirmagreg.core.common.data.tfgt;

import static su.terrafirmagreg.core.common.tfgt.TFGUITextures.*;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.recipe.gui.GTRecipeUIModifiers;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.progress.ProgressDrawable;
import fi.dea.mc.deafission.common.data.recipe.HeatRecipeCapability;

import su.terrafirmagreg.core.common.data.TFGSounds;

@SuppressWarnings("deprecation")
public class TFGTRecipeTypes {

    public static void init() {
    }

    public static final GTRecipeType GREENHOUSE_RECIPES = GTRecipeTypes.register("greenhouse", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.UP, PROGRESS_BAR_EGH)))
            .setSound(GTSoundEntries.MINER);

    public static final GTRecipeType BIOREACTOR_RECIPES = GTRecipeTypes.register("bioreactor", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 6, 6)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.UP, PROGRESS_BAR_DNA))
                    .addRecipeUIModifier((recipe, widget) -> {
                        var text = recipe.data.getString("action");
                        if (!text.isEmpty()) {
                            widget.textComponents.child(Text.str(text).asWidget());
                        }
                    }))
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType GROWTH_CHAMBER_RECIPES = GTRecipeTypes
            .register("growth_chamber", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(18, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.RIGHT, PROGRESS_BAR_PETRI))
                    .addRecipeUIModifier((recipe, widget) -> {
                        var text = recipe.data.getString("action");
                        if (!text.isEmpty()) {
                            widget.textComponents.child(Text.str(text).asWidget());
                        }
                    }))
            .setSound(GTSoundEntries.CHEMICAL);

    public static final GTRecipeType FOOD_OVEN_RECIPES = GTRecipeTypes.register("food_oven", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(2, 2, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
            .setSound(GTSoundEntries.FURNACE);

    public static final GTRecipeType FOOD_PROCESSOR_RECIPES = GTRecipeTypes
            .register("food_processor", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(9, 2, 3, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier((recipe, widget) -> {
                        var text = recipe.data.getString("action");
                        if (!text.isEmpty()) {
                            widget.textComponents.child(Text.str(text).asWidget());
                        }
                    }))
            .setSound(GTSoundEntries.MIXER);

    public static final GTRecipeType AQUEOUS_ACCUMULATOR_RECIPES = GTRecipeTypes
            .register("aqueous_accumulator", GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY))
            .setMaxTooltips(4)
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType GAS_PRESSURIZER_RECIPES = GTRecipeTypes
            .register("gas_pressurizer", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(3, 1, 3, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_COMPRESS)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY))
            .setSound(GTSoundEntries.COMPRESSOR);

    public static final GTRecipeType NUCLEAR_TURBINE = GTRecipeTypes
            .register("nuclear_turbine", GTRecipeTypes.GENERATOR)
            .setMaxIOSize(0, 0, 1, 1)
            .setSound(GTSoundEntries.TURBINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));

    public final static GTRecipeType EVAPORATION_TOWER = GTRecipeTypes
            .register("evaporation_tower", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 12)
            .setEUIO(IO.IN)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public final static GTRecipeType COOLING_TOWER = GTRecipeTypes
            .register("cooling_tower", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 2)
            .setSound(GTSoundEntries.TURBINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));

    public final static GTRecipeType HEAT_EXCHANGER = GTRecipeTypes
            .register("heat_exchanger", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 3, 3)
            .setSound(GTSoundEntries.TURBINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));

    public final static GTRecipeType OSTRUM_LINEAR_ACCELERATOR = GTRecipeTypes
            .register("ostrum_linear_accelerator", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 9, 6, 6)
            .setMaxSize(IO.IN, HeatRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, HeatRecipeCapability.CAP, 1)
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_CRACKING)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.ATOMIC_OVERLAY_1)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.ATOMIC_OVERLAY_1)
                    .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang("tfg.nuclear.skip"))))
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType SMR_GENERATOR = GTRecipeTypes
            .register("smr_generator", GTRecipeTypes.GENERATOR)
            .setEUIO(IO.OUT)
            .setMaxIOSize(0, 0, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.ATOMIC_OVERLAY_1))
            .setSound(GTSoundEntries.TURBINE);

    public static final GTRecipeType NUCLEAR_FUEL_FACTORY = GTRecipeTypes
            .register("nuclear_fuel_factory", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 3, 1, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.ATOMIC_OVERLAY_1)
                    .addRecipeUIModifier((recipe, widget) -> {
                        String heatText1 = recipe.data.getString("avgHeat1");
                        String heatText2 = recipe.data.getString("avgHeat2");
                        widget.textComponents.childIf(!heatText1.isEmpty(), () -> Text.lang("tfg.nuclear.average_heat.text", heatText1, heatText2).asWidget());
                    }))
            .setSound(GTSoundEntries.CUT);

    public static final GTRecipeType HYDROPONICS_FACILITY_RECIPES = GTRecipeTypes
            .register("hydroponics_facility", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.UP, PROGRESS_BAR_EGH)))
            .setSound(GTSoundEntries.MINER);

    public static final GTRecipeType PISCICULTURE_FISHERY_RECIPES = GTRecipeTypes
            .register("pisciculture_fishery", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.RIGHT, PROGRESS_BAR_FISH)))
            .setSound(GTSoundEntries.CHEMICAL);

    public static final GTRecipeType STEAM_BLOOMERY = GTRecipeTypes
            .register("steam_bloomery", GTRecipeTypes.STEAM)
            .setMaxIOSize(2, 1, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
            .setSound(GTSoundEntries.FIRE);

    public static final GTRecipeType PRECISION_FABRICATOR_RECIPES = GTRecipeTypes
            .register("high_temperature_precision_fabricator", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 1, 3, 0)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.DOWN, PROGRESS_BAR_BOULE))
                    .setItemSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.HEATING_OVERLAY_1)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.FURNACE_OVERLAY_2)
                    .addRecipeUIModifier(GTRecipeUIModifiers.TEMP_COIL_INFO))
            .setSound(GTSoundEntries.ARC);

    public static final GTRecipeType SUPER_BOILER = GTRecipeTypes
            .register("super_boiler", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 1)
            .setSound(GTSoundEntries.FURNACE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL));

    public static final GTRecipeType PASTORAL_ENGINE_RECIPES = GTRecipeTypes
            .register("pastoral_engine", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 1, 0, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType ORE_PROCESSING_GAS = GTRecipeTypes
            .register("ore_processing_gas", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 9, 2, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang("tfg.gui.ore_processing_gas.optimal_ratio.1")))
                    .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang("tfg.gui.ore_processing_gas.optimal_ratio.2"))))
            .setSound(TFGSounds.GEOLOGIC_VULCANIZER);
}
