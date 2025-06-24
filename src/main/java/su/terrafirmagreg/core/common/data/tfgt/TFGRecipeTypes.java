package su.terrafirmagreg.core.common.data.tfgt;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.recipe.condition.RockBreakerCondition;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("deprecation")
public class TFGRecipeTypes {

	public static void init() { }

	public final static GTRecipeType GREENHOUSE_RECIPES =
		GTRecipeTypes.register("greenhouse", GTRecipeTypes.MULTIBLOCK)
			.setEUIO(IO.IN)
			.setMaxIOSize(3, 4, 1, 0)
			.setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
			.setSound(GTSoundEntries.BATH);

	public final static GTRecipeType FOOD_OVEN_RECIPES =
		GTRecipeTypes.register("food_oven", GTRecipeTypes.ELECTRIC)
			.setEUIO(IO.IN)
			.setMaxIOSize(1, 1, 1, 0)
			.setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
			.setSound(GTSoundEntries.FURNACE);

	public final static GTRecipeType FOOD_PROCESSOR_RECIPES =
		GTRecipeTypes.register("food_processor", GTRecipeTypes.ELECTRIC)
			.setEUIO(IO.IN)
			.setMaxIOSize(6, 2, 2, 1)
			.setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, FillDirection.LEFT_TO_RIGHT)
			.setSound(GTSoundEntries.MIXER)
			.setUiBuilder((recipe, widgetGroup) -> {
				var text = recipe.data.getString("action");
				if (!text.isEmpty()) {
					widgetGroup.addWidget(new LabelWidget(widgetGroup.getSize().width - 50, widgetGroup.getSize().height - 30, Component.translatable(text))
						.setTextColor(-1)
						.setDropShadow(true));
				}
			});

	public final static GTRecipeType AQUEOUS_ACCUMULATOR_RECIPES =
		GTRecipeTypes.register("aqueous_accumulator", GTRecipeTypes.ELECTRIC)
			.setMaxIOSize(1, 0, 0, 1)
			.setEUIO(IO.IN)
			.setSlotOverlay(false, false, GuiTextures.INT_CIRCUIT_OVERLAY)
			.setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
			.setMaxTooltips(4)
			.setSound(GTSoundEntries.BATH)
			.prepareBuilder(recipeBuilder -> recipeBuilder.addCondition(RockBreakerCondition.INSTANCE))
			.setUiBuilder((recipe, widgetGroup) -> {
				var fluidA = BuiltInRegistries.FLUID.get(ResourceLocation.parse(recipe.data.getString("fluidA")));
				if (fluidA != Fluids.EMPTY) {
					widgetGroup.addWidget(new TankWidget(new CustomFluidTank(new FluidStack(fluidA, 1000)),
						widgetGroup.getSize().width - 50, widgetGroup.getSize().height - 35, false, false)
							.setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false));
				}
			});

}
