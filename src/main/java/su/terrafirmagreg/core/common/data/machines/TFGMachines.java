package su.terrafirmagreg.core.common.data.machines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableSteamMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import java.util.function.BiFunction;

import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

public class TFGMachines {

	public static void init() { }

	// Left here for future reference, but remember that steam machines can't handle fluids

//	public static final MachineDefinition STEAM_AQUEOUS_ACCUMULATOR =
//		registerSteamMachine("aqueous_accumulator",
//			SimpleSteamMachine::new, (pressure, builder) -> builder
//				.rotationState(RotationState.ALL)
//				.recipeType(TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES)
//				.recipeModifier(SimpleSteamMachine::recipeModifier)
//				.renderer(() -> new WorkableSteamMachineRenderer(pressure, GTCEu.id("block/machines/aqueous_accumulator")))
//				.register());

	public static final MachineDefinition[] AQUEOUS_ACCUMULATOR =
		registerTieredMachines("aqueous_accumulator",
			AqueousAccumulatorMachine::new, (tier, builder) -> builder
				.langValue("%s Aqueous Accumulator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
				.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("aqueous_accumulator"), TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES))
				.rotationState(RotationState.NON_Y_AXIS)
				.recipeType(TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES)
				.recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
				.workableTieredHullRenderer(GTCEu.id("block/machines/aqueous_accumulator"))
				.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
				.tooltips(GTMachineUtils.explosion())
				.register(),
			GTMachineUtils.LOW_TIERS);



	public static MachineDefinition[] registerTieredMachines(String name,
															 BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
															 BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
															 int... tiers) {
		MachineDefinition[] definitions = new MachineDefinition[tiers.length];
		for (int i = 0; i < tiers.length; i++) {
			int tier = tiers[i];
			var register =  REGISTRATE.machine(GTValues.VN[tier].toLowerCase() + "_" + name,
				holder -> factory.apply(holder, tier)).tier(tier);
			definitions[i] = builder.apply(tier, register);
		}
		return definitions;
	}

	public static MachineDefinition registerSteamMachine(String name,
													     BiFunction<IMachineBlockEntity, Boolean, MetaMachine> factory,
													     BiFunction<Boolean, MachineBuilder<MachineDefinition>, MachineDefinition> builder) {
		return builder.apply(true,
			REGISTRATE.machine("hp_%s".formatted(name), holder -> factory.apply(holder, true))
				.tier(1));
	}
}
