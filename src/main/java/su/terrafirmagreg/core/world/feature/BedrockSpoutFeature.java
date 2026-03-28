package su.terrafirmagreg.core.world.feature;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.mojang.serialization.Codec;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BedrockSpoutFeature extends Feature<BedrockSpoutConfig> {

    public BedrockSpoutFeature(Codec<BedrockSpoutConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BedrockSpoutConfig> context) {
        final RandomSource random = context.random();
        final BlockPos blockpos = context.origin();
        final WorldGenLevel level = context.level();
        final BedrockSpoutConfig config = context.config();

        final var savedData = BedrockFluidVeinSavedData.getOrCreate(level.getLevel());
        final ChunkPos chunkPos = new ChunkPos(blockpos);
        final var vein = savedData.getFluidVeinWorldEntry(chunkPos.x, chunkPos.z).getVein();

        if (vein == null)
            return false;

        final var fluid = vein.getStoredFluid().get();

        if (fluid != GTMaterials.Oil.getFluid()
                && fluid != GTMaterials.RawOil.getFluid()
                && fluid != GTMaterials.OilLight.getFluid()
                && fluid != GTMaterials.OilHeavy.getFluid())
            return false;

        final int surfaceHeight = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos.getX(), blockpos.getZ()) - 1;
        if (blockpos.getY() >= surfaceHeight)
            return false;

        final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
        final ChunkData data = provider.get(level, blockpos);
        final RockSettings rock = data.getRockData().getRock(blockpos.getX(), -50, blockpos.getZ());

        final BlockState rockBlockState = rock.raw().defaultBlockState();
        final BlockState fluidBlockState = fluid.defaultFluidState().createLegacyBlock();

        int size = config.size().sample(random);
        int radius = Mth.ceil(size / 2f);
        int x0 = blockpos.getX() - radius;
        int y0 = blockpos.getY() - radius;
        int z0 = blockpos.getZ() - radius;
        int width = size + 3;
        int height = size + 3;
        int length = size + 1;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int placedAmount = 0;

        // Sphère
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

                    BlockState state = distFromCenter > 0.75 ? rockBlockState : fluidBlockState;
                    mutablePos.set(x0 + x, y0 + y, z0 + z);
                    if (!level.isOutsideBuildHeight(mutablePos)) {
                        level.getChunk(mutablePos).setBlockState(mutablePos, state, false);
                        placedAmount++;
                    }
                }
            }
        }

        // Pipe
        int currentX = blockpos.getX();
        int currentZ = blockpos.getZ();
        int topOfSphere = blockpos.getY() + radius;
        int springHeight = surfaceHeight + config.surfaceOffset().sample(random);

        for (int currentY = blockpos.getY(); currentY <= springHeight; currentY++) {
            mutablePos.set(currentX, currentY, currentZ);
            if (!level.isOutsideBuildHeight(mutablePos)) {
                level.getChunk(mutablePos).setBlockState(mutablePos, fluidBlockState, false);
                placedAmount++;
            }

            if (currentY <= surfaceHeight) {
                var edgeState = currentY < surfaceHeight && currentY > topOfSphere ? rockBlockState : fluidBlockState;

                setIfValid(level, mutablePos, currentX + 1, currentY, currentZ, fluidBlockState);
                setIfValid(level, mutablePos, currentX - 1, currentY, currentZ, fluidBlockState);
                setIfValid(level, mutablePos, currentX, currentY, currentZ + 1, fluidBlockState);
                setIfValid(level, mutablePos, currentX, currentY, currentZ - 1, fluidBlockState);
                setIfValid(level, mutablePos, currentX + 1, currentY, currentZ + 1, edgeState);
                setIfValid(level, mutablePos, currentX + 1, currentY, currentZ - 1, edgeState);
                setIfValid(level, mutablePos, currentX - 1, currentY, currentZ - 1, edgeState);
                setIfValid(level, mutablePos, currentX - 1, currentY, currentZ + 1, edgeState);
                setIfValid(level, mutablePos, currentX + 2, currentY, currentZ, edgeState);
                setIfValid(level, mutablePos, currentX - 2, currentY, currentZ, edgeState);
                setIfValid(level, mutablePos, currentX, currentY, currentZ + 2, edgeState);
                setIfValid(level, mutablePos, currentX, currentY, currentZ - 2, edgeState);
            }
        }

        return placedAmount > 0;
    }

    private void setIfValid(WorldGenLevel level, BlockPos.MutableBlockPos mutablePos,
            int x, int y, int z, BlockState state) {
        mutablePos.set(x, y, z);
        if (!level.isOutsideBuildHeight(mutablePos))
            level.getChunk(mutablePos).setBlockState(mutablePos, state, false);
    }
}
