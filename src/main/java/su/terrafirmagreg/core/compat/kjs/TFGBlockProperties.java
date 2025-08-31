package su.terrafirmagreg.core.compat.kjs;

import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.world.level.material.Fluids;
import su.terrafirmagreg.core.common.data.TFGFluids;

import java.util.stream.Stream;

public class TFGBlockProperties {

	public static final FluidProperty SPACE_WATER = FluidProperty.create("fluid", Stream.of(Fluids.EMPTY, Fluids.WATER, TFCFluids.SALT_WATER, TFCFluids.SPRING_WATER, TFGFluids.MARS_WATER));
}
