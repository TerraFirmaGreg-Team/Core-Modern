package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

// A modified version of vanilla's LakeFeature
@SuppressWarnings({ "deprecation", "FieldCanBeLocal" })
public class LargeLakeFeature extends Feature<LargeLakeConfig> {
    private final int maxSizeX = 24;
    private final int maxSizeY = 12;
    private final int maxSizeZ = 24;
    private final int depth = 6;

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public LargeLakeFeature(Codec<LargeLakeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeLakeConfig> context) {
        BlockPos pos = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        LargeLakeConfig config = context.config();

        if (pos.getY() <= level.getMinBuildHeight() + depth) {
            return false;
        } else {
            pos = pos.below(depth);
            boolean[] array = new boolean[maxSizeX * maxSizeY * maxSizeZ];
            int i = random.nextInt(depth) + depth;

            for (int j = 0; j < i; ++j) {
                double ax = random.nextDouble() * 8.0 + 4.0;
                double ay = random.nextDouble() * 6.0 + 3.0;
                double az = random.nextDouble() * 8.0 + 4.0;
                double bx = random.nextDouble() * ((double) maxSizeX - ax - 2.0) + 1.0 + ax / 2.0;
                double by = random.nextDouble() * ((double) maxSizeY - ay - 2.0) + 1.0 + ay / 2.0;
                double bz = random.nextDouble() * ((double) maxSizeZ - az - 2.0) + 1.0 + az / 2.0;

                for (int x = 1; x < maxSizeX - 1; ++x) {
                    for (int z = 1; z < maxSizeZ - 1; ++z) {
                        for (int y = 1; y < maxSizeY - 1; ++y) {
                            double cx = ((double) x - bx) / (ax / 2.0);
                            double cy = ((double) y - by) / (ay / 2.0);
                            double cz = ((double) z - bz) / (az / 2.0);
                            double d = cx * cx + cy * cy + cz * cz;
                            if (d < 1.0) {
                                array[(x * maxSizeX + z) * maxSizeY + y] = true;
                            }
                        }
                    }
                }
            }

            // Run some tests to see if the feature touches enough ground
            BlockState fluidState = config.fluid().getState(random, pos);
            for (int x = 0; x < maxSizeX; ++x) {
                for (int z = 0; z < maxSizeZ; ++z) {
                    for (int y = 0; y < maxSizeY; ++y) {
                        boolean flag = !array[(x * maxSizeX + z) * maxSizeY + y]
                                && (x < maxSizeX - 1
                                        && array[((x + 1) * maxSizeX + z) * maxSizeY + y]
                                        || x > 0 && array[((x - 1) * maxSizeX + z) * maxSizeY + y]
                                        || z < maxSizeZ - 1 && array[(x * maxSizeX + z + 1) * maxSizeY + y]
                                        || z > 0 && array[(x * maxSizeX + (z - 1)) * maxSizeY + y]
                                        || y < maxSizeY - 1 && array[(x * maxSizeX + z) * maxSizeY + y + 1]
                                        || y > 0 && array[(x * maxSizeX + z) * maxSizeY + (y - 1)]);
                        if (flag) {
                            BlockState stateAt = level.getBlockState(pos.offset(x, y, z));
                            if (y >= depth && stateAt.liquid()) {
                                return false;
                            }

                            if (y < depth && !stateAt.isSolid() && level.getBlockState(pos.offset(x, y, z)) != fluidState) {
                                return false;
                            }
                        }
                    }
                }
            }

            final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
            final RockData rockData = provider.get(context.level(), pos).getRockData();

            for (int x = 0; x < maxSizeX; ++x) {
                for (int z = 0; z < maxSizeZ; ++z) {
                    for (int y = 0; y < maxSizeY; ++y) {
                        if (array[(x * maxSizeX + z) * maxSizeY + y]) {

                            BlockPos offsetPos = pos.offset(x, y, z);

                            if (this.canReplaceBlock(level.getBlockState(offsetPos))) {

                                boolean isAboveFluidLevel = y >= depth;
                                level.setBlock(offsetPos, isAboveFluidLevel ? AIR : fluidState, Block.UPDATE_CLIENTS);

                                if (isAboveFluidLevel) {
                                    // check if the block above needs hardening
                                    BlockPos abovePos = pos.offset(x, y + 1, z);
                                    BlockState aboveState = level.getBlockState(abovePos);
                                    if (!aboveState.isAir()) {
                                        level.setBlock(abovePos, rockData.getRock(pos).hardened().defaultBlockState(), Block.UPDATE_CLIENTS);
                                    }

                                    level.scheduleTick(offsetPos, AIR.getBlock(), 0);
                                    this.markAboveForPostProcessing(level, offsetPos);
                                }
                            }
                        }
                    }
                }
            }

            BlockState barrierState = config.barrier().getState(random, pos);
            if (barrierState.isAir()) {
                barrierState = rockData.getRock(pos).hardened().defaultBlockState();
            }

            for (int x = 0; x < maxSizeX; ++x) {
                for (int z = 0; z < maxSizeZ; ++z) {
                    for (int y = 0; y < maxSizeY; ++y) {
                        boolean flag2 = !array[(x * maxSizeX + z) * maxSizeY + y]
                                && (x < maxSizeX - 1 && array[((x + 1) * maxSizeX + z) * maxSizeY + y]
                                        || x > 0 && array[((x - 1) * maxSizeX + z) * maxSizeY + y]
                                        || z < maxSizeZ - 1 && array[(x * maxSizeX + z + 1) * maxSizeY + y]
                                        || z > 0 && array[(x * maxSizeX + (z - 1)) * maxSizeY + y]
                                        || y < maxSizeY - 1 && array[(x * maxSizeX + z) * maxSizeY + y + 1]
                                        || y > 0 && array[(x * maxSizeX + z) * maxSizeY + (y - 1)]);

                        if (flag2 && (y < depth || random.nextInt(2) != 0)) {

                            BlockState stateAt = level.getBlockState(pos.offset(x, y, z));

                            if (stateAt.isSolid() && !stateAt.is(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE)) {
                                BlockPos offsetPos = pos.offset(x, y, z);
                                level.setBlock(offsetPos, barrierState, Block.UPDATE_CLIENTS);
                                this.markAboveForPostProcessing(level, offsetPos);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean canReplaceBlock(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }
}
