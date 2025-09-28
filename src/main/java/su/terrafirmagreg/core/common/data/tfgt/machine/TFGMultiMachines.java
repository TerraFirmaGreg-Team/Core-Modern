package su.terrafirmagreg.core.common.data.tfgt.machine;

import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.DistillationTowerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.tfgt.TFGRecipeTypes;
import su.terrafirmagreg.core.common.data.tfgt.machine.multiblock.electric.*;

public class TFGMultiMachines {

    public static void init() {
    }

    // spotless:off
    public static final MultiblockMachineDefinition INTERPLANETARY_ITEM_LAUNCHER = REGISTRATE
            .multiblock("interplanetary_item_launcher", InterplanetaryItemLauncherMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(definition -> {
                IMachineBlock[] inputBuses = Arrays.stream(TFGMachines.RAILGUN_ITEM_LOADER_IN)
                    .map(MachineDefinition::get).toArray(IMachineBlock[]::new);
                return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                    .aisle("F###F", "#SSS#", "#SSS#", "#ESE#", "F###F")
                    .aisle("FsssF", "sSCSs", "sCCCs", "sSCSs", "FsysF")
                    .aisle("F###F", "#LCL#", "#R R#", "#LCL#", "F###F")
                    .aisle("FFFFF", "FLCLF", "FR RF", "FLCLF", "FFFFF")
                    .aisle("#####", "#L#L#", "#R R#", "#L#L#", "#####").setRepeatable(3)
                    .aisle("#####", "#CHC#", "#R R#", "#CHC#", "#####")
                    .aisle("#####", "#M#M#", "#R R#", "#M#M#", "#####").setRepeatable(3)
                    .aisle("#####", "#CHC#", "#R R#", "#CHC#", "#####")
                    .aisle("#####", "#C#C#", "#R R#", "#C#C#", "#####").setRepeatable(2)
                    .where('y', Predicates.controller(Predicates.blocks(definition.get())))
                    .where(' ', Predicates.air())
                    .where('#', Predicates.any())
                    .where('F', Predicates.frames(GTMaterials.Aluminium))
                    .where('H', Predicates.frames(GTMaterials.HSLASteel))
                    .where('S', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where('C', Predicates.blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                    .where('E', Predicates.abilities(PartAbility.INPUT_ENERGY).setExactLimit(2))
                    .where('s', Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                        .or(Predicates.blocks(inputBuses).setMinGlobalLimited(1))
                        .or(Predicates.blocks(TFGMachines.RAILGUN_AMMO_LOADER.get()).setExactLimit(1)))
                    .where('L', Predicates.blocks(TFGBlocks.SUPERCONDUCTOR_COIL_LARGE_BLOCK.get()))
                    .where('M', Predicates.blocks(TFGBlocks.SUPERCONDUCTOR_COIL_SMALL_BLOCK.get()))
                    .where('R', Predicates.blocks(TFGBlocks.ELECTROMAGNETIC_ACCELERATOR_BLOCK.get()))
                    .build();
            }).register();

    public static final MultiblockMachineDefinition INTERPLANETARY_ITEM_RECEIVER = REGISTRATE
            .multiblock("interplanetary_item_receiver", InterplanetaryItemReceiverMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(TFGBlocks.MACHINE_CASING_ALUMINIUM_PLATED_STEEL)
            .workableCasingModel(
                ResourceLocation.fromNamespaceAndPath("tfg", "block/casings/machine_casing_aluminium_plated_steel"),
                GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(def -> {
                IMachineBlock[] inputBuses = Arrays.stream(TFGMachines.RAILGUN_ITEM_LOADER_OUT)
                    .map(MachineDefinition::get).toArray(IMachineBlock[]::new);
                return FactoryBlockPattern.start()
                    .aisle("B     B", "BB   BB", " B   B ", "  CCC  ", "       ")
                    .aisle("       ", "B     B", "BBbbbBB", " CEFEC ", "  GGG  ")
                    .aisle("       ", "       ", " b   b ", "CF   FC", " G   G ")
                    .aisle("       ", "       ", " b   b ", "CE   EC", " G   G ")
                    .aisle("       ", "       ", " b   b ", "CF   FC", " G   G ")
                    .aisle("       ", "B     B", "BBbDbBB", " CEFEC ", "  GGG  ")
                    .aisle("B     B", "BB   BB", " B   B ", "  CCC  ", "       ")
                    .where("B", Predicates.blocks(TFGBlocks.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get()))
                    .where("b", Predicates.blocks(TFGBlocks.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get())
                        .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                        .or(Predicates.blocks(inputBuses))))
                    .where("C", Predicates.frames(GTMaterials.Aluminium))
                    .where("D", Predicates.controller(Predicates.blocks(def.get())))
                    .where("E", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('F', Predicates.blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                    .where("G", Predicates.blocks(GTBlocks.YELLOW_STRIPES_BLOCK_A.get())
                        .or(Predicates.blocks(GTBlocks.YELLOW_STRIPES_BLOCK_B.get())))
                    .where(" ", Predicates.any())
                    .build();
            })
            .register();

    public static final MultiblockMachineDefinition ELECTRIC_GREENHOUSE = REGISTRATE
            .multiblock("electric_greenhouse", GreenhouseMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.GREENHOUSE_RECIPES)
            .recipeModifier(GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(GTBlocks.STEEL_HULL)
            .workableCasingModel(GTCEu.id("block/casings/steam/steel"),
                GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(definition -> FactoryBlockPattern.start()
                .aisle("CCCCCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "   F   ")
                .aisle("CDDDDDC", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", " XXFXX ")
                .aisle("CDDDDDC", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", " XXFXX ")
                .aisle("CDDDDDC", "F     F", "F     F", "F     F", "F     F", "F     F", "F     F", "F     F", "FFFFFFF")
                .aisle("CDDDDDC", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", " XXFXX ")
                .aisle("CDDDDDC", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", "X     X", " XXFXX ")
                .aisle("CCCYCCC", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "   F   ")
                .where('Y', Predicates.controller(Predicates.blocks(definition.get())))
                .where('C', Predicates.blocks(GTBlocks.STEEL_HULL.get()).setMinGlobalLimited(15)
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.autoAbilities(true, false, false)))
                .where(' ', Predicates.any())
                .where('F', Predicates.frames(GTMaterials.Steel)
                    .or(Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get())))
                .where('X', Predicates.blockTag(Tags.Blocks.GLASS)
                    .or(Predicates.blocks(ForgeRegistries.BLOCKS
                        .getValue(ResourceLocation.fromNamespaceAndPath("ae2", "quartz_glass")))))
                .where('D', Predicates.blockTag(BlockTags.DIRT)
                    .or(Predicates.blockTag(TFCTags.Blocks.GRASS))
                    .or(Predicates.blockTag(BlockTags.SAND))
                    .or(Predicates.blockTag(TFCTags.Blocks.FARMLAND)))
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
                    .where('D', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfc", "grass/loam")))
                    .where('F', ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel))
                    .where('X', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("create", "framed_glass")))
                    .where('W', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfc", "wood/wood/oak")))
                    .where('L', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfc", "wood/leaves/oak")))
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

    private static final Supplier<Block> bioculture_casing = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture"));
    public static final MultiblockMachineDefinition BIOREACTOR = REGISTRATE
            .multiblock("bioreactor", BioreactorMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.BIOREACTOR_RECIPES)
            .recipeModifier(GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(bioculture_casing)
            .workableCasingModel(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture"),
                GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(definition -> FactoryBlockPattern.start()
                .aisle("#A#A#BCB#", "#BBB#DDD#", "#EEE#DDD#", "#EEE#FFF#", "#EEE#EEE#", "#EEE#EEE#", "#EEE#BCB#", "#BBB#####")
                .aisle("AGGGABBBB", "BBBBDHHHD", "E   DHHHD", "E   BBBBF", "E   EI IE", "E   EI IE", "E   BBBBB", "BBBBB####")
                .aisle("#GGGABBBC", "BBBBDHHHD", "E J DHHHD", "E J BBBBF", "E J E K E", "E   E   E", "E   BBBBC", "BBBBB####")
                .aisle("AGGGABBBB", "BBBBDHHHD", "E   DHHHD", "E   BBBBF", "E   EI IE", "E   EI IE", "E   BBBBB", "BBBBB####")
                .aisle("#A#A#BCB#", "#BBB#DDD#", "#EEE#DDD#", "#EEE#FLF#", "#EEE#EEE#", "#EEE#EEE#", "#EEE#BCB#", "#BBB#####")
                .where(" ", Predicates.air())
                .where("#", Predicates.any())
                .where("A", Predicates.blocks(GTBlocks.CASING_PTFE_INERT.get()))
                .where("B", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture"))))
                .where("C", Predicates.blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                .where("D", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_ultraviolet"))))
                .where("E", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture_glass"))))
                .where("F", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture")))
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.autoAbilities(true, false, false)))
                .where("G", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                .where("H", Predicates.blocks(GTBlocks.FILTER_CASING.get()))
                .where("I", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("gtceu", "purple_lamp"))))
                .where("J", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/bioculture_rotor_primary"))))
                .where("K", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/bioculture_rotor_secondary"))))
                .where("L", Predicates.controller(Predicates.blocks(definition.get())))
                .build())
            .register();

    public static final MultiblockMachineDefinition NUCLEAR_TURBINE = REGISTRATE
            .multiblock("nuclear_turbine", (holder) -> new LargeTurbineMachine(holder, GTValues.EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.NUCLEAR_TURBINE)
            .recipeModifier(LargeTurbineMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_STEEL_TURBINE)
            .workableCasingModel(GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"), GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .pattern(definition -> FactoryBlockPattern.start()
                .aisle("A   A", "A   A", "CCCCC", "CDCDC", "CDCDC", "CCCCC", "BBBBB", "     ", "     ", "     ", "     ")
                .aisle("     ", "     ", "CCCCC", "DEFED", "DEFED", "CAAAC", "BAAAB", " AAA ", "  A  ", "  A  ", "  A  ")
                .aisle("     ", "     ", "CCGCC", "CFHFC", "CFHFC", "CAFAC", "BAFAB", " A A ", " A A ", " A A ", " A A ")
                .aisle("     ", "     ", "CCCCC", "DEFED", "DEFED", "CAAAC", "BAAAB", " AAA ", "  A  ", "  A  ", "  A  ")
                .aisle("A   A", "A   A", "CCCCC", "CDYDC", "CDCDC", "CCCCC", "BBBBB", "     ", "     ", "     ", "     ")
                .where(" ", Predicates.air())
                .where('Y', Predicates.controller(Predicates.blocks(definition.get())))
                .where("A", Predicates.blocks(TFGBlocks.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get()))
                .where("B", Predicates.frames(GTMaterials.StainlessSteel))
                .where("C", Predicates.blocks(GTBlocks.CASING_STEEL_TURBINE.get()).setMinGlobalLimited(50)
                    .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                    .or(Predicates.autoAbilities(true, false, false))
                    .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1).setPreviewCount(1)))
                .where("D", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("ad_astra", "vent"))))
                .where("E", Predicates.blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                .where("F", Predicates.blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                .where("G", Predicates.blocks(PartAbility.ROTOR_HOLDER.getBlockRange(GTValues.EV, GTValues.UHV).toArray(Block[]::new)))
                .where("H", Predicates.blocks(GTBlocks.CASING_TITANIUM_GEARBOX.get()))
                .build())
            .register();

    private static final Supplier<Block> evaporation_casing = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_stainless_evaporation"));
    public static final MultiblockMachineDefinition EVAPORATION_TOWER = REGISTRATE
            .multiblock("evaporation_tower", DistillationTowerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.EVAPORATION_TOWER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(evaporation_casing)
            .workableCasingModel(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture"), GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(definition -> {
                TraceabilityPredicate exportPredicate = Predicates.abilities(PartAbility.EXPORT_FLUIDS_1X).or(Predicates.blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
                exportPredicate.setMaxLayerLimited(1);

                TraceabilityPredicate maint = Predicates.autoAbilities(true, false, false).setMaxGlobalLimited(1);
                return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                    .aisle("YSY", "YYY", "YYY")
                    .aisle("ZZZ", "Z#Z", "ZZZ")
                    .aisle("XXX", "X#X", "XXX").setRepeatable(0, 10)
                    .aisle("XXX", "XXX", "XXX")
                    .where('S', Predicates.controller(Predicates.blocks(definition.getBlock())))
                    .where("Y", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(
                        ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_stainless_evaporation")))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                    .setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setExactLimit(1))
                            .or(maint))
                    .where("Z", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(
                        ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_stainless_evaporation")))
                            .or(exportPredicate)
                            .or(maint))
                    .where('X', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(
                        ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_stainless_evaporation")))
                            .or(exportPredicate))
                    .where('#', Predicates.air())
                    .build();
            })
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfos = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                    .where('C', definition, Direction.NORTH)
                    .where('S', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_stainless_evaporation")))
                    .where('X', GTMachines.ITEM_EXPORT_BUS[GTValues.HV], Direction.NORTH)
                    .where('I', GTMachines.FLUID_IMPORT_HATCH[GTValues.HV], Direction.NORTH)
                    .where('E', GTMachines.ENERGY_INPUT_HATCH[GTValues.HV], Direction.SOUTH)
                    .where('M', GTMachines.MAINTENANCE_HATCH, Direction.SOUTH)
                    .where('#', Blocks.AIR.defaultBlockState())
                    .where('F', GTMachines.FLUID_EXPORT_HATCH[GTValues.HV], Direction.SOUTH);
                List<String> front = new ArrayList<>(15);
                front.add("XCI");
                front.add("SSS");
                List<String> middle = new ArrayList<>(15);
                middle.add("SSS");
                middle.add("SSS");
                List<String> back = new ArrayList<>(15);
                back.add("MES");
                back.add("FSS");
                for (int i = 1; i <= 11; ++i) {
                    front.add("SSS");
                    middle.add(1, "S#S");
                    back.add("SFS");
                    var copy = builder.shallowCopy()
                        .aisle(front.toArray(String[]::new))
                        .aisle(middle.toArray(String[]::new))
                        .aisle(back.toArray(String[]::new));
                    shapeInfos.add(copy.build());
                }
                return shapeInfos;
            })
            .allowExtendedFacing(false)
            .partSorter(Comparator.comparingInt(p -> p.self().getPos().getY()))
            .register();

    // spotless:on
}
