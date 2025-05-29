package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.recipe.condition.RockBreakerCondition;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings("deprecation")
public class TFGRecipeTypes {

	static {
		GTRegistries.RECIPE_TYPES.unfreeze();
	}

	public static void init()
	{
		GTRegistryInfo.registerFor(GTRegistries.RECIPE_TYPES.getRegistryName());
		GTRegistries.RECIPE_TYPES.freeze();
	}

	public final static GTRecipeType AQUEOUS_ACCUMULATOR_RECIPES =
		GTRecipeTypes.register("aqueous_accumulator", GTRecipeTypes.ELECTRIC)
			.setMaxIOSize(0, 0, 1, 1)
			.setEUIO(IO.IN)
			.setSlotOverlay(true, true, GuiTextures.FLUID_TANK_OVERLAY)
			.setSlotOverlay(false, true, GuiTextures.FLUID_TANK_OVERLAY)
			.setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
			.setSteamProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT_STEAM, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
			.setMaxTooltips(4)
			.setSound(GTSoundEntries.BATH)
			.setIconSupplier(() -> TFGMachines.AQUEOUS_ACCUMULATOR[GTValues.LV].asStack())
			.prepareBuilder(recipeBuilder -> recipeBuilder.addCondition(RockBreakerCondition.INSTANCE))
			.setUiBuilder((recipe, widgetGroup) -> {
				var fluidA = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidA")));
				var fluidB = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidB")));
				if (fluidA != Fluids.EMPTY) {
					widgetGroup.addWidget(new TankWidget(new CustomFluidTank(new FluidStack(fluidA, 1000)),
						widgetGroup.getSize().width - 30, widgetGroup.getSize().height - 30, false, false)
											  .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false));
				}
				if (fluidB != Fluids.EMPTY) {
					widgetGroup.addWidget(new TankWidget(new CustomFluidTank(new FluidStack(fluidB, 1000)),
						widgetGroup.getSize().width - 30 - 20, widgetGroup.getSize().height - 30, false, false)
											  .setBackground(GuiTextures.FLUID_SLOT).setShowAmount(false));
				}
			});

}
