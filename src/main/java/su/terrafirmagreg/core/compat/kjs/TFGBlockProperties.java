package su.terrafirmagreg.core.compat.kjs;

import java.util.stream.Stream;

import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;

import su.terrafirmagreg.core.common.data.TFGFluids;

public class TFGBlockProperties {

    public static final FluidProperty SPACE_WATER = FluidProperty.create("fluid",
            Stream.of(Fluids.EMPTY, Fluids.WATER, Fluids.LAVA, TFCFluids.SALT_WATER, TFCFluids.SPRING_WATER, TFGFluids.MARS_WATER));
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 5);
}
