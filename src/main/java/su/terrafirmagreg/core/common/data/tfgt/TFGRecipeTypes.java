package su.terrafirmagreg.core.common.data.tfgt;

import java.util.Collections;
import java.util.List;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.recipe.condition.AdjacentFluidCondition;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidHolderSetList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import fi.dea.mc.deafission.common.data.recipe.HeatRecipeCapability;

@SuppressWarnings("deprecation")
public class TFGRecipeTypes {

    public static void init() {
    }

    public static final GTRecipeType GREENHOUSE_RECIPES = GTRecipeTypes.register("greenhouse", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(3, 4, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH);

    public static final ResourceTexture PROGRESS_BAR_DNA = new ResourceTexture(
            "tfg:textures/gui/progress_bar/progress_bar_dna.png"); // I might move this later if we end up making/using
                                                                                                                                             // more custom progress bars.
    public static final GTRecipeType BIOREACTOR_RECIPES = GTRecipeTypes.register("bioreactor", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 6, 6, 6)
            .setProgressBar(PROGRESS_BAR_DNA, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.BATH)
            .setUiBuilder((recipe, widgetGroup) -> {
                var text = recipe.data.getString("action");
                if (!text.isEmpty()) {
                    widgetGroup.addWidget(new LabelWidget(widgetGroup.getSize().width - 50,
                            widgetGroup.getSize().height - 30, Component.translatable(text))
                            .setTextColor(-1)
                            .setDropShadow(true));
                }
            });

    public static final ResourceTexture PROGRESS_BAR_PETRI = new ResourceTexture(
            "tfg:textures/gui/progress_bar/progress_bar_petri.png");
    public static final GTRecipeType GROWTH_CHAMBER_RECIPES = GTRecipeTypes
            .register("growth_chamber", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(18, 6, 3, 3)
            .setProgressBar(PROGRESS_BAR_PETRI, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL)
            .setUiBuilder((recipe, widgetGroup) -> {
                var text = recipe.data.getString("action");
                if (!text.isEmpty()) {
                    widgetGroup.addWidget(new LabelWidget(widgetGroup.getSize().width - 50,
                            widgetGroup.getSize().height - 30, Component.translatable(text))
                            .setTextColor(-1)
                            .setDropShadow(true));
                }
            });

    public static final GTRecipeType FOOD_OVEN_RECIPES = GTRecipeTypes.register("food_oven", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 2, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.FURNACE);

    public static final GTRecipeType FOOD_PROCESSOR_RECIPES = GTRecipeTypes
            .register("food_processor", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(9, 2, 3, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.MIXER)
            .setUiBuilder((recipe, widgetGroup) -> {
                var text = recipe.data.getString("action");
                if (!text.isEmpty()) {
                    widgetGroup.addWidget(new LabelWidget(widgetGroup.getSize().width - 50,
                            widgetGroup.getSize().height - 30, Component.translatable(text))
                            .setTextColor(-1)
                            .setDropShadow(true));
                }
            });

    public static final GTRecipeType AQUEOUS_ACCUMULATOR_RECIPES = GTRecipeTypes
            .register("aqueous_accumulator", GTRecipeTypes.ELECTRIC)
            .setMaxIOSize(1, 0, 0, 1)
            .setEUIO(IO.IN)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, FillDirection.LEFT_TO_RIGHT)
            .setMaxTooltips(4)
            .setSound(GTSoundEntries.BATH)
            .setUiBuilder((recipe, widgetGroup) -> {
                // Copied and pasted from the rock breaker
                HolderSet<Fluid> fluid = null;
                for (RecipeCondition condition : recipe.conditions) {
                    if (condition instanceof AdjacentFluidCondition adjacentFluid) {
                        fluid = adjacentFluid.getOrInitFluids(recipe).get(0);
                        break;
                    }
                }
                if (fluid == null) {
                    return;
                }

                List<FluidEntryList> slots = Collections.singletonList(FluidHolderSetList.of(fluid, 1000, null));
                widgetGroup.addWidget(new TankWidget(new CycleFluidEntryHandler(slots),
                        widgetGroup.getSize().width - 50, widgetGroup.getSize().height - 35,
                        false, false)
                        .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false));
            });

    public static final GTRecipeType GAS_PRESSURIZER_RECIPES = GTRecipeTypes
            .register("gas_pressurizer", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(1, 1, 3, 1)
            .setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.COMPRESSOR);

    public static final GTRecipeType NUCLEAR_TURBINE = GTRecipeTypes
            .register("nuclear_turbine", GTRecipeTypes.GENERATOR)
            .setMaxIOSize(0, 0, 1, 1)
            .setSound(GTSoundEntries.TURBINE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressTexture.FillDirection.DOWN_TO_UP);

    public final static GTRecipeType EVAPORATION_TOWER = GTRecipeTypes
            .register("evaporation_tower", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(1, 1, 1, 12)
            .setEUIO(IO.IN)
            .setSound(GTSoundEntries.CHEMICAL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, FillDirection.LEFT_TO_RIGHT);

    public final static GTRecipeType COOLING_TOWER = GTRecipeTypes
            .register("cooling_tower", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(2, 2, 2, 2)
            .setSound(GTSoundEntries.CHEMICAL)
            .setProgressBar(GuiTextures.PROGRESS_BAR_BOILER_HEAT, FillDirection.LEFT_TO_RIGHT);

    public final static GTRecipeType OSTRUM_LINEAR_ACCELERATOR = GTRecipeTypes
            .register("ostrum_linear_accelerator", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(6, 9, 6, 6)
            .setMaxSize(IO.IN, HeatRecipeCapability.CAP, 1)
            .setMaxSize(IO.OUT, HeatRecipeCapability.CAP, 1)
            .setSound(GTSoundEntries.BATH)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, FillDirection.LEFT_TO_RIGHT)
            .addDataInfo(data -> LocalizationUtils.format("tfg.nuclear.skip"));

    public static final GTRecipeType SMR_GENERATOR = GTRecipeTypes
            .register("smr_generator", GTRecipeTypes.GENERATOR)
            .setEUIO(IO.OUT)
            .setMaxIOSize(0, 0, 1, 1)
            .setSound(GTSoundEntries.TURBINE)
            .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressTexture.FillDirection.DOWN_TO_UP);

    public static final GTRecipeType NUCLEAR_FUEL_FACTORY = GTRecipeTypes
            .register("nuclear_fuel_factory", GTRecipeTypes.ELECTRIC)
            .setEUIO(IO.IN)
            .setMaxIOSize(6, 3, 1, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
            .setSlotOverlay(false, false, GuiTextures.ATOMIC_OVERLAY_1)
            .setSound(GTSoundEntries.CUT)
            .addDataInfo(data -> {
                String heatText1 = data.getString("avgHeat1");
                String heatText2 = data.getString("avgHeat2");
                if (!heatText1.isEmpty()) {
                    return LocalizationUtils.format(
                            "tfg.nuclear.average_heat.text", heatText1, heatText2);
                }
                return "";
            });

}
