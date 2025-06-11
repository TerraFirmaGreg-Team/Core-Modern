package su.terrafirmagreg.core.common.data.machines;

import com.alekiponi.firmaciv.util.TFCWood;
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
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableSteamMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.recipes.ingredients.BlockIngredient.BlockTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

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
		.appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
		.workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/implosion_compressor"), false)
		.pattern(definition -> {
			TraceabilityPredicate energyHatch = Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2);
			TraceabilityPredicate importBus = Predicates.abilities(PartAbility.IMPORT_ITEMS).setExactLimit(1);
			TraceabilityPredicate exportBus = Predicates.abilities(PartAbility.EXPORT_ITEMS).setExactLimit(1);
			TraceabilityPredicate importHatch = Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1);
			TraceabilityPredicate maintHatch = Predicates.autoAbilities(true, false, false).setExactLimit(1);

			return FactoryBlockPattern.start()
			.aisle("CCCCCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "   F   ")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CDDDDDC", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "FFFFFFF")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", " XXFXX ")
			.aisle("CCCYCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "   F   ")
			.where('C', Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()).or(importBus).or(exportBus).or(importHatch).or(maintHatch).or(energyHatch))
			.where('Y', Predicates.controller(Predicates.blocks(definition.get())))
			.where('#', Predicates.air())
			.where(' ', Predicates.any())
			.where('F', Predicates.frames(GTMaterials.Steel))
			.where('X', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ae2", "quartz_glass"))))
			.where('D', Predicates.blockTag(BlockTags.DIRT))
			.build();
		})
		.register();

	public static final MachineDefinition[] FOOD_PROCESSOR = 
		registerTieredMachines("food_processor", 
		FoodProcessorMachine::new, (tier, builder) -> builder
			.langValue("%s Food Processor %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			.rotationState(RotationState.NON_Y_AXIS)
			.recipeType(TFGRecipeTypes.FOOD_PROCESSOR_RECIPES)
			.recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT)
			.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("mixer"), TFGRecipeTypes.FOOD_PROCESSOR_RECIPES))
			.workableTieredHullRenderer(GTCEu.id("block/machines/mixer"))
			.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.FOOD_PROCESSOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
			.register(),
		GTMachineUtils.LOW_TIERS);

	public static final MachineDefinition[] FOOD_OVEN = 
		registerTieredMachines("food_oven", 
		FoodProcessorMachine::new, (tier, builder) -> builder
			.langValue("%s Electric Oven %s".formatted(GTValues.VLVH[tier], GTValues.VLVT[tier]))
			.rotationState(RotationState.NON_Y_AXIS)
			.recipeType(TFGRecipeTypes.FOOD_OVEN_RECIPES)
			.recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
			.workableTieredHullRenderer(GTCEu.id("block/machines/electric_furnace"))
			.editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("electric_furnace"), TFGRecipeTypes.FOOD_OVEN_RECIPES))
			.tooltips(GTMachineUtils.workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64,
					TFGRecipeTypes.FOOD_PROCESSOR_RECIPES, GTMachineUtils.defaultTankSizeFunction.apply(tier), true))
			.register(),
		GTMachineUtils.LOW_TIERS);
	
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
