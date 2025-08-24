package su.terrafirmagreg.core.common.data;

import net.dries007.tfc.common.fluids.*;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import su.terrafirmagreg.core.TFGCore;

import java.util.function.Consumer;
import java.util.function.Function;

import static net.dries007.tfc.common.fluids.TFCFluids.*;


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

		new FluidTypeClientProperties(ALPHA_MASK | 0x55d9b1, WATER_STILL, WATER_FLOW, WATER_OVERLAY, UNDERWATER_LOCATION),
		MixingFluid.Source::new,
		MixingFluid.Flowing::new
	);

	// TFC, why did you have to make this private

	private static FluidType.Properties waterLike()
	{
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

	// Registration helpers

	private static <F extends FlowingFluid> FluidRegistryObject<F> register(String name, Consumer<ForgeFlowingFluid.Properties> builder, FluidType.Properties typeProperties, FluidTypeClientProperties clientProperties, Function<ForgeFlowingFluid.Properties, F> sourceFactory, Function<ForgeFlowingFluid.Properties, F> flowingFactory)
	{
		final int index = name.lastIndexOf('/');
		final String flowingName = index == -1 ? "flowing_" + name : name.substring(0, index) + "/flowing_" + name.substring(index + 1);

		return RegistrationHelpers.registerFluid(TFCFluids.FLUID_TYPES, FLUIDS, name, name, flowingName, builder, () -> new ExtendedFluidType(typeProperties, clientProperties), sourceFactory, flowingFactory);
	}
}
