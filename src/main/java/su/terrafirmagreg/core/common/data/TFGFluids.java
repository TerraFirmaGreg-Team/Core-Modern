package su.terrafirmagreg.core.common.data;

import static net.dries007.tfc.common.fluids.TFCFluids.*;

import java.util.function.Consumer;
import java.util.function.Function;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.fluids.*;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidInteractionRegistry;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;
import su.terrafirmagreg.core.common.data.items.TFGItems;
import su.terrafirmagreg.core.common.data.items.TFGItems_Asphalt;

public class TFGFluids {

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, TFGCore.MOD_ID);

    public static final FluidRegistryObject<ForgeFlowingFluid> MARS_WATER = register(
            "semiheavy_ammoniacal_water",
            properties -> properties
                    .block(TFGBlocks.MARS_WATER)
                    .bucket(TFGItems.MARS_WATER_BUCKET),
            waterLike()
                    .temperature(213)
                    .descriptionId("fluid.tfg.semiheavy_ammoniacal_water"),

            new FluidTypeClientProperties(ALPHA_MASK | 0x55d9b1, WATER_STILL, WATER_FLOW, WATER_OVERLAY,
                    UNDERWATER_LOCATION),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    public static final FluidRegistryObject<ForgeFlowingFluid> MUDDY_WATER = register(
            "muddy_water",
            properties -> properties
                    .block(TFGBlocks.MUDDY_WATER)
                    .bucket(TFGItems.MUDDY_WATER_BUCKET),
            waterLike()
                    .temperature(273)
                    .descriptionId("fluid.tfg.muddy_water"),

            new FluidTypeClientProperties(ALPHA_MASK | 0x734B26, WATER_STILL, WATER_FLOW, WATER_OVERLAY,
                    UNDERWATER_LOCATION),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    public static final FluidRegistryObject<ForgeFlowingFluid> SULFUR_FUMES = register(
            "sulfur_fumes",
            properties -> properties
                    .block(TFGBlocks.SULFUR_FUMES)
                    .bucket(TFGItems.SULFUR_FUMES_BUCKET),
            gasLike()
                    .temperature(737)
                    .viscosity(0)
                    .density(0)
                    .canSwim(true)
                    .descriptionId("fluid.tfg.sulfur_fumes"),
            new FluidTypeClientProperties(ALPHA_MASK | 0xFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "block/planets/venus/sulfur_fumes_still"),
                    ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "block/planets/venus/sulfur_fumes_flow"),
                    null, null),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    public static final FluidRegistryObject<ForgeFlowingFluid> GEYSER_SLURRY = register(
            "geyser_slurry",
            properties -> properties
                    .block(TFGBlocks.GEYSER_SLURRY)
                    .bucket(TFGItems.GEYSER_SLURRY_BUCKET),
            FluidType.Properties.create()
                    .adjacentPathType(BlockPathTypes.LAVA)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .density(3000)
                    .viscosity(6000)
                    .canConvertToSource(false)
                    .canExtinguish(true)
                    .canHydrate(false)
                    .supportsBoating(false)
                    .canDrown(true)
                    .canSwim(true)
                    .temperature(1300)
                    .canPushEntity(true)
                    .descriptionId("fluid.tfg.geyser_slurry"),
            new FluidTypeClientProperties(ALPHA_MASK | 0xFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "block/planets/venus/geyser_slurry_still"),
                    ResourceLocation.fromNamespaceAndPath(TFGCore.MOD_ID, "block/planets/venus/geyser_slurry_flow"),
                    null, null),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    public static final FluidRegistryObject<ForgeFlowingFluid> ASPHALT_MIX = register(
            "asphalt_mix",
            properties -> properties
                    .bucket(TFGItems_Asphalt.ASPHALT_MIX_BUCKET),
            FluidType.Properties.create()
                    .adjacentPathType(BlockPathTypes.LAVA)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                    .density(3000)
                    .viscosity(6000)
                    .canConvertToSource(false)
                    .canExtinguish(false)
                    .canHydrate(false)
                    .supportsBoating(false)
                    .canDrown(false)
                    .canSwim(false)
                    .temperature(AsphaltRoadHelper.TEMPERATURE)
                    .canPushEntity(false)
                    .descriptionId("fluid.tfg.asphalt_mix"),
            new FluidTypeClientProperties(ALPHA_MASK | 0x141418, WATER_STILL, WATER_FLOW, null, null),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    public static final FluidRegistryObject<ForgeFlowingFluid> PRISMATIC_PAINT = register(
            "prismatic_paint",
            properties -> properties
                    .bucket(TFGItems.PRISMATIC_PAINT_BUCKET),
            FluidType.Properties.create()
                    .adjacentPathType(BlockPathTypes.WATER)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(1200)
                    .viscosity(2000)
                    .canConvertToSource(false)
                    .canExtinguish(true)
                    .canHydrate(false)
                    .supportsBoating(false)
                    .canDrown(true)
                    .canSwim(true)
                    .temperature(293)
                    .canPushEntity(true)
                    .descriptionId("fluid.tfg.prismatic_paint"),
            new FluidTypeClientProperties(ALPHA_MASK | 0x00FFFF, WATER_STILL, WATER_FLOW, null, null),
            MixingFluid.Source::new,
            MixingFluid.Flowing::new);

    // TFC, why did you have to make this private

    private static FluidType.Properties waterLike() {
        return FluidType.Properties.create()
                .adjacentPathType(BlockPathTypes.WATER)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .canConvertToSource(true)
                .canDrown(true)
                .canExtinguish(true)
                .canHydrate(true)
                .canPushEntity(true)
                .canSwim(true)
                .supportsBoating(true);
    }

    private static FluidType.Properties gasLike() {
        return FluidType.Properties.create()
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .canConvertToSource(true)
                .canDrown(false)
                .canExtinguish(false)
                .canHydrate(false)
                .canPushEntity(false)
                .canSwim(false)
                .supportsBoating(false);
    }

    public static void registerFluidInteractions() {
        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                MARS_WATER.getSource().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource())
                        return Blocks.OBSIDIAN.defaultBlockState();
                    else
                        return GTBlocks.RED_GRANITE.get().defaultBlockState();
                }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                MUDDY_WATER.getSource().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource())
                        return Blocks.OBSIDIAN.defaultBlockState();
                    else
                        return TFCBlocks.ROCK_BLOCKS.get(Rock.DACITE).get(Rock.BlockType.RAW).get().defaultBlockState();
                }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                SULFUR_FUMES.getSource().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource())
                        return Blocks.OBSIDIAN.defaultBlockState();
                    else
                        return AllPaletteStoneTypes.SCORIA.getBaseBlock().get().defaultBlockState();
                }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                GEYSER_SLURRY.getSource().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource())
                        return Blocks.OBSIDIAN.defaultBlockState();
                    else
                        return Blocks.DRIPSTONE_BLOCK.defaultBlockState();
                }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                SPRING_WATER.getSource().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource())
                        return Blocks.OBSIDIAN.defaultBlockState();
                    else
                        return Blocks.CALCITE.defaultBlockState();
                }));

        FluidInteractionRegistry.addInteraction(ForgeMod.LAVA_TYPE.get(), new FluidInteractionRegistry.InteractionInformation(
                SPRING_WATER.getSource().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource())
                        return Blocks.OBSIDIAN.defaultBlockState();
                    else
                        return TFCBlocks.ROCK_BLOCKS.get(Rock.ANDESITE).get(Rock.BlockType.RAW).get().defaultBlockState();
                }));
    }

    // Registration helpers

    private static <F extends FlowingFluid> FluidRegistryObject<F> register(String name,
            Consumer<ForgeFlowingFluid.Properties> builder, FluidType.Properties typeProperties,
            FluidTypeClientProperties clientProperties, Function<ForgeFlowingFluid.Properties, F> sourceFactory,
            Function<ForgeFlowingFluid.Properties, F> flowingFactory) {
        final int index = name.lastIndexOf('/');
        final String flowingName = index == -1 ? "flowing_" + name
                : name.substring(0, index) + "/flowing_" + name.substring(index + 1);

        return RegistrationHelpers.registerFluid(TFCFluids.FLUID_TYPES, FLUIDS, name, name, flowingName, builder,
                () -> new ExtendedFluidType(typeProperties, clientProperties), sourceFactory, flowingFactory);
    }
}
