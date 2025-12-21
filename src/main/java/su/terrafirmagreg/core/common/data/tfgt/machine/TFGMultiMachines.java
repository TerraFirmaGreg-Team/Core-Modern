package su.terrafirmagreg.core.common.data.tfgt.machine;

import static com.gregtechceu.gtceu.api.GTValues.EV;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.PARALLEL_HATCH;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Vector3f;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.DistillationTowerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.gcym.LargeMixerMachine;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import fi.dea.mc.deafission.common.data.FissionMachines;
import fi.dea.mc.deafission.common.data.FisssionGtPartAbilities;
import fi.dea.mc.deafission.common.data.machine.AuxExchangerMachine;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.TFGTags;
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
            .appearanceBlock(GTBlocks.STEEL_HULL)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                GTCEu.id("block/casings/steam/steel/side"),
                GTCEu.id("block/multiblock/implosion_compressor"))
                   .andThen(b -> b.addDynamicRenderer(() -> DynamicRenderHelper.makeGrowingPlantRender(List.of(
                       new Vector3f(-2, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(0, 1, -1), new Vector3f(1, 1, -1), new Vector3f(2, 1, -1),
                       new Vector3f(-2, 1, -2), new Vector3f(-1, 1, -2), new Vector3f(0, 1, -2), new Vector3f(1, 1, -2), new Vector3f(2, 1, -2),
                       new Vector3f(-2, 1, -3), new Vector3f(-1, 1, -3), new Vector3f(0, 1, -3), new Vector3f(1, 1, -3), new Vector3f(2, 1, -3),
                       new Vector3f(-2, 1, -4), new Vector3f(-1, 1, -4), new Vector3f(0, 1, -4), new Vector3f(1, 1, -4), new Vector3f(2, 1, -4),
                       new Vector3f(-2, 1, -5), new Vector3f(-1, 1, -5), new Vector3f(0, 1, -5), new Vector3f(1, 1, -5), new Vector3f(2, 1, -5)
                   )))))
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
                                    .getValue(ResourceLocation.fromNamespaceAndPath("ae2", "quartz_glass"))))
                            .or(Predicates.blocks(ForgeRegistries.BLOCKS
                                    .getValue(ResourceLocation.fromNamespaceAndPath("ae2", "quartz_vibrant_glass")))))
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
                        .aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "#XXFXX#")
                        .aisle("CDDDDDC", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "F#####F", "FFFFFFF")
                        .aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "#XXFXX#")
                        .aisle("CDDDDDC", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "X#####X", "#XXFXX#")
                        .aisle("mitYfee", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "XXXFXXX", "###F###")
                        .where('Y', definition, Direction.SOUTH)
                        .where('C', GTBlocks.STEEL_HULL.getDefaultState())
                        .where('D', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfc", "dirt/loam")))
                        .where('F', ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel))
                        .where('X', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("create", "framed_glass")))
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

    private static final Supplier<Block> BIOCULTURE_CASING = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture"));
    public static final MultiblockMachineDefinition BIOREACTOR = REGISTRATE
            .multiblock("bioreactor", BioreactorMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.BIOREACTOR_RECIPES)
            .appearanceBlock(BIOCULTURE_CASING)
            .workableCasingModel(TFGCore.id("block/casings/machine_casing_bioculture"),
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
            .multiblock("nuclear_turbine", (holder) -> new NuclearLargeTurbineMachine(holder, EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.NUCLEAR_TURBINE)
            .recipeModifier(NuclearLargeTurbineMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_STEEL_TURBINE)
            .workableCasingModel(GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"), GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("A   A", "A   A", "CCCCC", "CDCDC", "CDCDC", "CCCCC", "BBBBB", "     ", "     ", "     ", "     ")
                    .aisle(" *** ", " *** ", "CCCCC", "DEFED", "DEFED", "CAAAC", "BAAAB", " AAA ", "  A  ", "  A  ", "  A  ")
                    .aisle(" *** ", " *** ", "CCGCC", "CFHFC", "CFHFC", "CAFAC", "BAFAB", " A*A ", " A*A ", " A*A ", " A*A ")
                    .aisle(" *** ", " *** ", "CCCCC", "DEFED", "DEFED", "CAAAC", "BAAAB", " AAA ", "  A  ", "  A  ", "  A  ")
                    .aisle("A   A", "A   A", "CCCCC", "CDYDC", "CDCDC", "CCCCC", "BBBBB", "     ", "     ", "     ", "     ")
                    .where("*", Predicates.air())
                    .where(" ", Predicates.any())
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
                    .where("G", Predicates.blocks(PartAbility.ROTOR_HOLDER.getBlockRange(EV, GTValues.UHV).toArray(Block[]::new)))
                    .where("H", Predicates.blocks(GTBlocks.CASING_TITANIUM_GEARBOX.get()))
                    .build())
            .register();

    private static final Supplier<Block> EVAPORATION_CASING = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_stainless_evaporation"));
    public static final MultiblockMachineDefinition EVAPORATION_TOWER = REGISTRATE
            .multiblock("evaporation_tower", DistillationTowerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.EVAPORATION_TOWER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(EVAPORATION_CASING)
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

    private static final Supplier<Block> tower_casing = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_ostrum_carbon"));
    private static final Supplier<Block> titanium_concrete = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "polished_titanium_concrete"));
    private static final Supplier<Block> heat_pipe = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/heat_pipe_casing"));
    private static final Supplier<Block> steel_catwalk = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("createdeco", "industrial_iron_catwalk"));
    private static final Supplier<Block> titanium_exhaust = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "titanium_exhaust_vent"));


    public static final MultiblockMachineDefinition COOLING_TOWER = REGISTRATE
            .multiblock("cooling_tower", (holder) -> new LargeMixerMachine(holder, EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.COOLING_TOWER)
            .appearanceBlock(tower_casing)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("********A  A  A********", "********A  A  A********", "********BBBBBBB********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .aisle("******A         A******", "******A         A******", "******BBEEEEEEEBB******", "******DDD     DDD******", "*******DDDD DDDD*******", "********DDDDDDD********", "**********DDD**********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .aisle("****A             A****", "****A             A****", "****BBEEEEEEEEEEEBB****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "*******DDD   DDD*******", "********DDDDDDD********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .aisle("***                 ***", "***                 ***", "***BEEEEE     EEEEEB***", "***DD    EEEEE    DD***", "****D     F F     D****", "*****D    G G    D*****", "*****DD         DD*****", "******DD       DD******", "*******DD     DD*******", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "**********EEE**********")
                    .aisle("**A                 A**", "**A                 A**", "**BEEEE         EEEEB**", "***D   EEEEEEEEE   D***", "***D    F     F    D***", "****D   G G G G   D****", "****D             D****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "******DD       DD******", "*******DD     DD*******", "********DDDDDDD********", "********DDDDDDD********", "*********DDDDD*********", "**********DDD**********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********D***********", "**********DDD**********", "*********DDDDD*********", "********EEEEEEE********")
                    .aisle("**                   **", "**                   **", "**BEEE           EEEB**", "**D   EEEEEEEEEEE   D**", "**D   F    F        D**", "***D  G G GFG G G  D***", "***D       F       D***", "****D      F      D****", "****D      G      D****", "*****D           D*****", "*****D           D*****", "******D    H    D******", "******DD       DD******", "*******D       D*******", "*******DD     DD*******", "********DD   DD********", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "*********DDDDD*********", "*********EEEEE*********", "*********DDDDD*********", "*********DDDDD*********", "********DDDDDDD********", "********DDD DDD********", "********DD   DD********", "*******DDD   DDD*******", "******EEEE   EEEE******")
                    .aisle("*A                   A*", "*A                   A*", "*BEEE             EEEB*", "*D  FEEEEEEEEEEEEE   D*", "**D F  F       F    D**", "**D G GFG G G GFG G D**", "***D   F       F   D***", "***D   F       F   D***", "****D  GGGGGGGGG  D****", "****D             D****", "****D             D****", "*****D   H H H   D*****", "*****D     I     D*****", "******D    I    D******", "******D    I    D******", "*******DJJJIJJJD*******", "*******D   I   D*******", "*******D   I   D*******", "*******DD  I  DD*******", "********DD I DD********", "********EEEEEEE********", "********DD   DD********", "*******DD     DD*******", "*******D       D*******", "*******D       D*******", "*******D       D*******", "******DD       DD******", "*****EEE       EEE*****")
                    .aisle("*         DDD         *", "*         DDD         *", "*BEE      DDD      EEB*", "*D  EEEEEEDDDEEEEEE  D*", "*D        KKK        D*", "**D G G G G G G G G D**", "**D                 D**", "***D               D***", "***D       G       D***", "****D             D****", "****D             D****", "****D    H H H    D****", "*****D   I   I   D*****", "*****D   I   I   D*****", "*****D   I   I   D*****", "******DJJIJJJIJJD******", "******D  I   I  D******", "******DD I   I DD******", "******DD I   I DD******", "*******DDI   IDD*******", "*******EEE   EEE*******", "*******DD     DD*******", "******DD       DD******", "******DD       DD******", "******D         D******", "******D         D******", "*****DD         DD*****", "*****EE         EE*****")
                    .aisle("A       DDBBBDD       A", "A       DDKKKDD       A", "BEEE    DDKKKDD    EEEB", "*D  EEEEDDKKKDDEEEE  D*", "*D   F  KKKKKKK  F   D*", "*D  GFG G G G G GFG  D*", "**D  F           F  D**", "**D  F           F  D**", "***D GGGGGGGGGGGGG D***", "***D               D***", "***D               D***", "****D  H H H H H  D****", "****D             D****", "****D             D****", "*****D           D*****", "*****DJJJJJJJJJJJD*****", "*****D           D*****", "*****D           D*****", "******D         D******", "******DD       DD******", "******EEE     EEE******", "******DD       DD******", "******D         D******", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****EE           EE****")
                    .aisle("        DBBBBBD        ", "        DKKKKKD        ", "BEE     DK   KD     EEB", "D  EEEEEDK   KDEEEEE  D", "*D      KKKKKKK      D*", "*D  G G G GFG G G G  D*", "**D        F        D**", "**D        F        D**", "**D        G        D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D  I       I  D****", "****D  I       I  D****", "****D  I       I  D****", "*****DJIJJJJJJJIJD*****", "*****D I       I D*****", "*****D I       I D*****", "*****D I       I D*****", "*****DDI       IDD*****", "*****EEE       EEE*****", "*****DD         DD*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****DD           DD****", "****EE           EE****")
                    .aisle("       DBBBBBBBD       ", "       DKKKKKKKD       ", "BEE    DK     KD    EEB", "D  EEEEDK     KDEEEE  D", "*D   F KKKKKKKKK F   D*", "*D  GFG G G G G GFG  D*", "*D   F           F   D*", "**D  F           F  D**", "**D  GGGGGGGGGGGGG  D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D             D****", "****D             D****", "****D             D****", "****D JJJJJJJJJJJ D****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****EE         EE*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****D             D****", "****D             D****", "***EE             EE***")
                    .aisle("A      DBBBBBBBD      A", "A      DKKKKKKKD      A", "BEE    DK  H  KD    EEB", "D  EEEEDK  H  KDEEEE  D", "D  F   KKKKHKKKK   F  D", "*D GGGGGGGGGGGGGGGGG D*", "*D         G         D*", "**D        G        D**", "**D        G        D**", "**D                 D**", "***D               D***", "***D   H H L H H   D***", "***D              D****", "****D             D****", "****D             D****", "****DJJJJJJJJJJJJJD****", "****D             D****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****EE         EE*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****D             D****", "****D             D****", "****D             D****", "***EE             EE***")
                    .aisle("       DBBBBBBBD       ", "       DKKKKKKKD       ", "BEE    DK     KD    EEB", "D  EEEEDK     KDEEEE  D", "*D   F KKKKKKKKK F   D*", "*D  GFG G G G G GFG  D*", "*D   F           F   D*", "**D  F           F  D**", "**D  GGGGGGGGGGGGG  D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D             D****", "****D             D****", "****D             D****", "****D JJJJJJJJJJJ D****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****EE         EE*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****D             D****", "****D             D****", "***EE             EE***")
                    .aisle("        DBBBBBD        ", "        DKKKKKD        ", "BEE     DK   KD     EEB", "D  EEEEEDK   KDEEEEE  D", "*D      KKKKKKK      D*", "*D  G G G GFG G G G  D*", "**D        F        D**", "**D        F        D**", "**D        G        D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D  I       I  D****", "****D  I       I  D****", "****D  I       I  D****", "*****DJIJJJJJJJIJD*****", "*****D I       I D*****", "*****D I       I D*****", "*****D I       I D*****", "*****DDI       IDD*****", "*****EEE       EEE*****", "*****DD         DD*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****DD           DD****", "****EE           EE****")
                    .aisle("A       DDBBBDD       A", "A       DDKKKDD       A", "BEEE    DDKKKDD    EEEB", "*D  EEEEDDKKKDDEEEE  D*", "*D   F  KKKKKKK  F   D*", "*D  GFG G G G G GFG  D*", "**D  F           F  D**", "**D  F           F  D**", "***D GGGGGGGGGGGGG D***", "***D               D***", "***D               D***", "****D  H H H H H  D****", "****D             D****", "****D             D****", "*****D           D*****", "*****DJJJJJJJJJJJD*****", "*****D           D*****", "*****D           D*****", "******D         D******", "******DD       DD******", "******EEE     EEE******", "******DD       DD******", "******D         D******", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****EE           EE****")
                    .aisle("*         DDD         *", "*         DDD         *", "*BEE      DDD      EEB*", "*D  EEEEEEDDDEEEEEE  D*", "*D        KKK        D*", "**D G G G G G G G G D**", "**D                 D**", "***D               D***", "***D       G       D***", "****D             D****", "****D             D****", "****D    H H H    D****", "*****D   I   I   D*****", "*****D   I   I   D*****", "*****D   I   I   D*****", "******DJJIJJJIJJD******", "******D  I   I  D******", "******DD I   I DD******", "******DD I   I DD******", "*******DDI   IDD*******", "*******EEE   EEE*******", "*******DD     DD*******", "******DD       DD******", "******DD       DD******", "******D         D******", "******D         D******", "*****DD         DD*****", "*****EE         EE*****")
                    .aisle("*A                   A*", "*A                   A*", "*BEEE             EEEB*", "*D  FEEEEEEEEEEEEEF  D*", "**D F  F       F  F D**", "**D G GFG G G GFG G D**", "***D   F       F   D***", "***D   F       F   D***", "****D  GGGGGGGGG  D****", "****D             D****", "****D             D****", "*****D   H H H   D*****", "*****D     I     D*****", "******D    I    D******", "******D    I    D******", "*******DJJJIJJJD*******", "*******D   I   D*******", "*******D   I   D*******", "*******DD  I  DD*******", "********DD I DD********", "********EEEEEEE********", "********DD   DD********", "*******DD     DD*******", "*******D       D*******", "*******D       D*******", "*******D       D*******", "******DD       DD******", "*****EEE       EEE*****")
                    .aisle("**                   **", "**                   **", "**BEEE           EEEB**", "**D   EEEEEEEEEEE   D**", "**D   F    F    F   D**", "***D  G G GFG G G  D***", "***D       F       D***", "****D      F      D****", "****D      G      D****", "*****D           D*****", "*****D           D*****", "******D    H    D******", "******DD       DD******", "*******D       D*******", "*******DD     DD*******", "********DD   DD********", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "*********DDDDD*********", "*********EEEEE*********", "*********DDDDD*********", "*********DDDDD*********", "********DDDDDDD********", "********DDD DDD********", "********DD   DD********", "*******DDD   DDD*******", "******EEEE   EEEE******")
                    .aisle("**A                 A**", "**A                 A**", "**BEEEE         EEEEB**", "***D   EEEEEEEEE   D***", "***D    F     F    D***", "****D   G G G G   D****", "****D             D****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "******DD       DD******", "*******DD     DD*******", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "**********DDD**********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********D***********", "**********DDD**********", "*********DDDDD*********", "********EEEEEEE********")
                    .aisle("***                 ***", "***                 ***", "***BEEEEE     EEEEEB***", "***DD    EEEEE    DD***", "****D     F F     D****", "*****D    G G    D*****", "*****DD         DD*****", "******DD       DD******", "*******DD     DD*******", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "**********EEE**********")
                    .aisle("****A             A****", "****A             A****", "****BBEEEEEEEEEEEBB****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "*******DDD   DDD*******", "********DDDDDDD********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .aisle("******A         A******", "******A         A******", "******BBEEEEEEEBB******", "******DDD     DDD******", "*******DDDD DDDD*******", "********DDDDDDD********", "**********DDD**********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .aisle("********A  A  A********", "********A  A  A********", "********BBBCBBB********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .where("*", Predicates.any())
                    .where(" ", Predicates.air())
                    .where("A", Predicates.frames(GTMaterials.TungstenSteel))
                    .where("B", Predicates.blocks(tower_casing.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where("C", Predicates.controller(Predicates.blocks(definition.getBlock())))
                    .where("D", Predicates.blocks(titanium_concrete.get())
                            .or(Predicates.blockTag(TFGTags.Blocks.TitaniumConcrete)))
                    .where("E", Predicates.blocks(tower_casing.get()))
                    .where("F", Predicates.frames(GTMaterials.WatertightSteel))
                    .where("G", Predicates.blocks(heat_pipe.get()))
                    .where("H", Predicates.blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                    .where("I", Predicates.frames(GTMaterials.StainlessSteel))
                    .where("J", Predicates.blocks(steel_catwalk.get()))
                    .where("K", Predicates.blocks(GCYMBlocks.CASING_CORROSION_PROOF.get()))
                    .where("L", Predicates.blocks(titanium_exhaust.get()))
                    .build())
            .register();

    public static final MultiblockMachineDefinition GROWTH_CHAMBER = REGISTRATE
            .multiblock("growth_chamber", GrowthChamberMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.GROWTH_CHAMBER_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH)
            .appearanceBlock(BIOCULTURE_CASING)
            .tooltips(Component.translatable("tfg.tooltip.machine.parallel"),
                    Component.translatable("tfg.tooltip.growth_chamber"))
            .workableCasingModel(TFGCore.id("block/casings/machine_casing_bioculture"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(definition -> FactoryBlockPattern
                    .start(RelativeDirection.LEFT, RelativeDirection.FRONT, RelativeDirection.DOWN)
                    .aisle("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "             ANA             ", "             NBN             ", "             AAA             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ").setRepeatable(1, 5)
                    .aisle("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "             HLH             ", "             HHH             ", "           HHAAAHH           ", "           LHAAAHL           ", "           HHAAAHH           ", "             HHH             ", "             HLH             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "              K              ", "                             ", "             AAA             ", "           K AAA K           ", "             AAA             ", "                             ", "              K              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "              K              ", "                             ", "             MMM             ", "           K MAM K           ", "             MMM             ", "                             ", "              K              ", "                             ", "                             ", "                             ", "                             ", "              O              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "              K              ", "                             ", "             AAA             ", "           K AAA K           ", "             AAA             ", "                             ", "              K              ", "                             ", "                             ", "                             ", "                             ", "              A              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .aisle("          AAAAAAAAA          ", "       AAAACCCCCCCAAAA       ", "      AACCCFDFDFDFCCCAA      ", "    AAACDFDFDFDFDFDFDCAAA    ", "   AACCDDFDFDFDFDFDFDDCCAA   ", "   ACCDFDFDCCCCCCCDFDFDCCA   ", "  AAFDCDFCCAAAAAAACCFDCDCAA  ", " AACDFDCCAAAIIJIIAAACCDFDCAA ", " ACDDDFCAAIIIIJIIIIAACFDDDCA ", " ACFFFCAAIIIIIJIIIIIAACFFFCA ", "AACDDDCAIIIIIIJIIIIIIACDDDCAA", "ACFFFCAAIIIIJJKJJIIIIAACFFFCA", "ACDDDCAIIIIJ  A  JIIIIACDDDCA", "ACFFFCAIIIIJ AAA JIIIIACFFFCA", "ACDDDCAJJJJKAAAAAKJJJJACDDDCA", "ACFFFCAIIIIJ AAA JIIIIACFFFCA", "ACDDDCAIIIIJ  A  JIIIIACDDDCA", "ACFFFCAAIIIIJJKJJIIIIAACFFFCA", "AACDDDCAIIIIIIJIIIIIIACDDDCAA", " ACFFFCAAIIIIIJIIIIIAACFFFCA ", " ACDDDFCAAIIIIJIIIIAACFDDDCA ", " AACDFDCCAAAIIJIIAAACCDFDCAA ", "  AACDCDFCCAAAAAAACCFDCDCAA  ", "   ACCDFDFDCCCCCCCDFDFDCCA   ", "   AACCDDFDFDFDFDFDFDDCCAA   ", "    AAACDFDFDFDFDFDFDCAAA    ", "      AACCCFDFDFDFCCCAA      ", "       AAAACCCCCCCAAAA       ", "          AAAAAAAAA          ")
                    .aisle("                             ", "           DDDDDDD           ", "        DDD       DDD        ", "       D             D       ", "     DD               DD     ", "    DC     CCCCCCC     CD    ", "    D C  CC       CC  C D    ", "   D   CC           CC   D   ", "  D    C             C    D  ", "  D   C               C   D  ", "  D   C               C   D  ", " D   C        K        C   D ", " D   C                 C   D ", " D   C                 C   D ", " D   C     K     K     C   D ", " D   C                 C   D ", " D   C                 C   D ", " D   C        K        C   D ", "  D   C               C   D  ", "  D   C               C   D  ", "  D    C             C    D  ", "   D   CC           CC   D   ", "    D C  CC       CC  C D    ", "    DC     CCCCCCC     CD    ", "     DD               DD     ", "       D             D       ", "        DDD       DDD        ", "           DDDDDDD           ", "                             ")
                    .aisle("                             ", "           DDDDDDD           ", "        DDDE E E EDDD        ", "       D E E E E E E D       ", "     DD  E E E E E E  DD     ", "    DC E E CCCCCCC E E CD    ", "    D C ECC       CCE C D    ", "   D E CC           CC E D   ", "  D   EC             CE   D  ", "  DEEEC               CEEED  ", "  D   C               C   D  ", " DEEEC        K        CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", " D   C     K     K     C   D ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC        K        CEEED ", "  D   C               C   D  ", "  DEEEC               CEEED  ", "  D   EC             CE   D  ", "   D E CC           CC E D   ", "    D C ECC       CCE C D    ", "    DC E E CCCCCCC E E CD    ", "     DD  E E E E E E  DD     ", "       D E E E E E E D       ", "        DDDE E E EDDD        ", "           DDDDDDD           ", "                             ")
                    .aisle("                             ", "           DDDDDDD           ", "        DDDE E E EDDD        ", "       D E E E E E E D       ", "     DD  E E E E E E  DD     ", "    DC E E CGCCCGC E E CD    ", "    D C ECC   H   CCE C D    ", "   D E CC     H     CC E D   ", "  D   EC      H      CE   D  ", "  DEEEC       H       CEEED  ", "  D   C       H       C   D  ", " DEEEC        H        CEEED ", " D   G                 G   D ", " DEEEC                 CEEED ", " D   CHHHHHH     HHHHHHC   D ", " DEEEC                 CEEED ", " D   G                 G   D ", " DEEEC        H        CEEED ", "  D   C       H       C   D  ", "  DEEEC       H       CEEED  ", "  D   EC      H      CE   D  ", "   D E CC     H     CC E D   ", "    D C ECC   H   CCE C D    ", "    DC E E CGCCCGC E E CD    ", "     DD  E E E E E E  DD     ", "       D E E E E E E D       ", "        DDDE E E EDDD        ", "           DDDDDDD           ", "                             ")
                    .aisle("                             ", "           DDDDDDD           ", "        DDDE E E EDDD        ", "       D E E E E E E D       ", "     DD  E E E E E E  DD     ", "    DC E E CCCCCCC E E CD    ", "    D C ECC       CCE C D    ", "   D E CC           CC E D   ", "  D   EC             CE   D  ", "  DEEEC               CEEED  ", "  D   C               C   D  ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", "  D   C               C   D  ", "  DEEEC               CEEED  ", "  D   EC             CE   D  ", "   D E CC           CC E D   ", "    D C ECC       CCE C D    ", "    DC E E CCCCCCC E E CD    ", "     DD  E E E E E E  DD     ", "       D E E E E E E D       ", "        DDDE E E EDDD        ", "           DDDDDDD           ", "                             ")
                    .aisle("           AAAAAAA           ", "        AAACCCCCCCAAA        ", "       ACCCAAAAAAACCCA       ", "     AACAAAAAAAAAAAAACAA     ", "    ACCAAAAAAAAAAAAAAACCA    ", "   ACAAAAAACCCCCCCAAAAAACA   ", "   ACAAAACC       CCAAAACA   ", "  ACAAAAC           CAAAACA  ", " ACAAAAC             CAAAACA ", " ACAAAC               CAAACA ", " ACAAAC               CAAACA ", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", " ACAAAC               CAAACA ", " ACAAAC               CAAACA ", " ACAAAAC             CAAAACA ", "  ACAAAAC           CAAAACA  ", "   ACAAAACC       CCAAAACA   ", "   ACAAAAAACCCCCCCAAAAAACA   ", "    ACCAAAAAAAAAAAAAAACCA    ", "     AACAAAAAAAAAAAAACAA     ", "       ACCCAAAAAAACCCA       ", "        AAACCCCCCCAAA        ", "           AAAAAAA           ")
                    .where("C", Predicates.blocks(GTBlocks.PLASTCRETE.get()))
                    .where("E", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "sample_rack"))))
                    .where("G", Predicates.blocks(GTBlocks.FILTER_CASING.get()))
                    .where("I", Predicates.blocks(GTBlocks.CLEANROOM_GLASS.get()))
                    .where("J", Predicates.frames(GTMaterials.HastelloyC276))
                    .where("K", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_sterilizing_pipes"))))
                    .where("L", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "growth_monitor"))))
                    .where("M", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture")))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1))
                            .or(abilities(PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where("N", Predicates.blocks(TFGMachines.SINGLE_ITEMSTACK_BUS.get()))
                    .where(" ", Predicates.any())
                    .where("H", Predicates.blocks(GTBlocks.CASING_PTFE_INERT.get()))
                    .where("A", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture"))))
                    .where("F", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_iron_desh"))))
                    .where("F", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_ultraviolet"))))
                    .where("D", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_bioculture_glass"))))
                    .where("B", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("megacells", "mega_crafting_unit"))))
                    .where("O", Predicates.controller(Predicates.blocks(definition.get())))
                    .build())
            .register();

    private static final Supplier<Block> MARS_CASING = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars"));
    public static final MultiblockMachineDefinition OSTRUM_LINEAR_ACCELERATOR = REGISTRATE
            .multiblock("ostrum_linear_accelerator", AuxExchangerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.OSTRUM_LINEAR_ACCELERATOR)
            .appearanceBlock(MARS_CASING)
            .workableCasingModel(ResourceLocation.fromNamespaceAndPath("tfg", "block/casings/machine_casing_mars"),
                    GTCEu.id("block/machines/thermal_centrifuge"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "         ", "         ")
                    .aisle("BAAAAAAAA", "B#######D", "BBBBBBBAA", " BCCCB   ", " BBBBB   ")
                    .aisle("AAAAAAAAA", "K#######D", "BB###BBAA", " C###C   ", " BHHHB   ")
                    .aisle("BEBEBEAAA", "BEBEBEA#D", "BBBBBBBAA", " BCCCB   ", " BBBBB   ")
                    .aisle("A     AFA", "A     AXA", "AAAAAAAFA", "         ", "         ")
                    .where('X', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('A', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars")))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('B', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_ostrum_carbon"))))
                    .where('C', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_vacuum_engine_intake"))))
                    .where('D', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("gtceu", "heat_vent"))))
                    .where('E', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars")))
                            .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(6))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(6)))
                    .where('F', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars")))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('H', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars")))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS)))
                    .where('K', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars")))
                        .or(Predicates.abilities(FisssionGtPartAbilities.USE_HEAT)))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .build())
            .shapeInfos(definition -> {
                List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                var builder = MultiblockShapeInfo.builder()
                    .aisle("KKAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "         ", "         " )
                    .aisle("BAAAAAAAA", "B       D", "BBBBBBBAA", " BCCCB   ", " BBBBB   " )
                    .aisle("AAAAAAAAA", "Z       D", "BB   BBAA", " C   C   ", " BIAHB   " )
                    .aisle("BEBEBEAAA", "BEBFBEA#D", "BBBBBBBAA", " BCCCB   ", " BBBBB   " )
                    .aisle("A     AMA", "A     AXA", "AAAAAAAAA", "         ", "         " )
                        .where('X', definition, Direction.SOUTH)
                        .where('A', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_mars")))
                        .where('B', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_ostrum_carbon")))
                        .where('C', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_vacuum_engine_intake")))
                        .where('D', ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("gtceu", "heat_vent")))
                        .where('E', GTMachines.FLUID_IMPORT_HATCH[EV], Direction.SOUTH)
                        .where('F', GTMachines.ITEM_IMPORT_BUS[EV], Direction.SOUTH)
                        .where('H', GTMachines.ITEM_EXPORT_BUS[EV], Direction.UP)
                        .where('I', GTMachines.FLUID_EXPORT_HATCH[EV], Direction.UP)
                        .where('M', GTMachines.AUTO_MAINTENANCE_HATCH, Direction.SOUTH)
                        .where('K', GTMachines.ENERGY_INPUT_HATCH[GTValues.HV], Direction.NORTH)
                        .where('Z', FissionMachines.HeatInputHatchEv, Direction.WEST)
                        .where(' ', Blocks.AIR);
                shapeInfo.add(builder.build());
                return shapeInfo;
            })
            .register();

    private static final Supplier<Block> OSTRUM_CASING = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_ostrum_carbon"));
    private static final Supplier<Block> DESH_PTFE = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_desh_ptfe"));
    public static final MultiblockMachineDefinition SMR_GENERATOR = REGISTRATE
            .multiblock("smr_generator", (holder) -> new SMRGenerator2(holder, GTValues.EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.SMR_GENERATOR)
            .recipeModifier(SMRGenerator2::recipeModifier, true)
            .appearanceBlock(DESH_PTFE)
            .workableCasingModel(ResourceLocation.fromNamespaceAndPath("tfg", "block/casings/machine_casing_ostrum_carbon"), GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("AAA", "ABA", "ABA", "AAA")
                    .aisle("AEA", "BDB", "BDB", "AEA")
                    .aisle("AAA", "AXA", "ABA", "AAA")
                    .where('X', Predicates.controller(Predicates.blocks(definition.get())))
                    .where("A", Predicates.blocks(OSTRUM_CASING.get()))
                    .where("B", Predicates.blocks(DESH_PTFE.get()).setMinGlobalLimited(1)
                            .or(Predicates.abilities((PartAbility.IMPORT_FLUIDS)))
                            .or(Predicates.abilities((PartAbility.EXPORT_FLUIDS)))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1).setPreviewCount(1)))
                    .where("D", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/heat_pipe_casing"))))
                    .where("E", Predicates.blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                    .build())
            .register();


    public static final MultiblockMachineDefinition ACTIVE_POWER_TRANSFORMER = REGISTRATE
            .multiblock("active_power_transformer", ActiveTransformerMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(TFGBlocks.MACHINE_CASING_POWER_CASING)
            .tooltips(Component.translatable("gtceu.machine.active_transformer.tooltip.0"),
                    Component.translatable("gtceu.machine.active_transformer.tooltip.1"))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.active_transformer.tooltip.2")
                            .append(Component.translatable("gtceu.machine.active_transformer.tooltip.3")
                                    .withStyle(TooltipHelper.RAINBOW_HSL_SLOW))))
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "XCX", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(TFGBlocks.MACHINE_CASING_POWER_CASING.get()).setMinGlobalLimited(12)
                            .or(ActiveTransformerMachine.getHatchPredicates()))
                    .where('C', blocks(TFGBlocks.SUPERCONDUCTOR_COIL_LARGE_BLOCK.get()))
                    .build())
            .workableCasingModel(TFGCore.id("block/casings/machine_casing_power_casing"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    private static final Supplier<Block> HORTICULTURE_CASING = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("tfg", "casings/machine_casing_egh"));
    public static final MultiblockMachineDefinition HYDROPONICS_FACILITY = REGISTRATE
            .multiblock("hydroponics_facility", GreenhouseMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGRecipeTypes.HYDROPONICS_FACILITY_RECIPES)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH)
            .appearanceBlock(HORTICULTURE_CASING)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
            ResourceLocation.fromNamespaceAndPath("tfg", "block/casings/machine_casing_egh"),
                GTCEu.id("block/machines/implosion_compressor"))
                    .andThen(b -> b.addDynamicRenderer(() -> DynamicRenderHelper.makeGrowingPlantRender(List.of(
                        // Layer 1
                        new Vector3f(-1, 0, -5), new Vector3f(-1,0,-6),new Vector3f(-1,0,-7),new Vector3f(-1,0,-8),new Vector3f(-1,0,-9),new Vector3f(-1,0,-10),
                        new Vector3f(1, 0, -5), new Vector3f(1,0,-6),new Vector3f(1,0,-7),new Vector3f(1,0,-8),new Vector3f(1,0,-9),new Vector3f(1,0,-10),
                        // Layer 2
                        new Vector3f(-1, 3, -5), new Vector3f(-1,3,-6),new Vector3f(-1,3,-7),new Vector3f(-1,3,-8),new Vector3f(-1,3,-9),new Vector3f(-1,3,-10),
                        new Vector3f(1, 3, -5), new Vector3f(1,3,-6),new Vector3f(1,3,-7),new Vector3f(1,3,-8),new Vector3f(1,3,-9),new Vector3f(1,3,-10),
                        // Layer 3
                        new Vector3f(-1, 6, -5), new Vector3f(-1,6,-6),new Vector3f(-1,6,-7),new Vector3f(-1,6,-8),new Vector3f(-1,6,-9),new Vector3f(-1,6,-10),
                        new Vector3f(1, 6, -5), new Vector3f(1,6,-6),new Vector3f(1,6,-7),new Vector3f(1,6,-8),new Vector3f(1,6,-9),new Vector3f(1,6,-10)
                    )))))
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("AGGGA", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", " BBB ")
                    .aisle("AHIHA", "B A B", "B A B", "BHIHB", "B A B", "B A B", "BHIHB", "B A B", "B A B", " BBB ").setRepeatable(2)
                    .aisle("EHIHE", "B A B", "B A B", "BHIHB", "B A B", "B A B", "BHIHB", "B A B", "B A B", " BBB ").setRepeatable(2)
                    .aisle("AHIHA", "B A B", "B A B", "BHIHB", "B A B", "B A B", "BHIHB", "B A B", "B A B", " BBB ").setRepeatable(2)
                    .aisle("AAAAA", "B A B", "B A B", "B A B", "B A B", "B A B", "B A B", "B A B", "B A B", " BBB ").setRepeatable(2)
                    .aisle(" AAA ", " B B ", " B B ", " B B ", " B B ", " B B ", " BFB ", " B B ", " B B ", " BBB ")
                    .aisle(" EEE ", " B B ", " B B ", " B B ", " B B ", " B B ", " B B ", " B B ", " B B ", " BBB ")
                    .aisle(" AAA ", " BCB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ")
                    .where(" ", Predicates.any())
                    .where("A", Predicates.blocks(HORTICULTURE_CASING.get()))
                    .where("B", Predicates.blocks(GTBlocks.CLEANROOM_GLASS.get()))
                    .where("C", controller(blocks(definition.getBlock())))
                    .where("E", Predicates.blocks(GTBlocks.FILTER_CASING.get()))
                    .where("F", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "cultivation_monitor"))))
                    .where("G", Predicates.blocks(HORTICULTURE_CASING.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)))
                    .where("H", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("tfg", "egh_planter"))))
                    .where("I", Predicates.blocks(GTBlocks.PLASTCRETE.get()))
                    .build())
            .register();

    // spotless:on
}
