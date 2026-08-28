package su.terrafirmagreg.core.common.data.tfgt;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK;
import static com.gregtechceu.gtceu.common.data.GTBlocks.ALL_FIREBOXES;
import static su.terrafirmagreg.core.TFGCore.REGISTRATE;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.joml.Vector3f;

import com.eerussianguy.firmalife.common.FLTags;
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.blocks.greenhouse.Greenhouse;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderHelper;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.DistillationTowerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.simibubi.create.AllBlocks;

import net.dries007.tfc.common.TFCTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import earth.terrarium.adastra.common.registry.ModBlocks;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Casings;
import su.terrafirmagreg.core.common.tfgt.interdim_logistics.machine.InterplanetaryItemLauncherMachine;
import su.terrafirmagreg.core.common.tfgt.interdim_logistics.machine.InterplanetaryItemReceiverMachine;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.electric.*;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam.GasWellMachine;
import su.terrafirmagreg.core.common.tfgt.machine.multiblock.steam.TFGLargeBoilerMachine;
import su.terrafirmagreg.core.common.tfgt.machine.render.BouleRender;
import su.terrafirmagreg.core.common.tfgt.machine.trait.GasWellRecipeLogic;
import su.terrafirmagreg.core.common.tfgt.recipe.modifier.AnimalProductModifier;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public class TFGMultiMachines {

    public static void init() {
    }

    private static net.minecraft.world.level.block.state.BlockState orientedBlockState(String namespace, String path, Direction dir) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (block == null)
            return Blocks.AIR.defaultBlockState();
        net.minecraft.world.level.block.state.BlockState state = block.defaultBlockState();
        if (dir.getAxis().isHorizontal() && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, dir);
        }
        return state;
    }

    // spotless:off
    public static final MultiblockMachineDefinition INTERPLANETARY_ITEM_LAUNCHER = REGISTRATE
            .multiblock("interplanetary_item_launcher", InterplanetaryItemLauncherMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(GTBlocks.CASING_STAINLESS_CLEAN)
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel"),
                    TFGCore.id("block/machines/interplanetary_item_launcher"))
            .pattern(definition -> {
                MetaMachineBlock[] inputBuses = Arrays.stream(TFGMachines.RAILGUN_ITEM_LOADER_IN)
                        .map(MachineDefinition::get).toArray(MetaMachineBlock[]::new);
                return MultiblockPatternBuilder.start(RelativeDirection.RIGHT, RelativeDirection.FRONT, RelativeDirection.UP)
                        .slice("F###F", "#SSS#", "#SSS#", "#ESE#", "F###F")
                        .slice("FsssF", "sSCSs", "sCCCs", "sSCSs", "FsysF")
                        .slice("F###F", "#LCL#", "#R R#", "#LCL#", "F###F")
                        .slice("FFFFF", "FLCLF", "FR RF", "FLCLF", "FFFFF")
                        .sliceRepeatable(1, 3,"#####", "#L#L#", "#R R#", "#L#L#", "#####")
                        .slice("#####", "#CHC#", "#R R#", "#CHC#", "#####")
                        .sliceRepeatable(1, 3, "#####", "#M#M#", "#R R#", "#M#M#", "#####")
                        .slice("#####", "#CHC#", "#R R#", "#CHC#", "#####")
                        .sliceRepeatable(1, 3, "#####", "#C#C#", "#R R#", "#C#C#", "#####")
                        .where('y', controller(blocks(definition.get())))
                        .where(' ', Predicates.air())
                        .where('#', Predicates.any())
                        .where('F', Predicates.frames(GTMaterials.Aluminium))
                        .where('H', Predicates.frames(GTMaterials.HSLASteel))
                        .where('S', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                        .where('C', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                        .where('E', abilities(PartAbility.INPUT_ENERGY).setExactLimit(2))
                        .where('s', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get())
                                .or(blocks(inputBuses).setMinGlobalLimited(1))
                                .or(blocks(TFGMachines.RAILGUN_AMMO_LOADER.get()).setExactLimit(1)))
                        .where('L', blocks(TFGBlocks_Casings.SUPERCONDUCTOR_COIL_LARGE_BLOCK.get()))
                        .where('M', blocks(TFGBlocks_Casings.SUPERCONDUCTOR_COIL_SMALL_BLOCK.get()))
                        .where('R', blocks(TFGBlocks_Casings.ELECTROMAGNETIC_ACCELERATOR_BLOCK.get()))
                        .build();
            }).register();

    public static final MultiblockMachineDefinition INTERPLANETARY_ITEM_RECEIVER = REGISTRATE
            .multiblock("interplanetary_item_receiver", InterplanetaryItemReceiverMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL)
            .workableCasingModel(
                    TFGCore.id( "block/casings/machine_casing_aluminium_plated_steel"),
                    TFGCore.id("block/machines/interplanetary_item_receiver"))
            .pattern(def -> {
                MetaMachineBlock[] inputBuses = Arrays.stream(TFGMachines.RAILGUN_ITEM_LOADER_OUT)
                        .map(MachineDefinition::get).toArray(MetaMachineBlock[]::new);
                return MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                        .slice("B     B", "BB   BB", " B   B ", "  CCC  ", "       ")
                        .slice("       ", "B     B", "BBbbbBB", " CEFEC ", "  GGG  ")
                        .slice("       ", "       ", " b   b ", "CF   FC", " G   G ")
                        .slice("       ", "       ", " b   b ", "CE   EC", " G   G ")
                        .slice("       ", "       ", " b   b ", "CF   FC", " G   G ")
                        .slice("       ", "B     B", "BBbDbBB", " CEFEC ", "  GGG  ")
                        .slice("B     B", "BB   BB", " B   B ", "  CCC  ", "       ")
                        .where('B', blocks(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get()))
                        .where('b', blocks(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get())
                                .or(abilities(PartAbility.INPUT_ENERGY)
                                        .or(blocks(inputBuses))))
                        .where('C', Predicates.frames(GTMaterials.Aluminium))
                        .where('D', controller(blocks(def.get())))
                        .where('E', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                        .where('F', blocks(GCYMBlocks.CASING_NONCONDUCTING.get()))
                        .where('G', blocks(GTBlocks.YELLOW_STRIPES_BLOCK_A.get())
                                .or(blocks(GTBlocks.YELLOW_STRIPES_BLOCK_B.get())))
                        .where(' ', Predicates.any())
                        .build();
            })
            .register();

    public static final MultiblockMachineDefinition ELECTRIC_GREENHOUSE = REGISTRATE
            .multiblock("electric_greenhouse", GreenhouseMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.GREENHOUSE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(GTBlocks.STEEL_HULL)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/steam/steel/side"),
                            TFGCore.id("block/machines/electric_greenhouse"))
                    .andThen(b -> b.addDynamicRenderer(() -> DynamicRenderHelper.makeGrowingPlantRender(List.of(
                            new Vector3f(-1f, 1.4f, -1f), new Vector3f(1f, 1.4f, -1f),
                            new Vector3f(-1f, 1.4f, -2f), new Vector3f(1f, 1.4f, -2f),
                            new Vector3f(-1f, 1.4f, -3f), new Vector3f(1f, 1.4f, -3f),
                            new Vector3f(-1f, 1.4f, -4f), new Vector3f(1f, 1.4f, -4f),
                            new Vector3f(-1f, 1.4f, -5f), new Vector3f(1f, 1.4f, -5f)
                    )))))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("AAAAA", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .slice("AFFFA", "BG GB", "B   B", "BH HB", "BBBBB")
                    .slice("AFFFA", "BG GB", "B   B", "BH HB", "BBBBB")
                    .slice("AFFFA", "BG GB", "B   B", "BH HB", "BBBBB")
                    .slice("AFFFA", "BG GB", "B   B", "BH HB", "BBBBB")
                    .slice("AFFFA", "BG GB", "B   B", "BH HB", "BBBBB")
                    .slice("AAIAA", "BBBBB", "BBBBB", "BBBBB", "BBBBB")
                    .where('I', controller(blocks(definition.get())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(GTBlocks.STEEL_HULL.get()).setMinGlobalLimited(10)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('B', Predicates.blockTag(FLTags.Blocks.ALL_IRON_GREENHOUSE)
                            .or(Predicates.blockTag(FLTags.Blocks.STAINLESS_STEEL_GREENHOUSE)))
                    .where('G', blocks(FLBlocks.LARGE_PLANTER.get()))
                    .where('F', Predicates.blockTag(TFCTags.Blocks.BLOOMERY_INSULATION)
                            .or(Predicates.blockTag(TagKey.create(Registries.BLOCK, TFGCore.id( "iron_greenhouse_casings"))))
                            .or(blocks(FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.IRON).get(Greenhouse.BlockType.TRAPDOOR).get()))
                            .or(blocks(FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.RUSTED_IRON).get(Greenhouse.BlockType.TRAPDOOR).get()))
                            .or(blocks(FLBlocks.GREENHOUSE_BLOCKS.get(Greenhouse.STAINLESS_STEEL).get(Greenhouse.BlockType.TRAPDOOR).get())))
                    .where('H', blocks(TFGBlocks_Casings.GROW_LIGHT.get()))
                    .build())
            .register();

    public static final MultiblockMachineDefinition BIOREACTOR = REGISTRATE
            .multiblock("bioreactor", BioreactorMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .recipeType(TFGTRecipeTypes.BIOREACTOR_RECIPES)
            .appearanceBlock(TFGBlocks_Casings.BIOCULTURE_CASING)
            .workableCasingModel(
                    TFGCore.id("block/casings/machine_casing_bioculture"),
                    TFGCore.id("block/machines/bioreactor"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("#A#A#BCB#", "#BBB#DDD#", "#EEE#DDD#", "#EEE#FFF#", "#EEE#EEE#", "#EEE#EEE#", "#EEE#BCB#", "#BBB#####")
                    .slice("AGGGABBBB", "BBBBDHHHD", "E   DHHHD", "E   BBBBF", "E   EI IE", "E   EI IE", "E   BBBBB", "BBBBB####")
                    .slice("#GGGABBBC", "BBBBDHHHD", "E J DHHHD", "E J BBBBF", "E J E K E", "E   E   E", "E   BBBBC", "BBBBB####")
                    .slice("AGGGABBBB", "BBBBDHHHD", "E   DHHHD", "E   BBBBF", "E   EI IE", "E   EI IE", "E   BBBBB", "BBBBB####")
                    .slice("#A#A#BCB#", "#BBB#DDD#", "#EEE#DDD#", "#EEE#FLF#", "#EEE#EEE#", "#EEE#EEE#", "#EEE#BCB#", "#BBB#####")
                    .where(' ', Predicates.air())
                    .where('#', Predicates.any())
                    .where('A', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                    .where('B', blocks(TFGBlocks_Casings.BIOCULTURE_CASING.get()))
                    .where('C', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('D', blocks(TFGBlocks_Casings.ULTRAVIOLET_CASING.get()))
                    .where('E', blocks(TFGBlocks_Casings.BIOCULTURE_GLASS_CASING.get()))
                    .where('F', blocks(TFGBlocks_Casings.BIOCULTURE_CASING.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('G', blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where('H', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('I', blocks(GTBlocks.LAMPS.get(DyeColor.PURPLE).get()))
                    .where('J', blocks(TFGBlocks_Casings.BIOCULTURE_ROTOR_PRIMARY.get()))
                    .where('K', blocks(TFGBlocks_Casings.BIOCULTURE_ROTOR_SECONDARY.get()))
                    .where('L', controller(blocks(definition.get())))
                    .build())
            .register();


    public static final MultiblockMachineDefinition NUCLEAR_TURBINE = REGISTRATE
            .multiblock("nuclear_turbine", (holder) -> new NuclearLargeTurbineMachine(holder, GTValues.EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGTRecipeTypes.NUCLEAR_TURBINE)
            .recipeModifier(NuclearLargeTurbineMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_STEEL_TURBINE)
            .workableCasingModel(
                    GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"),
                    GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("A   A", "A   A", "CCCCC", "CDCDC", "CDCDC", "CCCCC", "BBBBB", "     ", "     ", "     ", "     ")
                    .slice("     ", "     ", "CCCCC", "DEFED", "DEFED", "CAAAC", "BAAAB", " AAA ", "  A  ", "  A  ", "  A  ")
                    .slice("     ", "     ", "CCGCC", "CFHFC", "CFHFC", "CAFAC", "BAFAB", " A A ", " A A ", " A A ", " A A ")
                    .slice("     ", "     ", "CCCCC", "DEFED", "DEFED", "CAAAC", "BAAAB", " AAA ", "  A  ", "  A  ", "  A  ")
                    .slice("A   A", "A   A", "CCCCC", "CDYDC", "CDCDC", "CCCCC", "BBBBB", "     ", "     ", "     ", "     ")
                    .where('*', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('Y', controller(blocks(definition.get())))
                    .where('A', blocks(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get()))
                    .where('B', Predicates.frames(GTMaterials.StainlessSteel))
                    .where('C', blocks(GTBlocks.CASING_STEEL_TURBINE.get()).setMinGlobalLimited(50)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1).setPreviewCount(1)))
                    .where('D', blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("ad_astra", "vent"))))
                    .where('E', blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                    .where('F', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                    .where('G', blocks(PartAbility.ROTOR_HOLDER.getBlockRange(GTValues.EV, GTValues.UHV).toArray(Block[]::new)))
                    .where('H', blocks(GTBlocks.CASING_TITANIUM_GEARBOX.get()))
                    .build())
            .register();

    public static final MultiblockMachineDefinition EVAPORATION_TOWER = REGISTRATE
            .multiblock("evaporation_tower", DistillationTowerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.EVAPORATION_TOWER)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(TFGBlocks_Casings.STAINLESS_EVAPORATION_CASING)
            .workableCasingModel(
                    TFGCore.id("block/casings/machine_casing_stainless_evaporation"),
                    GTCEu.id("block/multiblock/implosion_compressor"))
            .pattern(definition -> {
                PatternPredicate exportPredicate = abilities(PartAbility.EXPORT_FLUIDS_1X).or(blocks(GTAEMachines.FLUID_EXPORT_HATCH_ME.get()));
                exportPredicate.setMaxLayerLimited(1);

                PatternPredicate maint = Predicates.autoAbilities(true, false, false).setMaxGlobalLimited(1);
                return MultiblockPatternBuilder.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                        .slice("YSY", "YYY", "YYY")
                        .slice("ZZZ", "Z#Z", "ZZZ")
                        .sliceRepeatable(0, 10, "XXX", "X#X", "XXX")
                        .slice("XXX", "XXX", "XXX")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('Y', blocks(TFGBlocks_Casings.STAINLESS_EVAPORATION_CASING.get())
                                .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(2))
                                .or(abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                .or(maint))
                        .where('Z', blocks(TFGBlocks_Casings.STAINLESS_EVAPORATION_CASING.get())
                                .or(exportPredicate)
                                .or(maint))
                        .where('X', blocks(TFGBlocks_Casings.STAINLESS_EVAPORATION_CASING.get())
                                .or(exportPredicate))
                        .where('#', Predicates.air())
                        .build();
            })
            .allowExtendedFacing(false)
            .partSorter(Comparator.comparingInt(p -> p.getBlockPos().getY()))
            .register();

    private static final Supplier<Block> titanium_concrete = () -> ForgeRegistries.BLOCKS
            .getValue(TFGCore.id( "polished_titanium_concrete"));
    private static final Supplier<Block> steel_catwalk = () -> ForgeRegistries.BLOCKS
            .getValue(ResourceLocation.fromNamespaceAndPath("createdeco", "industrial_iron_catwalk"));
    private static final Supplier<Block> titanium_exhaust = () -> ForgeRegistries.BLOCKS
            .getValue(TFGCore.id( "titanium_exhaust_vent"));


    public static final MultiblockMachineDefinition COOLING_TOWER = REGISTRATE
            .multiblock("cooling_tower", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.COOLING_TOWER)
            .recipeModifier(GTRecipeModifiers.OC_PERFECT_SUBTICK)
            .appearanceBlock(TFGBlocks_Casings.OSTRUM_CARBON_CASING)
            .workableCasingModel(TFGCore.id("block/casings/machine_casing_ostrum_carbon"), GTCEu.id("block/multiblock/gcym/large_mixer"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("********A  A  A********", "********A  A  A********", "********BBBBBBB********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .slice("******A         A******", "******A         A******", "******BBEEEEEEEBB******", "******DDD     DDD******", "*******DDDD DDDD*******", "********DDDDDDD********", "**********DDD**********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .slice("****A             A****", "****A             A****", "****BBEEEEEEEEEEEBB****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "*******DDD   DDD*******", "********DDDDDDD********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .slice("***                 ***", "***                 ***", "***BEEEEE     EEEEEB***", "***DD    EEEEE    DD***", "****D     F F     D****", "*****D    G G    D*****", "*****DD         DD*****", "******DD       DD******", "*******DD     DD*******", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "**********EEE**********")
                    .slice("**A                 A**", "**A                 A**", "**BEEEE         EEEEB**", "***D   EEEEEEEEE   D***", "***D    F     F    D***", "****D   G G G G   D****", "****D             D****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "******DD       DD******", "*******DD     DD*******", "********DDDDDDD********", "********DDDDDDD********", "*********DDDDD*********", "**********DDD**********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********D***********", "**********DDD**********", "*********DDDDD*********", "********EEEEEEE********")
                    .slice("**                   **", "**                   **", "**BEEE           EEEB**", "**D   EEEEEEEEEEE   D**", "**D   F    F        D**", "***D  G G GFG G G  D***", "***D       F       D***", "****D      F      D****", "****D      G      D****", "*****D           D*****", "*****D           D*****", "******D    H    D******", "******DD       DD******", "*******D       D*******", "*******DD     DD*******", "********DD   DD********", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "*********DDDDD*********", "*********EEEEE*********", "*********DDDDD*********", "*********DDDDD*********", "********DDDDDDD********", "********DDD DDD********", "********DD   DD********", "*******DDD   DDD*******", "******EEEEMMMEEEE******")
                    .slice("*A                   A*", "*A                   A*", "*BEEE             EEEB*", "*D  FEEEEEEEEEEEEE   D*", "**D F  F       F    D**", "**D G GFG G G GFG G D**", "***D   F       F   D***", "***D   F       F   D***", "****D  GGGGGGGGG  D****", "****D             D****", "****D             D****", "*****D   H H H   D*****", "*****D     I     D*****", "******D    I    D******", "******D    I    D******", "*******DJJJIJJJD*******", "*******D   I   D*******", "*******D   I   D*******", "*******DD  I  DD*******", "********DD I DD********", "********EEEEEEE********", "********DD   DD********", "*******DD     DD*******", "*******D       D*******", "*******D       D*******", "*******D       D*******", "******DD       DD******", "*****EEEMMMMMMMEEE*****")
                    .slice("*         DDD         *", "*         DDD         *", "*BEE      DDD      EEB*", "*D  EEEEEEDDDEEEEEE  D*", "*D        KKK        D*", "**D G G G G G G G G D**", "**D                 D**", "***D               D***", "***D       G       D***", "****D             D****", "****D             D****", "****D    H H H    D****", "*****D   I   I   D*****", "*****D   I   I   D*****", "*****D   I   I   D*****", "******DJJIJJJIJJD******", "******D  I   I  D******", "******DD I   I DD******", "******DD I   I DD******", "*******DDI   IDD*******", "*******EEE   EEE*******", "*******DD     DD*******", "******DD       DD******", "******DD       DD******", "******D         D******", "******D         D******", "*****DD         DD*****", "*****EEMMMMMMMMMEE*****")
                    .slice("A       DDBBBDD       A", "A       DDKKKDD       A", "BEEE    DDKKKDD    EEEB", "*D  EEEEDDKKKDDEEEE  D*", "*D   F  KKKKKKK  F   D*", "*D  GFG G G G G GFG  D*", "**D  F           F  D**", "**D  F           F  D**", "***D GGGGGGGGGGGGG D***", "***D               D***", "***D               D***", "****D  H H H H H  D****", "****D             D****", "****D             D****", "*****D           D*****", "*****DJJJJJJJJJJJD*****", "*****D           D*****", "*****D           D*****", "******D         D******", "******DD       DD******", "******EEE     EEE******", "******DD       DD******", "******D         D******", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****EEMMMMMMMMMMMEE****")
                    .slice("        DBBBBBD        ", "        DKKKKKD        ", "BEE     DK   KD     EEB", "D  EEEEEDK   KDEEEEE  D", "*D      KKKKKKK      D*", "*D  G G G GFG G G G  D*", "**D        F        D**", "**D        F        D**", "**D        G        D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D  I       I  D****", "****D  I       I  D****", "****D  I       I  D****", "*****DJIJJJJJJJIJD*****", "*****D I       I D*****", "*****D I       I D*****", "*****D I       I D*****", "*****DDI       IDD*****", "*****EEE       EEE*****", "*****DD         DD*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****DD           DD****", "****EEMMMMMMMMMMMEE****")
                    .slice("       DBBBBBBBD       ", "       DKKKKKKKD       ", "BEE    DK     KD    EEB", "D  EEEEDK     KDEEEE  D", "*D   F KKKKKKKKK F   D*", "*D  GFG G G G G GFG  D*", "*D   F           F   D*", "**D  F           F  D**", "**D  GGGGGGGGGGGGG  D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D             D****", "****D             D****", "****D             D****", "****D JJJJJJJJJJJ D****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****EE         EE*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****D             D****", "****D             D****", "***EEMMMMMMMMMMMMMEE***")
                    .slice("A      DBBBBBBBD      A", "A      DKKKKKKKD      A", "BEE    DK  H  KD    EEB", "D  EEEEDK  H  KDEEEE  D", "D  F   KKKKHKKKK   F  D", "*D GGGGGGGGGGGGGGGGG D*", "*D         G         D*", "**D        G        D**", "**D        G        D**", "**D                 D**", "***D               D***", "***D   H H L H H   D***", "***D              D****", "****D             D****", "****D             D****", "****DJJJJJJJJJJJJJD****", "****D             D****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****EE         EE*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****D             D****", "****D             D****", "****D             D****", "***EEMMMMMMMMMMMMMEE***")
                    .slice("       DBBBBBBBD       ", "       DKKKKKKKD       ", "BEE    DK     KD    EEB", "D  EEEEDK     KDEEEE  D", "*D   F KKKKKKKKK F   D*", "*D  GFG G G G G GFG  D*", "*D   F           F   D*", "**D  F           F  D**", "**D  GGGGGGGGGGGGG  D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D             D****", "****D             D****", "****D             D****", "****D JJJJJJJJJJJ D****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****EE         EE*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****D             D****", "****D             D****", "***EEMMMMMMMMMMMMMEE***")
                    .slice("        DBBBBBD        ", "        DKKKKKD        ", "BEE     DK   KD     EEB", "D  EEEEEDK   KDEEEEE  D", "*D      KKKKKKK      D*", "*D  G G G GFG G G G  D*", "**D        F        D**", "**D        F        D**", "**D        G        D**", "***D               D***", "***D               D***", "***D   H H H H H   D***", "****D  I       I  D****", "****D  I       I  D****", "****D  I       I  D****", "*****DJIJJJJJJJIJD*****", "*****D I       I D*****", "*****D I       I D*****", "*****D I       I D*****", "*****DDI       IDD*****", "*****EEE       EEE*****", "*****DD         DD*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****DD           DD****", "****EEMMMMMMMMMMMEE****")
                    .slice("A       DDBBBDD       A", "A       DDKKKDD       A", "BEEE    DDKKKDD    EEEB", "*D  EEEEDDKKKDDEEEE  D*", "*D   F  KKKKKKK  F   D*", "*D  GFG G G G G GFG  D*", "**D  F           F  D**", "**D  F           F  D**", "***D GGGGGGGGGGGGG D***", "***D               D***", "***D               D***", "****D  H H H H H  D****", "****D             D****", "****D             D****", "*****D           D*****", "*****DJJJJJJJJJJJD*****", "*****D           D*****", "*****D           D*****", "******D         D******", "******DD       DD******", "******EEE     EEE******", "******DD       DD******", "******D         D******", "*****D           D*****", "*****D           D*****", "*****D           D*****", "*****D           D*****", "****EEMMMMMMMMMMMEE****")
                    .slice("*         DDD         *", "*         DDD         *", "*BEE      DDD      EEB*", "*D  EEEEEEDDDEEEEEE  D*", "*D        KKK        D*", "**D G G G G G G G G D**", "**D                 D**", "***D               D***", "***D       G       D***", "****D             D****", "****D             D****", "****D    H H H    D****", "*****D   I   I   D*****", "*****D   I   I   D*****", "*****D   I   I   D*****", "******DJJIJJJIJJD******", "******D  I   I  D******", "******DD I   I DD******", "******DD I   I DD******", "*******DDI   IDD*******", "*******EEE   EEE*******", "*******DD     DD*******", "******DD       DD******", "******DD       DD******", "******D         D******", "******D         D******", "*****DD         DD*****", "*****EEMMMMMMMMMEE*****")
                    .slice("*A                   A*", "*A                   A*", "*BEEE             EEEB*", "*D  FEEEEEEEEEEEEEF  D*", "**D F  F       F  F D**", "**D G GFG G G GFG G D**", "***D   F       F   D***", "***D   F       F   D***", "****D  GGGGGGGGG  D****", "****D             D****", "****D             D****", "*****D   H H H   D*****", "*****D     I     D*****", "******D    I    D******", "******D    I    D******", "*******DJJJIJJJD*******", "*******D   I   D*******", "*******D   I   D*******", "*******DD  I  DD*******", "********DD I DD********", "********EEEEEEE********", "********DD   DD********", "*******DD     DD*******", "*******D       D*******", "*******D       D*******", "*******D       D*******", "******DD       DD******", "*****EEEMMMMMMMEEE*****")
                    .slice("**                   **", "**                   **", "**BEEE           EEEB**", "**D   EEEEEEEEEEE   D**", "**D   F    F    F   D**", "***D  G G GFG G G  D***", "***D       F       D***", "****D      F      D****", "****D      G      D****", "*****D           D*****", "*****D           D*****", "******D    H    D******", "******DD       DD******", "*******D       D*******", "*******DD     DD*******", "********DD   DD********", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "*********DDDDD*********", "*********EEEEE*********", "*********DDDDD*********", "*********DDDDD*********", "********DDDDDDD********", "********DDD DDD********", "********DD   DD********", "*******DDD   DDD*******", "******EEEEMMMEEEE******")
                    .slice("**A                 A**", "**A                 A**", "**BEEEE         EEEEB**", "***D   EEEEEEEEE   D***", "***D    F     F    D***", "****D   G G G G   D****", "****D             D****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "******DD       DD******", "*******DD     DD*******", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "**********DDD**********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********D***********", "**********DDD**********", "*********DDDDD*********", "********EEEEEEE********")
                    .slice("***                 ***", "***                 ***", "***BEEEEE     EEEEEB***", "***DD    EEEEE    DD***", "****D     F F     D****", "*****D    G G    D*****", "*****DD         DD*****", "******DD       DD******", "*******DD     DD*******", "********DDD DDD********", "********DDDDDDD********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "**********EEE**********")
                    .slice("****A             A****", "****A             A****", "****BBEEEEEEEEEEEBB****", "*****D           D*****", "*****DD         DD*****", "******DD       DD******", "*******DDD   DDD*******", "********DDDDDDD********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .slice("******A         A******", "******A         A******", "******BBEEEEEEEBB******", "******DDD     DDD******", "*******DDDD DDDD*******", "********DDDDDDD********", "**********DDD**********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .slice("********A  A  A********", "********A  A  A********", "********BBBCBBB********", "*********DDDDD*********", "***********D***********", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************", "***********************")
                    .where('*', Predicates.any())
                    .where(' ', Predicates.air())
                    .where('A', Predicates.frames(GTMaterials.TungstenSteel))
                    .where('B', blocks(TFGBlocks_Casings.OSTRUM_CARBON_CASING.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('D', blocks(titanium_concrete.get())
                            .or(Predicates.blockTag(TFGTags.Blocks.TitaniumConcrete)))
                    .where('E', blocks(TFGBlocks_Casings.OSTRUM_CARBON_CASING.get()))
                    .where('F', Predicates.frames(GTMaterials.WatertightSteel))
                    .where('G', blocks(TFGBlocks_Casings.HEAT_PIPE_CASING.get()))
                    .where('H', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                    .where('I', Predicates.frames(GTMaterials.StainlessSteel))
                    .where('J', blocks(steel_catwalk.get()))
                    .where('K', blocks(GCYMBlocks.CASING_CORROSION_PROOF.get()))
                    .where('L', blocks(titanium_exhaust.get()))
                    .where('M', Predicates.air()
                            .or(blocks(ModBlocks.VENT.get())))
                    .build())
            .register();

    public static final MultiblockMachineDefinition GROWTH_CHAMBER = REGISTRATE
            .multiblock("growth_chamber", GrowthChamberMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .recipeType(TFGTRecipeTypes.GROWTH_CHAMBER_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(TFGBlocks_Casings.BIOCULTURE_CASING)
            .tooltips(Component.translatable("tfg.tooltip.machine.parallel"),
                    Component.translatable("tfg.tooltip.growth_chamber"))
            .workableCasingModel(TFGCore.id("block/casings/machine_casing_bioculture"),
                    TFGCore.id("block/machines/growth_chamber"))
            .pattern(definition -> MultiblockPatternBuilder
                    .start(RelativeDirection.LEFT, RelativeDirection.FRONT, RelativeDirection.DOWN)
                    .sliceRepeatable(1, 5, "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "             ANA             ", "             NBN             ", "             AAA             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .slice("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "             HLH             ", "             HHH             ", "           HHAAAHH           ", "           LHAAAHL           ", "           HHAAAHH           ", "             HHH             ", "             HLH             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .slice("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "              K              ", "                             ", "             AAA             ", "           K AAA K           ", "             AAA             ", "                             ", "              K              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .slice("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "              K              ", "                             ", "             MMM             ", "           K MAM K           ", "             MMM             ", "                             ", "              K              ", "                             ", "                             ", "                             ", "                             ", "              O              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .slice("                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ", "              K              ", "                             ", "             AAA             ", "           K AAA K           ", "             AAA             ", "                             ", "              K              ", "                             ", "                             ", "                             ", "                             ", "              A              ", "                             ", "                             ", "                             ", "                             ", "                             ", "                             ")
                    .slice("          AAAAAAAAA          ", "       AAAACCCCCCCAAAA       ", "      AACCCFDFDFDFCCCAA      ", "    AAACDFDFDFDFDFDFDCAAA    ", "   AACCDDFDFDFDFDFDFDDCCAA   ", "   ACCDFDFDCCCCCCCDFDFDCCA   ", "  AAFDCDFCCAAAAAAACCFDCDCAA  ", " AACDFDCCAAAIIJIIAAACCDFDCAA ", " ACDDDFCAAIIIIJIIIIAACFDDDCA ", " ACFFFCAAIIIIIJIIIIIAACFFFCA ", "AACDDDCAIIIIIIJIIIIIIACDDDCAA", "ACFFFCAAIIIIJJKJJIIIIAACFFFCA", "ACDDDCAIIIIJ  A  JIIIIACDDDCA", "ACFFFCAIIIIJ AAA JIIIIACFFFCA", "ACDDDCAJJJJKAAAAAKJJJJACDDDCA", "ACFFFCAIIIIJ AAA JIIIIACFFFCA", "ACDDDCAIIIIJ  A  JIIIIACDDDCA", "ACFFFCAAIIIIJJKJJIIIIAACFFFCA", "AACDDDCAIIIIIIJIIIIIIACDDDCAA", " ACFFFCAAIIIIIJIIIIIAACFFFCA ", " ACDDDFCAAIIIIJIIIIAACFDDDCA ", " AACDFDCCAAAIIJIIAAACCDFDCAA ", "  AACDCDFCCAAAAAAACCFDCDCAA  ", "   ACCDFDFDCCCCCCCDFDFDCCA   ", "   AACCDDFDFDFDFDFDFDDCCAA   ", "    AAACDFDFDFDFDFDFDCAAA    ", "      AACCCFDFDFDFCCCAA      ", "       AAAACCCCCCCAAAA       ", "          AAAAAAAAA          ")
                    .slice("                             ", "           DDDDDDD           ", "        DDD       DDD        ", "       D             D       ", "     DD               DD     ", "    DC     CCCCCCC     CD    ", "    D C  CC       CC  C D    ", "   D   CC           CC   D   ", "  D    C             C    D  ", "  D   C               C   D  ", "  D   C               C   D  ", " D   C        K        C   D ", " D   C                 C   D ", " D   C                 C   D ", " D   C     K     K     C   D ", " D   C                 C   D ", " D   C                 C   D ", " D   C        K        C   D ", "  D   C               C   D  ", "  D   C               C   D  ", "  D    C             C    D  ", "   D   CC           CC   D   ", "    D C  CC       CC  C D    ", "    DC     CCCCCCC     CD    ", "     DD               DD     ", "       D             D       ", "        DDD       DDD        ", "           DDDDDDD           ", "                             ")
                    .slice("                             ", "           DDDDDDD           ", "        DDDE E E EDDD        ", "       D E E E E E E D       ", "     DD  E E E E E E  DD     ", "    DC E E CCCCCCC E E CD    ", "    D C ECC       CCE C D    ", "   D E CC           CC E D   ", "  D   EC             CE   D  ", "  DEEEC               CEEED  ", "  D   C               C   D  ", " DEEEC        K        CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", " D   C     K     K     C   D ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC        K        CEEED ", "  D   C               C   D  ", "  DEEEC               CEEED  ", "  D   EC             CE   D  ", "   D E CC           CC E D   ", "    D C ECC       CCE C D    ", "    DC E E CCCCCCC E E CD    ", "     DD  E E E E E E  DD     ", "       D E E E E E E D       ", "        DDDE E E EDDD        ", "           DDDDDDD           ", "                             ")
                    .slice("                             ", "           DDDDDDD           ", "        DDDE E E EDDD        ", "       D E E E E E E D       ", "     DD  E E E E E E  DD     ", "    DC E E CGCCCGC E E CD    ", "    D C ECC   H   CCE C D    ", "   D E CC     H     CC E D   ", "  D   EC      H      CE   D  ", "  DEEEC       H       CEEED  ", "  D   C       H       C   D  ", " DEEEC        H        CEEED ", " D   G                 G   D ", " DEEEC                 CEEED ", " D   CHHHHHH     HHHHHHC   D ", " DEEEC                 CEEED ", " D   G                 G   D ", " DEEEC        H        CEEED ", "  D   C       H       C   D  ", "  DEEEC       H       CEEED  ", "  D   EC      H      CE   D  ", "   D E CC     H     CC E D   ", "    D C ECC   H   CCE C D    ", "    DC E E CGCCCGC E E CD    ", "     DD  E E E E E E  DD     ", "       D E E E E E E D       ", "        DDDE E E EDDD        ", "           DDDDDDD           ", "                             ")
                    .slice("                             ", "           DDDDDDD           ", "        DDDE E E EDDD        ", "       D E E E E E E D       ", "     DD  E E E E E E  DD     ", "    DC E E CCCCCCC E E CD    ", "    D C ECC       CCE C D    ", "   D E CC           CC E D   ", "  D   EC             CE   D  ", "  DEEEC               CEEED  ", "  D   C               C   D  ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", " D   C                 C   D ", " DEEEC                 CEEED ", "  D   C               C   D  ", "  DEEEC               CEEED  ", "  D   EC             CE   D  ", "   D E CC           CC E D   ", "    D C ECC       CCE C D    ", "    DC E E CCCCCCC E E CD    ", "     DD  E E E E E E  DD     ", "       D E E E E E E D       ", "        DDDE E E EDDD        ", "           DDDDDDD           ", "                             ")
                    .slice("           AAAAAAA           ", "        AAACCCCCCCAAA        ", "       ACCCAAAAAAACCCA       ", "     AACAAAAAAAAAAAAACAA     ", "    ACCAAAAAAAAAAAAAAACCA    ", "   ACAAAAAACCCCCCCAAAAAACA   ", "   ACAAAACC       CCAAAACA   ", "  ACAAAAC           CAAAACA  ", " ACAAAAC             CAAAACA ", " ACAAAC               CAAACA ", " ACAAAC               CAAACA ", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", "ACAAAC                 CAAACA", " ACAAAC               CAAACA ", " ACAAAC               CAAACA ", " ACAAAAC             CAAAACA ", "  ACAAAAC           CAAAACA  ", "   ACAAAACC       CCAAAACA   ", "   ACAAAAAACCCCCCCAAAAAACA   ", "    ACCAAAAAAAAAAAAAAACCA    ", "     AACAAAAAAAAAAAAACAA     ", "       ACCCAAAAAAACCCA       ", "        AAACCCCCCCAAA        ", "           AAAAAAA           ")
                    .where('C', blocks(GTBlocks.PLASTCRETE.get()))
                    .where('E', blocks(TFGBlocks.SAMPLE_RACK.get()))
                    .where('G', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('I', blocks(GTBlocks.CLEANROOM_GLASS.get()))
                    .where('J', Predicates.frames(GTMaterials.HastelloyC276))
                    .where('K', blocks(TFGBlocks_Casings.STERILIZING_PIPE_CASING.get()))
                    .where('L', blocks(TFGBlocks.GROWTH_MONITOR.get()))
                    .where('M', blocks(TFGBlocks_Casings.BIOCULTURE_CASING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .or(abilities(PartAbility.IMPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_FLUIDS))
                            .or(abilities(PartAbility.EXPORT_ITEMS))
                            .or(abilities(PartAbility.MAINTENANCE).setMinGlobalLimited(1))
                            .or(abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1)))
                    .where('N', blocks(TFGMachines.SINGLE_ITEMSTACK_BUS.get()))
                    .where(' ', Predicates.any())
                    .where('H', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                    .where('A', blocks(TFGBlocks_Casings.BIOCULTURE_CASING.get()))
                    .where('F', blocks(TFGBlocks_Casings.IRON_DESH_CASING.get()))
                    .where('F', blocks(TFGBlocks_Casings.ULTRAVIOLET_CASING.get()))
                    .where('D', blocks(TFGBlocks_Casings.BIOCULTURE_GLASS_CASING.get()))
                    .where('B', blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath("megacells", "mega_crafting_unit"))))
                    .where('O', controller(blocks(definition.get())))
                    .build())
            .register();

    /* TODO once fission ported to 8.0
    public static final MultiblockMachineDefinition OSTRUM_LINEAR_ACCELERATOR = REGISTRATE
            .multiblock("ostrum_linear_accelerator", CustomAuxExchangerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGTRecipeTypes.OSTRUM_LINEAR_ACCELERATOR)
            .appearanceBlock(TFGBlocks_Casings.MARS_CASING)
            .workableCasingModel(TFGCore.id( "block/casings/machine_casing_mars"),
                    GTCEu.id("block/machines/thermal_centrifuge"))
            .pattern(definition -> {
                return MultiblockPatternBuilder.start(RelativeDirection.LEFT, RelativeDirection.BACK, RelativeDirection.UP)
                        .slice("A     AFA", "BEBEBEAAA", "AAAAAAAAA", "BAAAAAAAA", "AAAAAAAAA")
                        .slice("A     AXA", "BEBEBEA#D", "K#######D", "B#######D", "AAAAAAAAA")
                        .sliceRepeatable(0, 4, "A     AFA", "BEBEBEA#D", "K#######D", "B#######D", "AAAAAAAAA")
                        .slice("AAAAAAAAA", "BBBBBBBAA", "BB###BBAA", "BBBBBBBAA", "AAAAAAAAA")
                        .slice("         ", " BCCCB   ", " C###C   ", " BCCCB   ", "         ")
                        .slice("         ", " BBBBB   ", " BHHHB   ", " BBBBB   ", "         ")
                        .where('X', controller(blocks(definition.get())))
                        .where('A', blocks(TFGBlocks_Casings.MARS_CASING.get())
                                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                        .where('B', blocks(TFGBlocks_Casings.OSTRUM_CARBON_CASING.get()))
                        .where('C', blocks(TFGBlocks_Casings.VACUUM_ENGINE_INTAKE.get()))
                        .where('D', blocks(GCYMBlocks.HEAT_VENT.get()))
                        .where('E', blocks(TFGBlocks_Casings.MARS_CASING.get())
                                .or(abilities(PartAbility.IMPORT_FLUIDS))
                                .or(abilities(PartAbility.IMPORT_ITEMS))
                                .or(abilities(PartAbility.EXPORT_ITEMS))
                                .or(abilities(PartAbility.EXPORT_FLUIDS)))
                        .where('F', blocks(TFGBlocks_Casings.MARS_CASING.get())
                                .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                        .where('H', blocks(TFGBlocks_Casings.MARS_CASING.get())
                                .or(abilities(PartAbility.EXPORT_ITEMS))
                                .or(abilities(PartAbility.EXPORT_FLUIDS)))
                        .where('K', blocks(TFGBlocks_Casings.MARS_CASING.get())
                                .or(abilities(FisssionGtPartAbilities.USE_HEAT)))
                        .where('#', Predicates.air())
                        .where(' ', Predicates.any())
                        .build();
            })
            .register();*/

    public static final MultiblockMachineDefinition SMR_GENERATOR = REGISTRATE
            .multiblock("smr_generator", (holder) -> new SMRGenerator2(holder, GTValues.EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGTRecipeTypes.SMR_GENERATOR)
            .recipeModifier(SMRGenerator2::recipeModifier, true)
            .appearanceBlock(TFGBlocks_Casings.DESH_PTFE_CASING)
            .workableCasingModel(TFGCore.id( "block/casings/machine_casing_desh_ptfe"), TFGCore.id("block/machines/smr"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("AAA", "ABA", "ABA", "AAA")
                    .slice("AEA", "BDB", "BDB", "AEA")
                    .slice("AAA", "AXA", "ABA", "AAA")
                    .where('X', controller(blocks(definition.get())))
                    .where('A', blocks(TFGBlocks_Casings.OSTRUM_CARBON_CASING.get()))
                    .where('B', blocks(TFGBlocks_Casings.DESH_PTFE_CASING.get()).setMinGlobalLimited(1)
                            .or(abilities((TFGPartAbility.SMR_FLUID_INPUT)))
                            .or(abilities((PartAbility.EXPORT_FLUIDS)))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setExactLimit(1).setPreviewCount(1)))
                    .where('D', blocks(TFGBlocks_Casings.HEAT_PIPE_CASING.get()))
                    .where('E', blocks(MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                    .build())
            .register();


    public static final MultiblockMachineDefinition ACTIVE_POWER_TRANSFORMER = REGISTRATE
            .multiblock("active_power_transformer", ActiveTransformerMachine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .appearanceBlock(TFGBlocks_Casings.MACHINE_CASING_POWER_CASING)
            .tooltips(Component.translatable("gtceu.machine.active_transformer.tooltip.0"),
                    Component.translatable("gtceu.machine.active_transformer.tooltip.1"))
            .tooltipBuilder(
                    (stack,
                     components) -> components.add(Component.translatable("gtceu.machine.active_transformer.tooltip.2")
                            .append(Component.translatable("gtceu.machine.active_transformer.tooltip.3")
                                    .withStyle(TooltipHelper.RAINBOW_HSL_SLOW))))
            .pattern((definition) -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("XXX", "XXX", "XXX")
                    .slice("XXX", "XCX", "XXX")
                    .slice("XXX", "XSX", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('X', blocks(TFGBlocks_Casings.MACHINE_CASING_POWER_CASING.get()).setMinGlobalLimited(12)
                            .or(ActiveTransformerMachine.getHatchPredicates()))
                    .where('C', blocks(TFGBlocks_Casings.SUPERCONDUCTOR_COIL_LARGE_BLOCK.get()))
                    .build())
            .workableCasingModel(TFGCore.id("block/casings/machine_casing_power_casing"),
                    GTCEu.id("block/multiblock/data_bank"))
            .register();

    public static final MultiblockMachineDefinition HYDROPONICS_FACILITY = REGISTRATE
            .multiblock("hydroponics_facility", GreenhouseMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.HYDROPONICS_FACILITY_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(TFGBlocks_Casings.EGH_CASING)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            TFGCore.id( "block/casings/machine_casing_egh"),
                            TFGCore.id("block/machines/hydroponics_facility"))
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
            .pattern((definition) -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("AGGGA", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", "BBGBB", " BBB ")
                    .sliceRepeatable(1, 2, "AHIHA", "B A B", "B A B", "BHIHB", "B A B", "B A B", "BHIHB", "B A B", "BDADB", " BBB ")
                    .sliceRepeatable(1, 2, "EHIHE", "B A B", "B A B", "BHIHB", "B A B", "B A B", "BHIHB", "B A B", "BDADB", " BBB ")
                    .sliceRepeatable(1, 2, "AHIHA", "B A B", "B A B", "BHIHB", "B A B", "B A B", "BHIHB", "B A B", "BDADB", " BBB ")
                    .sliceRepeatable(1, 2, "AAAAA", "B A B", "B A B", "B A B", "B A B", "B A B", "B A B", "B A B", "B A B", " BBB ")
                    .slice(" AAA ", " B B ", " B B ", " B B ", " B B ", " B B ", " BFB ", " B B ", " B B ", " BBB ")
                    .slice(" EEE ", " B B ", " B B ", " B B ", " B B ", " B B ", " B B ", " B B ", " B B ", " BBB ")
                    .slice(" AAA ", " BCB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ", " BBB ")
                    .where(' ', Predicates.any())
                    .where('A', blocks(TFGBlocks_Casings.EGH_CASING.get()))
                    .where('B', Predicates.blockTag(TFGTags.Blocks.StainlessSteelGreenhouseCasings))
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('D', blocks(TFGBlocks_Casings.GROW_LIGHT.get()))
                    .where('E', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('F', blocks(TFGBlocks.CULTIVATION_MONITOR.get()))
                    .where('G', blocks(TFGBlocks_Casings.EGH_CASING.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('H', blocks(TFGBlocks_Casings.EGH_PLANTER.get()))
                    .where('I', blocks(GTBlocks.PLASTCRETE.get()))
                    .build())
            .register();

    public static final MultiblockMachineDefinition PISCICULTURE_FISHERY = REGISTRATE
            .multiblock("pisciculture_fishery", GreenhouseMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.PISCICULTURE_FISHERY_RECIPES)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_NON_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .workableCasingModel(
                    TFGCore.id( "block/casings/machine_casing_aluminium_plated_steel"),
                    TFGCore.id("block/machines/pisciculture_fishery"))
            .pattern((definition) -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("    AAAAA    ", "    BBBBB    ", "    BBBBB    ", "    CCCCC    ")
                    .slice("   ACCECCA   ", "   CFFFFFC   ", "   CFFFFFC   ", "   CFFFFFC   ")
                    .slice("  ACGCECGCA  ", "  BFFFFFFFB  ", "  BFFFFFFFB  ", "  CFFFFFFFC  ")
                    .slice(" ACCGCECGCCA ", " CFFFFFFFFFC ", " CFFFFFFFFFC ", " CFFFFFFFFFC ")
                    .slice("ACGGGCECGGGCA", "BFFFFFFFFFFFB", "BFFFFFFFFFFFB", "CFFFFFFFFFFFC")
                    .slice("ACCCCCECCCCCA", "BFFFFFFFFFFFB", "BFFFFFFFFFFFB", "CFFFFFFFFFFFC")
                    .slice("AEEEEEIEEEEEA", "BFFFFFFFFFFFB", "BFFFFFFFFFFFB", "CFFFFFFFFFFFC")
                    .slice("ACCCCCECCCCCA", "BFFFFFFFFFFFB", "BFFFFFFFFFFFB", "CFFFFFFFFFFFC")
                    .slice("ACGGGCECGGGCA", "BFFFFFFFFFFFB", "BFFFFFFFFFFFB", "CFFFFFFFFFFFC")
                    .slice(" ACCGCECGCCA ", " CFFFFFFFFFC ", " CFFFFFFFFFC ", " CFFFFFFFFFC ")
                    .slice("  ACGCECGCA  ", "  BFFFFFFFB  ", "  BFFFFFFFB  ", "  CFFFFFFFC  ")
                    .slice("   ACCECCA   ", "   CFFFFFC   ", "   CFFFFFC   ", "   CFFFFFC   ")
                    .slice("    AAAAA    ", "    BBBBB    ", "    BBBBB    ", "    CCJCC    ")
                    .where(' ', Predicates.any())
                    .where('A', blocks(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get()).setMinGlobalLimited(20)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, true))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('B', Predicates.blockTag(TFGTags.Blocks.StainlessSteelGreenhouseCasings))
                    .where('C', blocks(TFGBlocks_Casings.MACHINE_CASING_ALUMINIUM_PLATED_STEEL.get()))
                    .where('E', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                    .where('F', Predicates.fluidTag(TagKey.create(Registries.FLUID, TFGCore.id( "pisciculture_fishery_fluids"))))
                    .where('G', Predicates.blockTag(TagKey.create(Registries.BLOCK, TFGCore.id( "gtceu_concrete_blocks"))))
                    .where('I', blocks(TFGBlocks_Casings.PISCICULTURE_CORE.get()))
                    .where('J', controller(blocks(definition.getBlock())))
                    .build())
            .register();

    public static final MultiblockMachineDefinition STEAM_BLOOMERY = REGISTRATE
            .multiblock("steam_bloomery", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.STEAM_BLOOMERY)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    TFGCore.id( "block/machines/steam_bloomery"))
            .pattern((definition) -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice(" F ", " C ", " E ", " E ", " E ")
                    .slice("FCF", "C#C", "E#E", "E#E", "E#E")
                    .slice(" F ", "CXC", " E ", " E ", " E ")
                    .where('X', controller(blocks(definition.getBlock())))
                    .where('C', Predicates.blockTag(TFCTags.Blocks.BLOOMERY_INSULATION))
                    .where('F', blocks(GTBlocks.FIREBOX_BRONZE.get())
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('E', abilities(PartAbility.STEAM_IMPORT_ITEMS).setMaxGlobalLimited(2)
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setExactLimit(1))
                            .or(Predicates.blockTag(TFCTags.Blocks.BLOOMERY_INSULATION)))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .build())
            .register();

    public final static MultiblockMachineDefinition STEAM_THERMAL_CENTRIFUGE = REGISTRATE
            .multiblock("steam_thermal_centrifuge", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .recipeTypes(GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES)
            .recipeModifier((machine, recipe) -> {
                int parallelAmount = ParallelLogic.getParallelAmount(machine, recipe, 8);
                return ModifierFunction.builder()
                        .inputModifier(ContentModifier.multiplier(parallelAmount))
                        .outputModifier(ContentModifier.multiplier(parallelAmount))
                        .eutMultiplier(parallelAmount)
                        .parallels(parallelAmount)
                        .build();
            }, true)
            .appearanceBlock(GCYMBlocks.CASING_INDUSTRIAL_STEAM)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice(" FFF ", "BBBBB", " BBB ")
                    .slice("FXXXF", "B#P#B", "BBBBB")
                    .slice("FXXXF", "BPGPB", "BBBBB")
                    .slice("FXXXF", "B#P#B", "BBBBB")
                    .slice(" FFF ", "BBSBB", " BBB ")
                    .where('S', controller(blocks(definition.get())))
                    .where('F', blocks(GTBlocks.FIREBOX_STEEL.get())
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .where('X', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('G', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('P', blocks(GTBlocks.CASING_BRONZE_PIPE.get())
							.or(blocks(GTBlocks.CASING_STEEL_PIPE.get()))
							.or(blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
							.or(blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
							.or(blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get())))
                    .where('B', blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get())
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .build())
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                            GTCEu.id("block/machines/thermal_centrifuge"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(BoilerFireboxType.STEEL_FIREBOX, GCYMBlocks.CASING_INDUSTRIAL_STEAM))))
            .register();

    public static final MultiblockMachineDefinition STEAM_FUSER = REGISTRATE
            .multiblock("steam_fuser", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.ALLOY_SMELTER_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("FFF", "XXX", "   ")
                    .slice("FFF", "X#X", "XXX")
                    .slice("FFF", "XSX", "   ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('X', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(6)
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setExactLimit(1)))
                    .where('F', blocks(GTBlocks.FIREBOX_BRONZE.get())
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/machines/alloy_smelter"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(BoilerFireboxType.BRONZE_FIREBOX, GTBlocks.CASING_BRONZE_BRICKS))))
            .register();

    public static final MultiblockMachineDefinition STEAM_SQUASHER = REGISTRATE
            .multiblock("steam_squasher", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.COMPRESSOR_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("XXX", "FXF", "   ")
                    .slice("XXX", "A#A", "FAF")
                    .slice("XXX", "FSF", "   ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('A', blocks(GTBlocks.BRONZE_HULL.get()))
                    .where('F', Predicates.frames(GTMaterials.Steel))
                    .where('X', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(7)
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/machines/compressor"))
            .register();

    public static final MultiblockMachineDefinition STEAM_PRESSER = REGISTRATE
            .multiblock("steam_presser", SteamParallelMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .recipeType(GTRecipeTypes.FORGE_HAMMER_RECIPES)
            .recipeModifier(SteamParallelMultiblockMachine::recipeModifier, true)
            .addOutputLimit(ItemRecipeCapability.CAP, 1)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("XXX", "G G", "G G", "XXX")
                    .slice("XAX", " A ", " A ", "XAX")
                    .slice("XSX", "G G", "G G", "XXX")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where(' ', Predicates.any())
                    .where('A', blocks(GTBlocks.STEEL_HULL.get()))
                    .where('G', blocks(AllBlocks.METAL_GIRDER.get()))
                    .where('X', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(12)
                            .or(abilities(PartAbility.STEAM_IMPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.STEAM_EXPORT_ITEMS).setExactLimit(1))
                            .or(abilities(PartAbility.STEAM).setExactLimit(1)))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                    GTCEu.id("block/machines/forge_hammer"))
            .register();

    public static final MultiblockMachineDefinition HEAT_EXCHANGER = REGISTRATE
            .multiblock("heat_exchanger", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.ALL)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .recipeType(TFGTRecipeTypes.HEAT_EXCHANGER)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT_SUBTICK, GTRecipeModifiers.BATCH_MODE)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("       ","BBBBBBB","BCCCCCB","BBBBBBB","       ")
                    .slice("AAAAAAA","A#####A","LDDDDDL","A#####A","AAAAAAA")
                    .slice("AFFFFFA","L#####L","LEEEEEL","L#####L","AFFFFFA")
                    .slice("AAAAAAA","A#####A","LDDDDDL","A#####A","AAAAAAA")
                    .slice("       ","BBBXBBB","BCCCCCB","BBBMBBB","       ")
                    .where('X', controller(blocks(definition.get())))
                    .where('A', blocks(GCYMBlocks.CASING_ATOMIC.get()))
                    .where('B', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())
                            .or(abilities(PartAbility.INPUT_ENERGY).setExactLimit(1)))
                    .where('C', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('D', blocks(GTBlocks.FIREBOX_TITANIUM.get()))
                    .where('E', blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                    .where('F', blocks(GTBlocks.CASING_ENGINE_INTAKE.get()))
                    .where('L', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS_1X, PartAbility.IMPORT_FLUIDS_4X, PartAbility.IMPORT_FLUIDS_9X)
                                    .setMaxGlobalLimited(4).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS_1X, PartAbility.EXPORT_FLUIDS_4X, PartAbility.EXPORT_FLUIDS_9X)
                                    .setMaxGlobalLimited(4).setPreviewCount(1)))
                    .where('M', abilities(PartAbility.MAINTENANCE).setExactLimit(1)
                            .or(blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .build())
            .workableCasingModel(
                    GTCEu.id("gtceu:block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("gtceu:block/machines/fluid_heater"))
            .register();

    /* TODO once fission ported to 8.0
    public static final MultiblockMachineDefinition HEAT_BATTERY_MK_1 = REGISTRATE
            .multiblock("heat_battery_mk1", HbMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .recipeType(FissionGtRecipeTypes.HbImportRecipe)
            .recipeType(FissionGtRecipeTypes.HbExportRecipe)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .appearanceBlock(TFGBlocks_Casings.MARS_CASING)
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .workableCasingModel(
                    TFGCore.id( "block/casings/machine_casing_mars"),
                    TFGCore.id("block/machines/bioreactor"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("##BBB##", "##CCC##", "##CDC##", "##CDC##", "##CDC##", "##CCC##", "##BBB##")
                    .slice("#BBBBB#", "#BAAAB#", "#BAAAB#", "#BAAAB#", "#BAAAB#", "#BAAAB#", "#BBBBB#")
                    .slice("BBFFFBB", "CAFFFAC", "CAFAFAC", "CAFAFAC", "CAFAFAC", "CAFFFAC", "BBFFFBB")
                    .slice("BBFFFBB", "CAFFFAC", "DAAGAAD", "DAAGAAD", "DAAGAAD", "CAFFFAC", "BBFFFBB")
                    .slice("BBFFFBB", "CAFFFAC", "CAFAFAC", "CAFAFAC", "CAFAFAC", "CAFFFAC", "BBFFFBB")
                    .slice("#BBBBB#", "#BAAAB#", "#BAAAB#", "#BAAAB#", "#BAAAB#", "#BAAAB#", "#BBBBB#")
                    .slice("##BBB##", "##CYC##", "##CDC##", "##CDC##", "##CDC##", "##CCC##", "##BBB##")
                    .where('Y', controller(blocks(definition.getBlock())))
                    .where('#', Predicates.any())
                    .where('A', Predicates.air())
                    .where('B', blocks(TFGBlocks_Casings.OSTRUM_CARBON_CASING.get()))
                    .where('C', blocks(TFGBlocks_Casings.MARS_CASING.get())
                            .or(abilities(PartAbility.IMPORT_FLUIDS_1X, PartAbility.IMPORT_FLUIDS_4X, PartAbility.IMPORT_FLUIDS_9X)
                                    .setMaxGlobalLimited(6).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_FLUIDS_1X, PartAbility.EXPORT_FLUIDS_4X, PartAbility.EXPORT_FLUIDS_9X)
                                    .setMaxGlobalLimited(6).setPreviewCount(1)))
                    .where('D', blocks(GTBlocks.CASING_LAMINATED_GLASS.get())
                            .or(blocks(HeatPortEv.get()).setMaxGlobalLimited(1).setPreviewCount(1)))
                    .where('F', blocks(TFGBlocks_Casings.HEAT_PIPE_CASING.get()))
                    .where('G', Predicates.blockTag(FissionTags.COMPONENT_HB)
                            .or(Predicates.air()))
                    .build())
            .register();
     */
    public static final MultiblockMachineDefinition PRECISION_FABRICATOR = REGISTRATE
            .multiblock("high_temp_precision_fabricator", CoilWorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.PRECISION_FABRICATOR_RECIPES)
            .recipeModifiers(GTRecipeModifiers::ebfOverclock, GTRecipeModifiers.BATCH_MODE)
            .appearanceBlock(TFGBlocks_Casings.STERLING_SILVER_CASING)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("ACABB", "ACABA", "AAAAA", "     ")
                    .slice("CDCBB", "C#C#B", "AFFFB", " AAAB")
                    .slice("AXABB", "AEABA", "AAAAA", "     ")
                    .where('X', controller(blocks(definition.getBlock())))
                    .where('A', blocks(TFGBlocks_Casings.STERLING_SILVER_CASING.get()).setMinGlobalLimited(15)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('B', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('C', Predicates.heatingCoils())
                    .where('D', blocks(TFGBlocks.QUARTZ_CRUCIBLE.get()))
                    .where('E', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                    .where('F', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .build())
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            TFGCore.id("block/casings/sterling_silver_casing"),
                            GTCEu.id("block/multiblock/gcym/large_chemical_bath"))
                    .andThen(b -> b.addDynamicRenderer(BouleRender::makeRender))
            )
            .additionalDisplay((controller, components) -> {
                if (controller instanceof CoilWorkableElectricMultiblockMachine coilMachine && controller.isFormed()) {
                    components.add(Component.translatable("gtceu.multiblock.blast_furnace.max_temperature",
                            Component.translatable(FormattingUtil.formatNumbers(coilMachine.getCoilType().getCoilTemperature() +
                                            100L * Math.max(0, coilMachine.getTier() - GTValues.MV)) + "K")
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                }
            })
            .register();

    public static final MultiblockMachineDefinition LARGE_BOILER_BRONZE = REGISTRATE
            .multiblock("large_bronze_boiler",
                    holder -> new TFGLargeBoilerMachine(holder, 480, 1))
            .langValue("Large Bronze Boiler")
            .allowExtendedFacing(false)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.LARGE_BOILER_RECIPES)
            .recipeModifier(TFGLargeBoilerMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_BRONZE_BRICKS)
            .partAppearance((controller, part, side) ->
                    controller.getBlockPos().below().getY() == part.getBlockPos().getY() ?
                            GTBlocks.FIREBOX_BRONZE.get().defaultBlockState() :
                            GTBlocks.CASING_BRONZE_BRICKS.get().defaultBlockState())
            .pattern((definition) -> {
                PatternPredicate fireboxPred = blocks(ALL_FIREBOXES.get(BoilerFireboxType.BRONZE_FIREBOX).get())
                        .setMinGlobalLimited(3)
                        .or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(PartAbility.MUFFLER).setExactLimit(1));
                if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
                    fireboxPred = fireboxPred.or(abilities(PartAbility.MAINTENANCE).setExactLimit(1));
                }
                return MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                        .slice("XXX", "CCC", "CCC", "CCC")
                        .slice("XXX", "CPC", "CPC", "CCC")
                        .slice("XXX", "CSC", "CCC", "CCC")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('P', blocks(GTBlocks.CASING_BRONZE_PIPE.get()))
                        .where('X', fireboxPred)
                        .where('C', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()).setMinGlobalLimited(20)
                                .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1)))
                        .build();
            })
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_bronze_plated_bricks"),
                            GTCEu.id("block/multiblock/generator/large_bronze_boiler"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(
                                    BoilerFireboxType.BRONZE_FIREBOX, GTBlocks.CASING_BRONZE_BRICKS))))
            .tooltips(
                    Component.translatable("tfg.multiblock.large_boiler.max_temperature", 480, 480),
                    Component.translatable("gtceu.multiblock.large_boiler.heat_time_tooltip", 480 / 1 / 20),
                    Component.translatable("gtceu.multiblock.large_boiler.explosion_tooltip")
                            .withStyle(ChatFormatting.DARK_RED))
            .register();

    public static final MultiblockMachineDefinition LARGE_STEEL_BOILER = REGISTRATE
            .multiblock("large_steel_boiler",
                    holder -> new TFGLargeBoilerMachine(holder, 1280, 1))
            .langValue("Large Steel Boiler")
            .allowExtendedFacing(false)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.LARGE_BOILER_RECIPES, TFGTRecipeTypes.SUPER_BOILER)
            .recipeModifier(TFGLargeBoilerMachine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .partAppearance((controller, part, side) ->
                    controller.getBlockPos().below().getY() == part.getBlockPos().getY() ?
                            GTBlocks.FIREBOX_STEEL.get().defaultBlockState() :
                            GTBlocks.CASING_STEEL_SOLID.get().defaultBlockState())
            .pattern((definition) -> {
                PatternPredicate fireboxPred = blocks(ALL_FIREBOXES.get(BoilerFireboxType.STEEL_FIREBOX).get())
                        .setMinGlobalLimited(3)
                        .or(abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(PartAbility.MUFFLER).setExactLimit(1));
                if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
                    fireboxPred = fireboxPred.or(abilities(PartAbility.MAINTENANCE).setExactLimit(1));
                }
                return MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                        .slice("XXX", "CCC", "CCC", "CCC")
                        .slice("XXX", "CPC", "CPC", "CCC")
                        .slice("XXX", "CSC", "CCC", "CCC")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('P', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                        .where('X', fireboxPred)
                        .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get()).setMinGlobalLimited(20)
                                .or(abilities(PartAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(1)))
                        .build();
            })
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(
                            GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                            GTCEu.id("block/multiblock/generator/large_steel_boiler"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> DynamicRenderHelper.makeBoilerPartRender(
                                    BoilerFireboxType.STEEL_FIREBOX, GTBlocks.CASING_STEEL_SOLID))))
            .tooltips(
                    Component.translatable("tfg.multiblock.large_boiler.max_temperature", 1280, 1280),
                    Component.translatable("gtceu.multiblock.large_boiler.heat_time_tooltip", 1280 / 1 / 20),
                    Component.translatable("gtceu.multiblock.large_boiler.explosion_tooltip")
                            .withStyle(ChatFormatting.DARK_RED))
            .register();

    public static final MultiblockMachineDefinition LARGE_STEAM_TURBINE = REGISTRATE
            .multiblock("large_steam_turbine", holder -> new LargeSteamTurbine(holder, GTValues.HV))
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.STEAM_TURBINE_FUELS)
            .generator(true)
            .recipeModifier(LargeSteamTurbine::recipeModifier, true)
            .appearanceBlock(GTBlocks.CASING_STEEL_TURBINE)
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("CCCC", "CHHC", "CCCC")
                    .slice("CHHC", "RGGR", "CHHC")
                    .slice("CCCC", "CSHC", "CCCC")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('G', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .where('C', blocks(GTBlocks.CASING_STEEL_TURBINE.get()))
                    .where('R',
                            new PatternPredicate(
                                            state -> {
                                                var result = state.getBlockEntity() instanceof RotorHolderPartMachine rotorHolder &&
                                                        state.getLevel()
                                                                .getBlockState(state.getPos()
                                                                        .relative(rotorHolder.getFrontFacing()))
                                                                .isAir() &&
                                                        rotorHolder.getDefinition().getTier() >= GTValues.HV &&
                                                        rotorHolder.getDefinition().getTier() <= GTValues.EV;
                                                return result ? null : new BlockMatchingError(state.getPos().immutable(), PartAbility.ROTOR_HOLDER.getBlockRange(GTValues.HV, GTValues.EV).stream().toList());
                                            },
                                            PartAbility.ROTOR_HOLDER.getBlockRange(GTValues.HV, GTValues.EV).stream()
                                                    .map(BlockInfo::fromBlock).collect(Collectors.toList()))
                                    .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                                    .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                            VN[GTValues.HV]))
                                    .setExactLimit(1)
                                    .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                    .where('H', blocks(GTBlocks.CASING_STEEL_TURBINE.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                            .or(Predicates.autoAbilities(true, false, false))) // needsMuffler = false
                    .build())
            .recoveryItems(
                    () -> new ItemLike[] {
                            GTMaterialItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
            .workableCasingModel(
                    GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"),
                    GTCEu.id("block/multiblock/generator/large_steam_turbine"))
            .tooltips(
                    Component.translatable("gtceu.universal.tooltip.base_production_eut", V[GTValues.HV] * 4),
                    Component.translatable("gtceu.multiblock.turbine.efficiency_tooltip", VNF[GTValues.HV]))
            .register();

    public static final MultiblockMachineDefinition GAS_WELL = REGISTRATE
            .multiblock("gas_well", GasWellMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(GTBlocks.STEEL_HULL)
            .tooltips(
                    Component.translatable("tfg.tooltip.machine.gas_well_1"),
                    Component.translatable("tfg.tooltip.machine.gas_well_2",
                            GasWellRecipeLogic.EXPLOSIVE_CONSUMPTION_INTERVAL))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("AAA", "FXF")
                    .slice("AAA", "XBX")
                    .slice("AAA", "FSF")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(GTBlocks.STEEL_HULL.get()).setMinGlobalLimited(1)
                            .or(abilities(PartAbility.IMPORT_FLUIDS_1X).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)))
                    .where('A', blocks(GTBlocks.STEEL_BRICKS_HULL.get()))
                    .where('F', Predicates.frames(GTMaterials.Steel))
                    .where('B', abilities(PartAbility.EXPORT_FLUIDS_1X).setMinGlobalLimited(1).setMaxGlobalLimited(1).setPreviewCount(1))
                    .build())
            .workableCasingModel(
                    GTCEu.id("block/casings/steam/steel/side"),
                    GTCEu.id("block/generators/naquadah_reactor_solid"))
            .register();

    public static final MultiblockMachineDefinition PASTORAL_ENGINE = REGISTRATE
            .multiblock("pastoral_engine", PastoralEngineMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .recipeType(TFGTRecipeTypes.PASTORAL_ENGINE_RECIPES)
            .recipeModifiers(AnimalProductModifier.INSTANCE)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .tooltips(
                    Component.translatable("tfg.tooltip.machine.pastoral_engine_1"),
                    Component.translatable("tfg.tooltip.machine.pastoral_engine_2"))
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    TFGCore.id("block/machines/pisciculture_fishery"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("BBBBBBBBB", "DDDDDDDDD", "AAAAAAAAA", "AAAAAAAAA")
                    .slice("BFFFFFFFB", "DAAAAAAAD", "AAAAAAAAA", "AAAAAAAAA")
                    .slice("BFFFFFFFB", "DAAAAAAAD", "AAAAAAAAA", "AAAAAAAAA")
                    .slice("BFFFFFFFB", "DAAAAAAAD", "AAAAAAAAA", "AAAAAAAAA")
                    .slice("BFFFBBBBB", "DAAACEEEC", "AAAACEEEC", "AAAACCCCC")
                    .slice("BFFFBEEEB", "DAAAEHHGA", "AAAAEHHGA", "AAAACEEEC")
                    .slice("BFFFBEEEB", "DAAAEGGGA", "AAAAEGSGA", "AAAACEEEC")
                    .slice("BBBBBBBBB", "DDDDCAAAC", "AAAACAAAC", "AAAACCCCC")
                    .where('S', controller(blocks(definition.get())))
                    .where('A', Predicates.any())
                    .where('B', blocks(GTBlocks.STEEL_HULL.get()))
					.where('C', Predicates.blockTag(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", "stone_bricks"))))
                    .where('D', Predicates.blockTag(Tags.Blocks.FENCES)
                            .or(Predicates.blockTag(Tags.Blocks.FENCE_GATES))
							.or(Predicates.blockTag(BlockTags.WALLS)))
                    .where('E', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('F', Predicates.blockTag(BlockTags.DIRT)
                            .or(Predicates.blockTag(TFCTags.Blocks.GRASS)))
                    .where('G', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.autoAbilities(true, false, false)))
                    .where('H', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                    .build())
            .register();

    public static final MultiblockMachineDefinition ORE_PROCESSING_BENEATH = REGISTRATE
            .multiblock("ore_processing_beneath", OreProcessingBeneathMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(TFGTRecipeTypes.ORE_PROCESSING_GAS)
            .recipeModifiers(
                    OreProcessingBeneathMachine::parallelModifier,
                    GTRecipeModifiers.OC_NON_PERFECT,
                    OreProcessingBeneathMachine::recipeModifier)
            .appearanceBlock(GCYMBlocks.CASING_INDUSTRIAL_STEAM)
            .tooltips(
                    Component.translatable("tfg.tooltip.machine.ore_proc_beneath_1"),
                    Component.translatable("tfg.tooltip.machine.ore_proc_beneath_2"),
					Component.translatable("tfg.tooltip.machine.ore_proc_beneath_3"),
                    Component.translatable("tfg.tooltip.machine.two_energy_hatches"))
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/industrial_steam_casing"),
                    GTCEu.id("block/machines/electromagnetic_separator"))
            .pattern(definition -> MultiblockPatternBuilder.start(RelativeDirection.FRONT, RelativeDirection.RIGHT, RelativeDirection.UP)
                    .slice("AAAAA", "BCCCB", "BCCCB", "BCCCB", "BCCCB", "BACAB", "AAAAA")
                    .slice("ADDDA", "C#F#C", "C#F#C", "C#F#C", "C#F#C", "A#F#A", "AAAAA")
                    .slice("ADDDA", "CFGFC", "CFGFC", "CFGFC", "CFGFC", "AFGFA", "AAHAA")
                    .slice("AAAAA", "BAFAB", "B#F#B", "B#F#B", "B#F#B", "B#F#B", "AAAAA")
                    .slice(" AAA ", " AXA ", "     ", "     ", "     ", "     ", " AAA ")
                    .where('X', controller(blocks(definition.get())))
                    .where('A', blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get()).setMinGlobalLimited(6)
                            .or(abilities(PartAbility.IMPORT_FLUIDS_1X).setExactLimit(2).setPreviewCount(2))
                            .or(abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.autoAbilities(true, false, false))
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2)))
                    .where('B', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                    .where('C', blocks(GTBlocks.CASING_BRONZE_BRICKS.get()))
                    .where('D', blocks(GTBlocks.STEEL_BRICKS_HULL.get()))
                    .where('#', Predicates.air())
                    .where(' ', Predicates.any())
                    .where('F', Predicates.frames(GTMaterials.Bronze))
                    .where('G', blocks(GTBlocks.FIREBOX_BRONZE.get()))
                    .where('H', abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .build())
            .register();

    // spotless:on
}
