/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.world.feature.cave;

import com.mojang.serialization.Codec;
import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.tags.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.RockSpikeBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraftforge.common.Tags;
import su.terrafirmagreg.core.common.data.TFGBlockProperties;
import su.terrafirmagreg.core.common.data.TFGFluids;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks;

public class CaveSpikesFeature extends Feature<NoneFeatureConfiguration>
{
    public CaveSpikesFeature(Codec<NoneFeatureConfiguration> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        final WorldGenLevel level = context.level();
        final BlockPos pos = context.origin();
        final var random = context.random();
        final RockLayerSettings rockSettings = ((ChunkGeneratorExtension) context.chunkGenerator()).rockLayerSettings();

        // The direction that the spike is pointed
        Direction direction = random.nextBoolean() ? Direction.UP : Direction.DOWN;
        BlockState wallState = level.getBlockState(pos.relative(direction.getOpposite()));
        RockSettings wallRock = rockSettings.getRock(wallState.getBlock());
        if (wallRock != null && wallRock.isRawOrHardened(wallState))
        {
            placeIfPresent(level, pos, direction, random, wallRock);
        }
        else
        {
            // Switch directions and try again
            direction = direction.getOpposite();
            wallState = level.getBlockState(pos.relative(direction));
            wallRock = rockSettings.getRock(wallState.getBlock());
            if (wallRock != null && wallRock.isRawOrHardened(wallState))
            {
                placeIfPresent(level, pos, direction, random, wallRock);
            }
        }
        return true;
    }

    protected void place(WorldGenLevel level, BlockPos pos, BlockState spike, BlockState raw, Direction direction, RandomSource random)
    {
        placeSmallSpike(level, pos, spike, raw, direction, random);
    }

    protected void placeSmallSpike(WorldGenLevel level, BlockPos pos, BlockState spike, BlockState raw, Direction direction, RandomSource random)
    {
        placeSmallSpike(level, pos, spike, raw, direction, random.nextFloat());
    }

    protected void placeSmallSpike(WorldGenLevel level, BlockPos pos, BlockState spike, BlockState raw, Direction direction, float sizeWeight)
    {
		TagKey<Block> blockTag;

		var dim = level.getLevel().dimension();
		if (dim == Level.OVERWORLD) {
			blockTag = BlockTags.BASE_STONE_OVERWORLD;
		} else if (dim == Level.NETHER) {
			blockTag = BlockTags.BASE_STONE_NETHER;
		} else if (dim == Planet.MARS) {
			blockTag = ModBlockTags.MARS_STONE_REPLACEABLES;
		} else if (dim == Planet.VENUS) {
			blockTag = ModBlockTags.VENUS_STONE_REPLACEABLES;
		} else if (dim == Planet.MERCURY) {
			blockTag = ModBlockTags.MERCURY_STONE_REPLACEABLES;
		} else if (dim == Planet.GLACIO) {
			blockTag = ModBlockTags.GLACIO_STONE_REPLACEABLES;
		} else {
			blockTag = Tags.Blocks.STONE;
		}

        // Replace the block above from raw -> hardened, if necessary
        final BlockPos above = pos.above();
        final BlockState stateAbove = level.getBlockState(pos.above());
        if (Helpers.isBlock(stateAbove, blockTag))
        {
            level.setBlock(above, raw, 2);
        }

        // Build a spike starting downwards from the target block
        if (sizeWeight < 0.2f)
        {
            replaceBlock(level, pos, spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.MIDDLE));
            replaceBlock(level, pos.relative(direction, 1), spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.TIP));
        }
        else if (sizeWeight < 0.7f)
        {
            replaceBlock(level, pos, spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.BASE));
            replaceBlock(level, pos.relative(direction, 1), spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.MIDDLE));
            replaceBlock(level, pos.relative(direction, 2), spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.TIP));
        }
        else
        {
            replaceBlockWithoutFluid(level, pos, raw);
            replaceBlock(level, pos.relative(direction, 1), spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.BASE));
            replaceBlock(level, pos.relative(direction, 2), spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.MIDDLE));
            replaceBlock(level, pos.relative(direction, 3), spike.setValue(RockSpikeBlock.PART, RockSpikeBlock.Part.TIP));
        }
    }

    protected void replaceBlock(WorldGenLevel level, BlockPos pos, BlockState state)
    {
		final Block block = level.getBlockState(pos).getBlock();
		if (block == Blocks.AIR || block == Blocks.CAVE_AIR) {
			level.setBlock(pos, state, Block.UPDATE_ALL);
		} else if (block == Blocks.WATER || block == TFCBlocks.RIVER_WATER.get()) {
			level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(Fluids.WATER)), Block.UPDATE_ALL);
		} else if (block == Blocks.LAVA) {
			level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(Fluids.LAVA)), Block.UPDATE_ALL);
		} else if (block == TFGBlocks.MARS_WATER.get()) {
			level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(TFGFluids.MARS_WATER.getSource())), Block.UPDATE_ALL);
		} else if (block == TFGBlocks.SULFUR_FUMES.get()) {
			level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(TFGFluids.SULFUR_FUMES.getSource())), Block.UPDATE_ALL);
		} else if (block == TFGBlocks.GEYSER_SLURRY.get()) {
			level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(TFGFluids.GEYSER_SLURRY.getSource())), Block.UPDATE_ALL);
		} else if (block == TFGBlocks.MUDDY_WATER.get()) {
			level.setBlock(pos, state.setValue(TFGBlockProperties.SPACE_WATER_AND_LAVA, TFGBlockProperties.SPACE_WATER_AND_LAVA.keyFor(TFGFluids.MUDDY_WATER.getSource())), Block.UPDATE_ALL);
		}
    }

    protected void replaceBlockWithoutFluid(WorldGenLevel level, BlockPos pos, BlockState state)
    {
		final Block block = level.getBlockState(pos).getBlock();
		if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.WATER || block == TFCBlocks.RIVER_WATER.get()
				|| block == Blocks.LAVA || block == TFGBlocks.MARS_WATER.get() || block == TFGBlocks.SULFUR_FUMES.get()
				|| block == TFGBlocks.GEYSER_SLURRY.get() || block == TFGBlocks.MUDDY_WATER.get()) {
			level.setBlock(pos, state, Block.UPDATE_ALL);
		}
    }

    private void placeIfPresent(WorldGenLevel level, BlockPos pos, Direction direction, RandomSource random, RockSettings wallRock)
    {
        wallRock.spike().ifPresent(spike -> place(level, pos, spike.defaultBlockState(), wallRock.hardened().defaultBlockState(), direction, random));
    }
}