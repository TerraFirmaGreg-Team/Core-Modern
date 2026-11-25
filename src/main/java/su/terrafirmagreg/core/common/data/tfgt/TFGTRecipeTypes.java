package su.terrafirmagreg.core.common.data.tfgt;

import static su.terrafirmagreg.core.common.tfgt.ui.TFGUITextures.*;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;
import com.gregtechceu.gtceu.api.recipe.gui.RecipeUIModifier;
import com.gregtechceu.gtceu.api.sound.ExistingSoundEntry;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.recipe.gui.GTRecipeUIModifiers;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.progress.CircularProgressDrawable;
import brachy.modularui.drawable.progress.ProgressDrawable;
import fi.dea.mc.deafission.common.data.recipe.HeatRecipeCapability;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGSounds;
import su.terrafirmagreg.core.common.tfgt.ui.TFGRecipeModifiers;

public class TFGTRecipeTypes {

    public static void init() {
    }

    public static final GTRecipeType GREENHOUSE_RECIPES = GTRecipeTypes.register(TFGCore.id("greenhouse"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.UP, PROGRESS_BAR_EGH)))
            .setSound(GTSoundEntries.MINER);

    public static final GTRecipeType BIOREACTOR_RECIPES = GTRecipeTypes.register(TFGCore.id("bioreactor"), GTRecipeTypes.MULTIBLOCK)
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
            .register(TFGCore.id("growth_chamber"), GTRecipeTypes.MULTIBLOCK)
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

    public static final GTRecipeType FOOD_OVEN_RECIPES = GTRecipeTypes.register(TFGCore.id("food_oven"), GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(2, 2, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.FURNACE_OVERLAY_1))
            .setSound(GTSoundEntries.FURNACE);

    public static final GTRecipeType FOOD_PROCESSOR_RECIPES = GTRecipeTypes
            .register(TFGCore.id("food_processor"), GTRecipeTypes.ELECTRIC)
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
            .register(TFGCore.id("aqueous_accumulator"), GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.INT_CIRCUIT_OVERLAY))
            .setMaxTooltips(4)
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType GAS_PRESSURIZER_RECIPES = GTRecipeTypes
            .register(TFGCore.id("gas_pressurizer"), GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(3, 1, 3, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_COMPRESS)
                    .setItemSlotOverlay(IO.IN, 2, GTGuiTextures.INT_CIRCUIT_OVERLAY))
            .setSound(GTSoundEntries.COMPRESSOR);

    public static final GTRecipeType NUCLEAR_TURBINE = GTRecipeTypes
            .register(TFGCore.id("nuclear_turbine"), GTRecipeTypes.GENERATOR)
            .setMaxIOSize(0, 0, 1, 1)
            .setSound(GTSoundEntries.TURBINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));

    public final static GTRecipeType EVAPORATION_TOWER = GTRecipeTypes
            .register(TFGCore.id("evaporation_tower"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 12)
            .setEUIO(IO.IN)
            .setSound(GTSoundEntries.CHEMICAL)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW_MULTIPLE));

    public final static GTRecipeType COOLING_TOWER = GTRecipeTypes
            .register(TFGCore.id("cooling_tower"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 2)
            .setSound(GTSoundEntries.TURBINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));

    public final static GTRecipeType HEAT_EXCHANGER = GTRecipeTypes
            .register(TFGCore.id("heat_exchanger"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 3, 3)
            .setSound(GTSoundEntries.TURBINE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR));

    public final static GTRecipeType OSTRUM_LINEAR_ACCELERATOR = GTRecipeTypes
            .register(TFGCore.id("ostrum_linear_accelerator"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 9, 6, 6)
            .setMaxSize(IO.IN, HeatRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, HeatRecipeCapability.CAP, 1)
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_CRACKING)
                    .setItemSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.ATOMIC_OVERLAY_1))
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType SMR_GENERATOR = GTRecipeTypes
            .register(TFGCore.id("smr_generator"), GTRecipeTypes.GENERATOR)
            .setEUIO(IO.OUT)
            .setMaxIOSize(0, 0, 1, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.ATOMIC_OVERLAY_1))
            .setSound(GTSoundEntries.TURBINE);

    public static final GTRecipeType NUCLEAR_FUEL_FACTORY = GTRecipeTypes
            .register(TFGCore.id("nuclear_fuel_factory"), GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 3, 1, 2)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.ATOMIC_OVERLAY_1)
                    .addRecipeUIModifier((recipe, widget) -> {
                        String heatText1 = recipe.data.getString("avgHeat1");
                        String heatText2 = recipe.data.getString("avgHeat2");
                        widget.textComponents.childIf(!heatText1.isEmpty(), () -> Text.lang("tfg.nuclear.average_heat.text", heatText1, heatText2).asWidget());
                    }))
            .setSound(GTSoundEntries.CUT);

    public static final GTRecipeType HYDROPONICS_FACILITY_RECIPES = GTRecipeTypes
            .register(TFGCore.id("hydroponics_facility"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.UP, PROGRESS_BAR_EGH)))
            .setSound(GTSoundEntries.MINER);

    public static final GTRecipeType PISCICULTURE_FISHERY_RECIPES = GTRecipeTypes
            .register(TFGCore.id("pisciculture_fishery"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 3, 3)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.RIGHT, PROGRESS_BAR_FISH)))
            .setSound(GTSoundEntries.CHEMICAL);

    public static final GTRecipeType STEAM_BLOOMERY = GTRecipeTypes
            .register(TFGCore.id("steam_bloomery"), GTRecipeTypes.STEAM)
            .setMaxIOSize(2, 1, 0, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotsOverlay(IO.IN, 0, 1, GTGuiTextures.FURNACE_OVERLAY_1))
            .setSound(GTSoundEntries.FIRE);

    public static final GTRecipeType PRECISION_FABRICATOR_RECIPES = GTRecipeTypes
            .register(TFGCore.id("high_temperature_precision_fabricator"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 1, 3, 0)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20, ProgressDrawable.Direction.DOWN, PROGRESS_BAR_BOULE))
                    .setItemSlotsOverlay(IO.IN, 0, 5, GTGuiTextures.HEATING_OVERLAY_1)
                    .setItemSlotOverlay(IO.OUT, 0, GTGuiTextures.FURNACE_OVERLAY_2)
                    .addRecipeUIModifier(GTRecipeUIModifiers.TEMP_COIL_INFO))
            .setSound(GTSoundEntries.ARC);

    public static final GTRecipeType SUPER_BOILER = GTRecipeTypes
            .register(TFGCore.id("super_boiler"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 0, 1, 1)
            .setSound(GTSoundEntries.FURNACE)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_BOILER_FUEL_STEEL));

    public static final GTRecipeType PASTORAL_ENGINE_RECIPES = GTRecipeTypes
            .register(TFGCore.id("pastoral_engine"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 1, 0, 1)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW))
            .setSound(GTSoundEntries.BATH);

    public static final GTRecipeType ORE_PROCESSING_GAS = GTRecipeTypes
            .register(TFGCore.id("ore_processing_gas"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 9, 2, 0)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang("tfg.gui.ore_processing_gas.optimal_ratio.1")))
                    .addRecipeUIModifier(RecipeUIModifier.textLine(Text.lang("tfg.gui.ore_processing_gas.optimal_ratio.2"))))
            .setSound(TFGSounds.GEOLOGIC_VULCANIZER);

    // While oxygen distribution recipes are running the room is oxygenated.
    public static final GTRecipeType OXYGEN_DISTRIBUTION = GTRecipeTypes
            .register(TFGCore.id("oxygen_distribution"), GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(0, 0, 1, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setSound(GTSoundEntries.COOLING);

    // While space heating recipes are running the bubble has safe temperature.
    public static final GTRecipeType SPACE_HEATING = GTRecipeTypes
            .register(TFGCore.id("space_heating"), GTRecipeTypes.ELECTRIC)
            //.setMaxIOSize(0, 0, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(new ProgressBarTextureSet(20,
                    ProgressDrawable.Direction.UP, GTGuiTextures.PROGRESS_BAR_BOILER_HEAT)))
            .setSound(GTSoundEntries.COOLING);

    // While gravity emission recipes are running the bubble has normal gravity.
    public static final GTRecipeType GRAVITY_EMISSION = GTRecipeTypes
            .register(TFGCore.id("gravity_emission"), GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_GAS_COLLECTOR))
            .setSound(GTSoundEntries.COOLING);

    public static final GTRecipeType ME_ASSEMBLER = GTRecipeTypes
            .register(TFGCore.id("me_assembler"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(9, 1, 3, 0)
            .setSound(GTSoundEntries.ASSEMBLER)
            .setHasResearchSlot(true)
            .onRecipeBuild(ResearchManager::createDefaultResearchRecipe)
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_ASSEMBLER)
                    .addRecipeUIModifier(((recipe, widget) -> widget.textComponents.child(
                            Text.lang("tfg.recipe.me_assembler.budding_hint").asWidget())))
                    .addRecipeUIModifier(TFGRecipeModifiers.ME_ASSEMBLER));

    public static final GTRecipeType BUDDING_CHARGE_RECIPES = GTRecipeTypes
            .register(TFGCore.id("budding_charger"), GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 0, 1, 0)
            .setSound(GTSoundEntries.COMPRESSOR)
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .addRecipeUIModifier(TFGRecipeModifiers.BUDDING_CHARGER));

    // region Ore processing multiblocks (ore_proc)

    public final static GTRecipeType TFG_ORE_WASHER_RECIPES = GTRecipeTypes
            .register(TFGCore.id("tfg_ore_washer"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 3, 1, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(16))
            .UI(builder -> builder
                    .setProgressBarSupplier((l, v, m) -> new CircularProgressDrawable()
                            .emptyTexture(GTGuiTextures.PROGRESS_BATH[0])
                            .filledTexture(GTGuiTextures.PROGRESS_BATH[1])
                            .clockwise()
                            .asWidget()
                            .value(v))
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotsOverlay(IO.OUT, 0, 2, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType TFG_ORE_MACERATOR_RECIPES = GTRecipeTypes
            .register(TFGCore.id("tfg_ore_macerator"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 4, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(150).EUt(2))
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_MACERATE)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotsOverlay(IO.OUT, 0, 3, GTGuiTextures.DUST_OVERLAY)
                    .addRecipeUIModifier(RecipeUIModifier
                            .textLine(Text.lang("gtceu.recipe.byproduct_tier", GTValues.VNF[GTValues.HV]))))
            .setSound(GTSoundEntries.MACERATOR);

    public final static GTRecipeType TFG_CHEMICAL_BATH_RECIPES = GTRecipeTypes
            .register(TFGCore.id("tfg_ore_chemical_bath"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 6, 1, 1)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(GTValues.VA[GTValues.LV]))
            .UI(builder -> builder
                    .setProgressBarSupplier((l, v, m) -> new CircularProgressDrawable()
                            .emptyTexture(GTGuiTextures.PROGRESS_BATH[0])
                            .filledTexture(GTGuiTextures.PROGRESS_BATH[1])
                            .clockwise()
                            .asWidget()
                            .value(v))
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.BREWER_OVERLAY)
                    .setItemSlotsOverlay(IO.OUT, 0, 5, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.BATH);

    public final static GTRecipeType TFG_ORE_CENTRIFUGE_RECIPES = GTRecipeTypes
            .register(TFGCore.id("tfg_ore_centrifuge"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 6, 1, 6)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.EUt(5))
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_EXTRACT)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.EXTRACTOR_OVERLAY)
                    .setItemSlotOverlay(IO.IN, 1, GTGuiTextures.CANISTER_OVERLAY)
                    .setFluidSlotOverlay(IO.IN, 0, GTGuiTextures.CENTRIFUGE_OVERLAY))
            .setSound(GTSoundEntries.CENTRIFUGE);

    public final static GTRecipeType TFG_SIFTER_RECIPES = GTRecipeTypes
            .register(TFGCore.id("tfg_sifter"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 6, 0, 0)
            .setEUIO(IO.IN)
            .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_SIFTER))
            .setSound(new ExistingSoundEntry(SoundEvents.SAND_PLACE, SoundSource.BLOCKS));

    public final static GTRecipeType TFG_THERMAL_CENTRIFUGE_RECIPES = GTRecipeTypes
            .register(TFGCore.id("tfg_thermal_centrifuge"), GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 3, 0, 0)
            .setEUIO(IO.IN)
            .prepareBuilder(recipeBuilder -> recipeBuilder.duration(400).EUt(30))
            .UI(builder -> builder
                    .setProgressBar(GTGuiTextures.PROGRESS_ARROW)
                    .setItemSlotOverlay(IO.IN, 0, GTGuiTextures.CRUSHED_ORE_OVERLAY)
                    .setItemSlotsOverlay(IO.OUT, 0, 2, GTGuiTextures.DUST_OVERLAY))
            .setSound(GTSoundEntries.CENTRIFUGE);

    // endregion
}
