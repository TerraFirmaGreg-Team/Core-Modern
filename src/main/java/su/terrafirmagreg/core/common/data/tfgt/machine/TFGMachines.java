package su.terrafirmagreg.core.common.data.tfgt.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.network.chat.Component;
import su.terrafirmagreg.core.common.data.tfgt.machine.electric.*;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.part.RailgunItemBusMachine;

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


	public static final MachineDefinition[] FOOD_PROCESSOR =
		registerTieredMachines("food_processor",
		SimpleFoodProcessingMachine::new, (tier, builder) -> builder
			.langValue("%s Food Processor %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			.rotationState(RotationState.NON_Y_AXIS)
			.recipeType(TFGRecipeTypes.FOOD_PROCESSOR_RECIPES)
			.recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT)
			.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("food_processor"), TFGRecipeTypes.FOOD_PROCESSOR_RECIPES))
			.workableTieredHullModel(GTCEu.id("block/machines/food_processor"))
			.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.FOOD_PROCESSOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
			.register(),
		GTMachineUtils.LOW_TIERS);

	public static final MachineDefinition[] FOOD_OVEN =
		registerTieredMachines("food_oven",
				SimpleFoodProcessingMachine::new, (tier, builder) -> builder
			.langValue("%s Electric Oven %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			.rotationState(RotationState.NON_Y_AXIS)
			.recipeType(TFGRecipeTypes.FOOD_OVEN_RECIPES)
			.recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
			.workableTieredHullModel(GTCEu.id("block/machines/food_oven"))
			.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("food_oven"), TFGRecipeTypes.FOOD_OVEN_RECIPES))
			.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.FOOD_PROCESSOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
			.register(),
		GTMachineUtils.LOW_TIERS);

	public static final MachineDefinition[] FOOD_REFRIGERATOR =
		registerTieredMachines("food_refrigerator",
			FoodRefrigeratorMachine::new, (tier, builder) -> builder
			.langValue("%s Refrigerator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			.rotationState(RotationState.NON_Y_AXIS)
			.tooltips(
				Component.translatable("gtceu.universal.tooltip.voltage_in", FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
				Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", FormattingUtil.formatNumbers(GTValues.V[tier] * 64)),
				Component.translatable("gtceu.universal.tooltip.item_storage_capacity", FoodRefrigeratorMachine.INVENTORY_SIZE(tier))
			)
			.workableTieredHullModel(GTCEu.id("block/machines/food_refrigerator"))
			.register(),
			GTValues.tiersBetween(GTValues.MV, GTValues.EV));

	public static final MachineDefinition[] AQUEOUS_ACCUMULATOR =
		registerTieredMachines("aqueous_accumulator",
			AqueousAccumulatorMachine::new, (tier, builder) -> builder
				.langValue("%s Aqueous Accumulator %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
				.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("aqueous_accumulator"), TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES))
				.rotationState(RotationState.NON_Y_AXIS)
				.recipeType(TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES)
				.recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
				.workableTieredHullModel(GTCEu.id("block/machines/aqueous_accumulator"))
				.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.AQUEOUS_ACCUMULATOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
				.tooltips(GTMachineUtils.explosion())
				.register(),
			GTMachineUtils.LOW_TIERS);

	public static final MachineDefinition[] GAS_PRESSURIZER =
		registerTieredMachines("gas_pressurizer",
			GasPressurizerMachine::new, (tier, builder) -> builder
			   .langValue("%s Gas Pressurizer %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			   .rotationState(RotationState.NON_Y_AXIS)
			   .recipeType(TFGRecipeTypes.GAS_PRESSURIZER_RECIPES)
			   .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
			   .workableTieredHullModel(GTCEu.id("block/machines/gas_pressurizer"))
			   .tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
				   TFGRecipeTypes.GAS_PRESSURIZER_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
			   .register(),
			GTMachineUtils.LOW_TIERS);

	public static final MachineDefinition[] RAILGUN_ITEM_LOADER_IN = registerTieredMachines("railgun_item_loader_in", (holder, tier) -> new RailgunItemBusMachine(holder, tier, IO.IN),
			(tier, builder) ->
			builder.langValue("%s Interplanetary Railgun Loader %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
					.rotationState(RotationState.ALL)
					.overlayTieredHullModel(GTCEu.id("block/machine/part/item_bus.import"))
					.abilities(PartAbility.IMPORT_ITEMS)
					.register(),
			GTMachineUtils.ALL_TIERS);

	public static final MachineDefinition[] RAILGUN_ITEM_LOADER_OUT = registerTieredMachines("railgun_item_loader_out", (holder, tier) -> new RailgunItemBusMachine(holder, tier, IO.OUT),
			(tier, builder) ->
					builder.langValue("%s Interplanetary Railgun Unloader %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
							.rotationState(RotationState.ALL)
							.overlayTieredHullModel(GTCEu.id("block/machine/part/item_bus.export"))
							.abilities(PartAbility.EXPORT_ITEMS)
							.register(),
			GTMachineUtils.ALL_TIERS);

	public static final MachineDefinition INTERPLANETARY_LOGISTICS_MONITOR = REGISTRATE.machine("interplanetary_logistics_monitor", InterplanetaryLogisticsMonitorMachine::new)
			.rotationState(RotationState.NON_Y_AXIS)
			.register();


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
