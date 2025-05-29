package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import it.unimi.dsi.fastutil.Pair;

public class TFGMachines {

	static {
		GTRegistration.REGISTRATE.creativeModeTab(() -> GTCreativeModeTabs.MACHINE);
		GTRegistries.MACHINES.unfreeze();
	}

	public static void init() {
		GTRegistryInfo.registerFor(GTRegistries.MACHINES.getRegistryName());
		GTRegistries.MACHINES.freeze();
	}

	public static final Pair<MachineDefinition, MachineDefinition> STEAM_AQUEOUS_ACCUMULATOR =
		GTMachineUtils.registerSimpleSteamMachines("aqueous_accumulator", TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES);

	public static final MachineDefinition[] AQUEOUS_ACCUMULATOR =
		GTMachineUtils.registerTieredMachines("aqueous_accumulator",
			AqueousAccumulatorMachine::new, (tier, builder) -> builder
				.langValue("%s Aqueous Accumulator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
				.rotationState(RotationState.NON_Y_AXIS)
				.recipeType(TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES)
				.recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
				.workableTieredHullRenderer(GTCEu.id("block/machines/item_collector"))
				.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
				.tooltips(GTMachineUtils.explosion())
				.register(),
			GTMachineUtils.ELECTRIC_TIERS);
}
