package su.terrafirmagreg.core.common.data.tfgt.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.*;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric.*;

import java.util.ArrayList;
import java.util.List;

import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

public class TFGMultiMachines {

	public static void init() { }

	public static final MultiblockMachineDefinition INTERPLANETARY_ITEM_LAUNCHER =
			REGISTRATE.multiblock("interplanetary_item_launcher", InterplanetaryItemLauncherMachine::new)
					.rotationState(RotationState.NON_Y_AXIS)
					.recipeType(GTRecipeTypes.DUMMY_RECIPES)
					.noRecipeModifier()
					.appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
					.workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"), GTCEu.id("block/multiblock/implosion_compressor"), false)
					.pattern(definition -> {

							return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
									// Each char in a string refers to the x-axis, each array of strings refers to the z-axis, and each aisle refers to the y-axis
									.aisle( "F###F",
											"#SSS#",
											"#SSS#",
											"#ESE#",
											"F###F")
									.aisle( "FsssF",
											"sSCSs",
											"sCCCs",
											"sSCSs",
											"FsysF")
									.aisle( "F###F",
											"#CCC#",
											"#CCC#",
											"#CCC#",
											"F###F")
									.aisle( "FFFFF",
											"FCCCF",
											"FC#CF",
											"FCCCF",
											"FFFFF")
									.aisle( "#####",
											"#FCF#",
											"#C#C#",
											"#FCF#",
											"#####").setRepeatable(4)
									.where('y', Predicates.controller(Predicates.blocks(definition.get())))
									.where('#', Predicates.air())
									.where('F', Predicates.frames(GTMaterials.Aluminium))
									.where('S', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
									.where('C', Predicates.blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
									.where('E', Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(2))
									.where('s', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()).or(Predicates.blocks(TFGMachines.RAILGUN_ITEM_LOADER_IN.get()).setMinGlobalLimited(1)))
										.build();
							}
					).register();

	public static final MultiblockMachineDefinition INTERPLANETARY_ITEM_RECIEVER =
			REGISTRATE.multiblock("interplanetary_item_reciever", InterplanetaryItemRecieverMachine::new)
					.rotationState(RotationState.NON_Y_AXIS)
					.recipeType(GTRecipeTypes.DUMMY_RECIPES)
					.noRecipeModifier()
					.appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
					.sidedWorkableCasingRenderer("block/casings/steam/steel", GTCEu.id("block/multiblock/implosion_compressor"), false)
					.pattern( def -> FactoryBlockPattern.start()
							.aisle("sys", "SSS", "SCS")
							.aisle("sSs", "S#S", "C#C")
							.aisle("sss", "SSS", "SCS")
							.where('#', Predicates.air())
							.where('y', Predicates.controller(Predicates.blocks(def.get())))
							.where('s', Predicates.blocks(GTBlocks.STEEL_HULL.get()).or(Predicates.abilities(PartAbility.INPUT_ENERGY).or(Predicates.blocks(TFGMachines.RAILGUN_ITEM_LOADER_OUT.get()))))
							.where('S', Predicates.blocks(GTBlocks.STEEL_HULL.get()))
							.where('C', Predicates.blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
							.build())
					.register();

	public static final MultiblockMachineDefinition ELECTRIC_GREENHOUSE =
		REGISTRATE.multiblock("electric_greenhouse", GreenhouseMachine::new)
		.rotationState(RotationState.NON_Y_AXIS)
		.recipeType(TFGRecipeTypes.GREENHOUSE_RECIPES)
		.recipeModifier(GTRecipeModifiers.OC_PERFECT)
		.appearanceBlock(GTBlocks.STEEL_HULL)
		.sidedWorkableCasingRenderer("block/casings/steam/steel", GTCEu.id("block/multiblock/implosion_compressor"), false)
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
				.or(Predicates.blockTag(BlockTags.LEAVES))
				.or(Predicates.blockTag(TFCTags.Blocks.PLANTS))
				// Apparently TFC likes to sometimes spawn loose rocks on top of grass, including inside the greenhouse?
				.or(Predicates.blockTag(dev.latvian.mods.kubejs.util.Tags.block(new ResourceLocation("tfc", "loose_rocks")))))
			.where(' ', Predicates.any())
			.where('F', Predicates.frames(GTMaterials.Steel))
			.where('X', Predicates.blockTag(Tags.Blocks.GLASS)
				.or(Predicates.blocks(ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ae2", "quartz_glass")))))
			.where('D', Predicates.blockTag(BlockTags.DIRT)
				.or(Predicates.blockTag(TFCTags.Blocks.GRASS)))
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
				.aisle("mitYfee", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "###F###")
				.where('Y', definition, Direction.SOUTH)
				.where('C', GTBlocks.STEEL_HULL.getDefaultState())
				.where('D', ForgeRegistries.BLOCKS.getValue(new ResourceLocation("tfc", "grass/loam")))
				.where('F', ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel))
				.where('X', ForgeRegistries.BLOCKS.getValue(new ResourceLocation("create", "framed_glass")))
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
}
