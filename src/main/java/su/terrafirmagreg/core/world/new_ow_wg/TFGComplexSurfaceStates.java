package su.terrafirmagreg.core.world.new_ow_wg;

import java.util.function.Supplier;

import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TFGComplexSurfaceStates {
    private static TFGComplexSurfaceStates instance = null;

    public static TFGComplexSurfaceStates INSTANCE() {
        if (instance == null) {
            instance = new TFGComplexSurfaceStates();
        }
        return instance;
    }

    public final SurfaceState TOP_GRASS_TO_SAND;
    public final SurfaceState MID_DIRT_TO_SAND;
    public final SurfaceState VOLCANIC_TOP_GRASS_TO_LOCAL_GRAVEL;
    public final SurfaceState VOLCANIC_MID_DIRT_TO_LOCAL_GRAVEL;

    public final SurfaceState UNDER_GRAVEL;

    public final SurfaceState VOLCANIC_SHORE_SAND;
	public final SurfaceState SHORE_SAND;
	public final SurfaceState RARE_SHORE_SAND;
	public final SurfaceState SHORE_SURFACE;
	public final SurfaceState RARE_SHORE_SANDSTONE;
	public final SurfaceState VOLCANIC_SHORE_SANDSTONE;
	public final SurfaceState SHORE_SANDSTONE;
	public final SurfaceState SHORE_UNDERLAYER;


	private TFGComplexSurfaceStates() {
        TFGSimpleSurfaceStates simpleStates = TFGSimpleSurfaceStates.INSTANCE();

        TOP_GRASS_TO_SAND = TFGSoilSurfaceState.buildSurfaceType(SoilBlockType.GRASS, simpleStates.SAND);
        MID_DIRT_TO_SAND = TFGSoilSurfaceState.buildMidType(SoilBlockType.DIRT, simpleStates.SAND);
        VOLCANIC_TOP_GRASS_TO_LOCAL_GRAVEL = TFGSoilSurfaceState.buildVolcanicSurfaceType(SoilBlockType.GRASS, SurfaceStates.GRAVEL);
        VOLCANIC_MID_DIRT_TO_LOCAL_GRAVEL = TFGSoilSurfaceState.buildVolcanicMidType(SoilBlockType.DIRT, SurfaceStates.GRAVEL);

        UNDER_GRAVEL = TFGSoilSurfaceState.buildUnderType();

		// Similar to rare shore sand, but forces volcanic types and green sand is rarer
        VOLCANIC_SHORE_SAND = new SurfaceState() {
            private final Supplier<Block> greenSand = TFCBlocks.SAND.get(SandBlockType.GREEN);
            private final Supplier<Block> blackSand = TFCBlocks.SAND.get(SandBlockType.BLACK);

            @Override
            public BlockState getState(SurfaceBuilderContext context) {
                if (context.rainfall() > 420f) {
                    return greenSand.get().defaultBlockState();
                }
                return blackSand.get().defaultBlockState();
            }
        };

		// Selects between three common sand types or the rare type based on absolute-value noise map
		SHORE_SAND = new SurfaceState()
		{
			private final Supplier<Block> redSand = TFCBlocks.SAND.get(SandBlockType.RED);
			private final Supplier<Block> brownSand = TFCBlocks.SAND.get(SandBlockType.BROWN);
			private final Supplier<Block> yellowSand = TFCBlocks.SAND.get(SandBlockType.YELLOW);

			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				final BlockPos pos = context.pos();
				final int x = pos.getX();
				final int z = pos.getZ();
				final float variantNoiseValue = (float) sandVariantNoise().noise(x, z);
				if (variantNoiseValue > 0.55) return RARE_SHORE_SAND.getState(context);
				else if (variantNoiseValue > 0.2) return yellowSand.get().defaultBlockState();
				else if (variantNoiseValue > 0.1) return brownSand.get().defaultBlockState();
				else return redSand.get().defaultBlockState();
			}
		};

		// Selected rarely by the shore sand SurfaceState, this defaults to white sand unless certain climatic requirements are met
		RARE_SHORE_SAND = new SurfaceState()
		{
			private final Supplier<Block> pinkSand = TFCBlocks.SAND.get(SandBlockType.PINK);
			private final Supplier<Block> greenSand = TFCBlocks.SAND.get(SandBlockType.GREEN);
			private final Supplier<Block> blackSand = TFCBlocks.SAND.get(SandBlockType.BLACK);
			private final Supplier<Block> whiteSand = TFCBlocks.SAND.get(SandBlockType.WHITE);

			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				if (context.rainfall() > 300f && context.averageTemperature() > 15f)
				{
					return pinkSand.get().defaultBlockState();
				}
				else if ((context.getSeaLevelRock().mafic().isPresent() ? context.getSeaLevelRock().mafic().get() : false))
				{
					if (context.rainfall() > 300f)
					{
						return greenSand.get().defaultBlockState();
					}
					return blackSand.get().defaultBlockState();
				}
				else
				{
					return whiteSand.get().defaultBlockState();
				}
			}
		};

		// Selects between placing shore sands or gravel, with gravel more common in cold climates and sand more common in warm climates
		SHORE_SURFACE = new SurfaceState()
		{
			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				final BlockPos pos = context.pos();
				final int x = pos.getX();
				final int z = pos.getZ();
				final float variantNoiseValue = (float) sandGravelBeachNoise().noise(x, z);
				final double gravelCutoff = Mth.clampedMap(context.averageTemperature(), -15, 25, -0.7, 0.7);
				return (variantNoiseValue > gravelCutoff ? SurfaceStates.GRAVEL : SHORE_SAND).getState(context);
			}
		};

		RARE_SHORE_SANDSTONE = new SurfaceState()
		{
			private final Supplier<Block> pinkSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.PINK).get(SandstoneBlockType.RAW);
			private final Supplier<Block> greenSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.GREEN).get(SandstoneBlockType.RAW);
			private final Supplier<Block> blackSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.BLACK).get(SandstoneBlockType.RAW);
			private final Supplier<Block> whiteSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.WHITE).get(SandstoneBlockType.RAW);

			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				if (context.rainfall() > 300f && context.averageTemperature() > 15f)
				{
					return pinkSandstone.get().defaultBlockState();
				}
				else if ((context.getSeaLevelRock().mafic().isPresent() ? context.getSeaLevelRock().mafic().get() : false))
				{
					if (context.rainfall() > 300f)
					{
						return greenSandstone.get().defaultBlockState();
					}
					return blackSandstone.get().defaultBlockState();
				}
				else
				{
					return whiteSandstone.get().defaultBlockState();
				}
			}
		};

		VOLCANIC_SHORE_SANDSTONE = new SurfaceState()
		{
			private final Supplier<Block> greenSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.GREEN).get(SandstoneBlockType.RAW);
			private final Supplier<Block> blackSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.BLACK).get(SandstoneBlockType.RAW);

			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				if (context.rainfall() > 420f)
				{
					return greenSandstone.get().defaultBlockState();
				}
				return blackSandstone.get().defaultBlockState();
			}
		};

		SHORE_SANDSTONE = new SurfaceState()
		{
			private final Supplier<Block> redSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.RED).get(SandstoneBlockType.RAW);
			private final Supplier<Block> brownSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.BROWN).get(SandstoneBlockType.RAW);
			private final Supplier<Block> yellowSandstone = TFCBlocks.SANDSTONE.get(SandBlockType.YELLOW).get(SandstoneBlockType.RAW);

			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				final BlockPos pos = context.pos();
				final int x = pos.getX();
				final int z = pos.getZ();
				final float variantNoiseValue = (float) sandVariantNoise().noise(x, z);
				if (variantNoiseValue > 0.8) return RARE_SHORE_SANDSTONE.getState(context);
				else if (variantNoiseValue > 0.4) return yellowSandstone.get().defaultBlockState();
				else if (variantNoiseValue > 0.2) return redSandstone.get().defaultBlockState();
				else return brownSandstone.get().defaultBlockState();
			}
		};

		SHORE_UNDERLAYER = new SurfaceState()
		{
			@Override
			public BlockState getState(SurfaceBuilderContext context)
			{
				final BlockPos pos = context.pos();
				final int x = pos.getX();
				final int z = pos.getZ();
				final float variantNoiseValue = (float) sandGravelBeachNoise().noise(x, z);
				final double gravelCutoff = Mth.clampedMap(context.averageTemperature(), -15, 25, -0.7, 0.7);
				return (variantNoiseValue > gravelCutoff ? SurfaceStates.RAW : SHORE_SANDSTONE).getState(context);
			}
		};
    }

	public static Noise2D sandVariantNoise()
	{
		return new OpenSimplex2D(36263276L).octaves(5).spread(0.0003f).abs();
	}

	public static Noise2D sandGravelBeachNoise()
	{
		return new OpenSimplex2D(124154L).octaves(3).spread(0.00002f);
	}
}
