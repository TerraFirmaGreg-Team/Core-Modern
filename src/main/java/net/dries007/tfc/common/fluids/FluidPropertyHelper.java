package net.dries007.tfc.common.fluids;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import su.terrafirmagreg.core.common.data.TFGBlockProperties;

import java.util.Optional;

public class FluidPropertyHelper {
	static final public FluidProperty[] fluidProperties = {
		TFCBlockStateProperties.WATER,
		TFCBlockStateProperties.ALL_WATER,
		TFCBlockStateProperties.WATER_AND_LAVA,
		TFCBlockStateProperties.SALT_WATER,
		TFCBlockStateProperties.FRESH_WATER,
		TFGBlockProperties.SPACE_WATER,
		TFGBlockProperties.SPACE_WATER_AND_LAVA
	};

	public static Optional<FluidProperty> getFluidProperty(BlockState state)
	{
		for (FluidProperty property : FluidPropertyHelper.fluidProperties)
		{ if (state.hasProperty(property)) { return Optional.of(property); } }
		return Optional.empty();
	}

	public static Optional<Fluid> getFluid(BlockState state)
	{
		return getFluidProperty(state).map(property -> state.getValue(property).getFluid());
	}

	public static boolean hasProperty(BlockState state)
	{
		return getFluidProperty(state).isPresent();
	}

	public static boolean canContain(BlockState state, Fluid fluid)
	{
		return getFluidProperty(state).map(property -> property.canContain(fluid)).orElse(false);
	}

	public static BlockState getFluidBlockState(BlockState state)
	{
		return getFluid(state).map(fluid -> fluid.defaultFluidState().createLegacyBlock()).orElse(state);
	}

	public static BlockState set(BlockState state, Fluid fluid)
	{
		return getFluidProperty(state).map(property -> state.setValue(property, property.keyForOrEmpty(fluid))).orElse(state);
	}

	public static BlockState empty(BlockState state)
	{
		return set(state, Fluids.EMPTY);
	}

	public static boolean needScheduledTick(BlockState state) {
		return state.is(TFCTags.Blocks.AQUEDUCTS);
	}

	public static boolean canDrain(BlockState state)
	{
		return FluidPropertyHelper.getFluid(state).map(fluid -> !fluid.isSame(Fluids.EMPTY) && !state.is(TFCTags.Blocks.AQUEDUCTS)).orElse(false);
	}

	public static boolean canFill(BlockState state, Fluid fluid)
	{
		return FluidPropertyHelper.canContain(state, fluid) && !state.is(TFCTags.Blocks.AQUEDUCTS);
	}

	public static boolean canOpenPipe(BlockState state)
	{
		return FluidPropertyHelper.hasProperty(state) && !state.is(TFCTags.Blocks.PLANTS);
	}
}
