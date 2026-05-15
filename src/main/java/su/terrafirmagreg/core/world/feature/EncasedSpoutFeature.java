package su.terrafirmagreg.core.world.feature;

import org.apache.commons.lang3.mutable.MutableInt;

import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;
import com.mojang.serialization.Codec;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/**
 * A modified version of GregTech's FluidSproutFeature that encases the spout with an appropriate
 * igneous rock
 */

public class EncasedSpoutFeature extends Feature<FluidSproutConfiguration> {

    public EncasedSpoutFeature(Codec<FluidSproutConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FluidSproutConfiguration> context) {
        RandomSource random = context.random();
        BlockPos blockpos = context.origin();
        WorldGenLevel level = context.level();
        FluidSproutConfiguration config = context.config();

        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final ChunkData data = provider.get(context.level(), blockpos);
        final RockSettings rock = data.getRockData().getRock(blockpos.getX(), -50, blockpos.getZ());
        final Block raw = rock.raw();

        // Find the appropriate intrusive igneous magma block, otherwise use basalt as a fallback
        Block magma = TFCBlocks.MAGMA_BLOCKS.get(Rock.BASALT).get();
        if (raw == TFCBlocks.ROCK_BLOCKS.get(Rock.GRANITE).get(Rock.BlockType.RAW).get())
            magma = TFCBlocks.MAGMA_BLOCKS.get(Rock.GRANITE).get();
        else if (raw == TFCBlocks.ROCK_BLOCKS.get(Rock.DIORITE).get(Rock.BlockType.RAW).get())
            magma = TFCBlocks.MAGMA_BLOCKS.get(Rock.DIORITE).get();
        else if (raw == TFCBlocks.ROCK_BLOCKS.get(Rock.GABBRO).get(Rock.BlockType.RAW).get())
            magma = TFCBlocks.MAGMA_BLOCKS.get(Rock.GABBRO).get();
        final BlockState magmaBlockState = magma.defaultBlockState();

        final BlockState fluidBlockState = config.fluid().defaultFluidState().createLegacyBlock();

        MutableInt placedAmount = new MutableInt(0);
        int size = config.size().sample(random);
        int radius = Mth.ceil(size / 2f);
        int x0 = blockpos.getX() - radius;
        int y0 = blockpos.getY() - radius;
        int z0 = blockpos.getZ() - radius;
        int width = size + 3;
        int length = size + 1;
        int height = size + 3;

        if (config.fluid().isSame(Fluids.EMPTY)) {
            return false;
        }
        int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos.getX(), blockpos.getZ());
        if (blockpos.getY() >= surfaceHeight) {
            return false;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        try (BulkSectionAccess bulkSectionAccess = new BulkSectionAccess(level)) {
            // Sphere
            for (int x = 0; x < width; x++) {
                float dx = x * 2f / width - 1;
                if (dx * dx > 1)
                    continue;

                for (int y = 0; y < height; y++) {
                    float dy = y * 2f / height - 1;
                    if (dx * dx + dy * dy > 1)
                        continue;
                    if (level.isOutsideBuildHeight(y0 + y))
                        continue;

                    for (int z = 0; z < length; z++) {
                        float dz = z * 2f / length - 1;
                        float distFromCenter = dx * dx + dy * dy + dz * dz;
                        if (distFromCenter > 1)
                            continue;

                        final int currentX = x0 + x;
                        final int currentY = y0 + y;
                        final int currentZ = z0 + z;

                        // Decide what to place
                        BlockState state = fluidBlockState;
                        if (distFromCenter > 0.75) {
                            state = magmaBlockState;
                        }

                        setBlock(mutablePos, currentX, currentY, currentZ, bulkSectionAccess, level, state, placedAmount);
                    }
                }
            }

            int topOfSphere = blockpos.getY() + radius;

            // Pipe
            if (random.nextFloat() <= config.sproutChance()) {
                int currentX = blockpos.getX();
                int currentZ = blockpos.getZ();

                int springHeight = surfaceHeight + config.surfaceOffset().sample(random);
                for (int currentY = blockpos.getY(); currentY <= springHeight; ++currentY) {
                    setBlock(mutablePos, currentX, currentY, currentZ,
                            bulkSectionAccess, level,
                            fluidBlockState, placedAmount);
                    if (currentY <= surfaceHeight) {
                        // fluid in a + shape
                        setBlock(mutablePos, currentX + 1, currentY, currentZ, bulkSectionAccess, level, fluidBlockState, placedAmount);
                        setBlock(mutablePos, currentX - 1, currentY, currentZ, bulkSectionAccess, level, fluidBlockState, placedAmount);
                        setBlock(mutablePos, currentX, currentY, currentZ + 1, bulkSectionAccess, level, fluidBlockState, placedAmount);
                        setBlock(mutablePos, currentX, currentY, currentZ - 1, bulkSectionAccess, level, fluidBlockState, placedAmount);
                        // magma
                        var edgeBlockState = currentY < surfaceHeight && currentY > topOfSphere ? magmaBlockState : fluidBlockState;
                        setBlock(mutablePos, currentX + 1, currentY, currentZ + 1, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX + 1, currentY, currentZ - 1, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX - 1, currentY, currentZ - 1, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX - 1, currentY, currentZ + 1, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX + 2, currentY, currentZ, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX - 2, currentY, currentZ, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX, currentY, currentZ + 2, bulkSectionAccess, level, edgeBlockState, placedAmount);
                        setBlock(mutablePos, currentX, currentY, currentZ - 2, bulkSectionAccess, level, edgeBlockState, placedAmount);
                    }
                }
            }
        }

        return placedAmount.intValue() > 0;
    }

    public void setBlock(BlockPos.MutableBlockPos mutablePos, int currentX, int currentY, int currentZ,
            BulkSectionAccess access, WorldGenLevel level, BlockState state, MutableInt placedAmount) {

        mutablePos.set(currentX, currentY, currentZ);
        if (!level.ensureCanWrite(mutablePos))
            return;

        LevelChunkSection levelChunkSection = access.getSection(mutablePos);
        if (levelChunkSection == null)
            return;

        int sectionX = SectionPos.sectionRelative(currentX);
        int sectionY = SectionPos.sectionRelative(currentY);
        int sectionZ = SectionPos.sectionRelative(currentZ);

        // Don't replace bedrock
        if (levelChunkSection.getBlockState(sectionX, sectionY, sectionZ).is(BlockTags.FEATURES_CANNOT_REPLACE))
            return;

        levelChunkSection.setBlockState(sectionX, sectionY, sectionZ, state, false);
        level.getChunk(mutablePos).markPosForPostprocessing(mutablePos);
        placedAmount.add(1);
    }
}
