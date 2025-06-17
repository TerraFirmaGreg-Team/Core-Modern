package su.terrafirmagreg.core.common.data.tfgt.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.common.data.tfgt.machine.electric.AqueousAccumulatorMachine;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeTypes;

import java.util.ArrayList;
import java.util.List;
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

	public static final MultiblockMachineDefinition ELECTRIC_GREENHOUSE = REGISTRATE.multiblock("electric_greenhouse", GreenhouseMachine::new)
		.rotationState(RotationState.NON_Y_AXIS)
		.recipeType(TFGRecipeTypes.GREENHOUSE_RECIPES)
		.recipeModifier(GTRecipeModifiers.OC_PERFECT)
		.appearanceBlock(GTBlocks.STEEL_HULL)
		.workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/implosion_compressor"), false)
		.pattern(definition -> FactoryBlockPattern.start()
			.aisle("CCCCCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "   F   ")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CDDDDDC", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "FFFFFFF")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CCCYCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "   F   ")
			.where('Y', Predicates.controller(Predicates.blocks(definition.get())))
			.where('C', Predicates.blocks(GTBlocks.STEEL_HULL.get()).setMinGlobalLimited(15)
				.or(Predicates.autoAbilities(definition.getRecipeTypes()))
				.or(Predicates.autoAbilities(true, false, false)))
			.where('#', Predicates.air()
				.or(Predicates.blockTag(BlockTags.LOGS))
				.or(Predicates.blockTag(BlockTags.LEAVES)))
			.where(' ', Predicates.any())
			.where('F', Predicates.frames(GTMaterials.Steel))
			.where('X', Predicates.blocks(Blocks.GLASS))
			.where('D', Predicates.blockTag(BlockTags.DIRT))
			.build())
		.shapeInfos(definition -> {
			List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
			var builder = MultiblockShapeInfo.builder()
				.aisle("CCCCCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "###F###")
				.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "#XXFXX#")
				.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X##L##X", "X#LLL#X", "X##L##X", "X#####X", "#XXFXX#")
				.aisle("CDDDDDC", "F##W##F", "F##W##F", "F##W##F", "F#LWL#F", "F#LWL#F", "F#LLL#F", "F#####F", "FFFFFFF")
				.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X##L##X", "X#LLL#X", "X##L##X", "X#####X", "#XXFXX#")
				.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "#XXFXX#")
				.aisle("mitYfeC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "###F###")
				.where('Y', definition, Direction.SOUTH)
				.where('C', GTBlocks.STEEL_HULL.getDefaultState())
				.where('D', ForgeRegistries.BLOCKS.getValue(new ResourceLocation("tfc", "dirt/loam")))
				.where('F', ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel))
				.where('X', Blocks.GLASS)
				.where('W', ForgeRegistries.BLOCKS.getValue(new ResourceLocation("tfc", "wood/wood/oak")))
				.where('L', ForgeRegistries.BLOCKS.getValue(new ResourceLocation("tfc", "wood/leaves/oak")))
				.where('#', Blocks.AIR)
				.where('i', GTMachines.ITEM_IMPORT_BUS[GTValues.ULV], Direction.SOUTH)
				.where('t', GTMachines.ITEM_EXPORT_BUS[GTValues.MV], Direction.SOUTH)
				.where('f', GTMachines.FLUID_IMPORT_HATCH[GTValues.ULV], Direction.SOUTH)
				.where('e', GTMachines.ENERGY_INPUT_HATCH[GTValues.LV], Direction.SOUTH)
				.where('m', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH);
			shapeInfo.add(builder.build());
			return shapeInfo;
		})
		.register();

	public static final MachineDefinition[] FOOD_PROCESSOR =
		registerTieredMachines("food_processor",
		SimpleFoodProcessingMachine::new, (tier, builder) -> builder
			.langValue("%s Food Processor %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			.rotationState(RotationState.NON_Y_AXIS)
			.recipeType(TFGRecipeTypes.FOOD_PROCESSOR_RECIPES)
			.recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT)
			.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("food_processor"), TFGRecipeTypes.FOOD_PROCESSOR_RECIPES))
			.workableTieredHullRenderer(GTCEu.id("block/machines/food_processor"))
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
			.workableTieredHullRenderer(GTCEu.id("block/machines/food_oven"))
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
			.workableTieredHullRenderer(GTCEu.id("block/machines/food_refrigerator"))
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
