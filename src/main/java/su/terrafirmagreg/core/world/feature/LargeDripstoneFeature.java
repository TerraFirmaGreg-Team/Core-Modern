package su.terrafirmagreg.core.world.feature;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.RockData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.phys.Vec3;

/**
 * Mostly copied from here (which itself is mostly copied from vanilla), but with TFC rock layers added:
 * https://github.com/Apollounknowndev/lithostitched/blob/1.20.1/common/src/main/java/dev/worldgen/lithostitched/worldgen/feature/LargeDripstoneFeature.java
 */

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfig> {

    public LargeDripstoneFeature(Codec<LargeDripstoneConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeDripstoneConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        LargeDripstoneConfig config = context.config();
        RandomSource random = context.random();

        if (!EnvironmentHelpers.isWorldgenReplaceable(level, origin)) {
            return false;
        } else {
            Optional<Column> column = Column.scan(level, origin, config.floorToCeilingSearchRange(), DripstoneUtils::isEmptyOrWater, state -> isReplaceableOrLava(state, config.replaceableBlocks()));
            if (column.isPresent() && column.get() instanceof Column.Range range) {
                if (range.height() < 4) {
                    return false;
                } else {
                    final ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
                    final RockData rockData = provider.get(context.level(), origin).getRockData();

                    int unclampedRadius = (int) (range.height() * config.maxColumnRadiusToCaveHeightRatio());
                    int maxRadius = Mth.clamp(unclampedRadius, config.columnRadius().getMinValue(), config.columnRadius().getMaxValue());

                    int radius = Mth.randomBetweenInclusive(random, config.columnRadius().getMinValue(), maxRadius);

                    LargeDripstoneFeature.LargeDripstone ceilingDripstone = makeDripstone(
                            rockData, random, origin.atY(range.ceiling() - 1), false, radius, config.stalactiteBluntness(), config.heightScale());
                    LargeDripstoneFeature.LargeDripstone floorDripstone = makeDripstone(
                            rockData, random, origin.atY(range.floor() + 1), true, radius, config.stalagmiteBluntness(), config.heightScale());

                    LargeDripstoneFeature.WindOffsetter windOffsetter;

                    if (ceilingDripstone.isSuitableForWind(config) && floorDripstone.isSuitableForWind(config)) {
                        windOffsetter = new LargeDripstoneFeature.WindOffsetter(origin.getY(), random, config.windSpeed());
                    } else {
                        windOffsetter = LargeDripstoneFeature.WindOffsetter.noWind();
                    }

                    boolean $$14 = ceilingDripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, windOffsetter);
                    boolean $$15 = floorDripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, windOffsetter);
                    if ($$14) {
                        ceilingDripstone.placeBlocks(level, random, windOffsetter);
                    }

                    if ($$15) {
                        floorDripstone.placeBlocks(level, random, windOffsetter);
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }

    public static boolean isReplaceableOrLava(BlockState state, HolderSet<Block> replaceable) {
        return state.is(replaceable) || Helpers.isBlock(state, Blocks.LAVA);
    }

    private static LargeDripstoneFeature.LargeDripstone makeDripstone(RockData rockData, RandomSource random, BlockPos root, boolean pointingUp, int radius, FloatProvider bluntness,
            FloatProvider scale) {
        return new LargeDripstoneFeature.LargeDripstone(rockData, root, pointingUp, radius, bluntness.sample(random), scale.sample(random));
    }

    static final class LargeDripstone {
        private final RockData rockData;
        private BlockPos root;
        private final boolean pointingUp;
        private int radius;
        private final double bluntness;
        private final double scale;

        LargeDripstone(RockData rockData, BlockPos root, boolean pointingUp, int radius, double bluntness, double scale) {
            this.rockData = rockData;
            this.root = root;
            this.pointingUp = pointingUp;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0F);
        }

        boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel $$0, LargeDripstoneFeature.WindOffsetter $$1) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos $$2 = this.root.mutable();
                int $$3 = Math.min(10, this.getHeight());

                for (int $$4 = 0; $$4 < $$3; $$4++) {
                    if ($$0.getBlockState($$2).is(Blocks.LAVA)) {
                        return false;
                    }

                    if (isCircleMostlyEmbeddedInStone($$0, $$1.offset($$2), this.radius)) {
                        this.root = $$2;
                        return true;
                    }

                    $$2.move(this.pointingUp ? Direction.DOWN : Direction.UP);
                }

                this.radius /= 2;
            }

            return false;
        }

        private int getHeightAtRadius(float $$0) {
            return (int) getDripstoneHeight($$0, this.radius, this.scale, this.bluntness);
        }

        void placeBlocks(WorldGenLevel level, RandomSource random, LargeDripstoneFeature.WindOffsetter windOffsetter) {
            for (int x = -this.radius; x <= this.radius; x++) {
                for (int z = -this.radius; z <= this.radius; z++) {

                    float rootDistance = Mth.sqrt((float) (x * x + z * z));

                    if (!(rootDistance > (float) this.radius)) {
                        int height = this.getHeightAtRadius(rootDistance);
                        if (height > 0) {
                            if (random.nextFloat() < 0.2) {
                                height = (int) (height * Mth.randomBetween(random, 0.8F, 1.0F));
                            }

                            BlockPos.MutableBlockPos pos = this.root.offset(x, 0, z).mutable();
                            boolean placedBlock = false;
                            int maxY = this.pointingUp ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) : Integer.MAX_VALUE;

                            for (int $$10 = 0; $$10 < height && pos.getY() < maxY; $$10++) {
                                BlockPos dripstonePos = windOffsetter.offset(pos);
                                if (EnvironmentHelpers.isWorldgenReplaceable(level, dripstonePos)) {
                                    placedBlock = true;
                                    level.setBlock(dripstonePos, rockData.getRock(dripstonePos).raw().defaultBlockState(), 2);
                                } else if (placedBlock && level.getBlockState(dripstonePos).is(TFCTags.Blocks.CAN_CARVE)) {
                                    break;
                                }

                                pos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                            }
                        }
                    }
                }
            }
        }

        boolean isSuitableForWind(LargeDripstoneConfig $$0) {
            return this.radius >= $$0.minRadiusForWind() && this.bluntness >= (double) $$0.minBluntnessForWind();
        }

        static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel $$0, BlockPos $$1, int $$2) {
            if (EnvironmentHelpers.isWorldgenReplaceable($$0, $$1)) {
                return false;
            } else {
                float $$3 = 6.0F;
                float $$4 = 6.0F / (float) $$2;

                for (float $$5 = 0.0F; $$5 < (float) (Math.PI * 2); $$5 += $$4) {
                    int $$6 = (int) (Mth.cos($$5) * (float) $$2);
                    int $$7 = (int) (Mth.sin($$5) * (float) $$2);
                    if (EnvironmentHelpers.isWorldgenReplaceable($$0, $$1.offset($$6, 0, $$7))) {
                        return false;
                    }
                }

                return true;
            }
        }

        static double getDripstoneHeight(double $$0, double $$1, double $$2, double $$3) {
            if ($$0 < $$3) {
                $$0 = $$3;
            }

            double $$4 = 0.384;
            double $$5 = $$0 / $$1 * 0.384;
            double $$6 = 0.75 * Math.pow($$5, 1.3333333333333333);
            double $$7 = Math.pow($$5, 0.6666666666666666);
            double $$8 = 0.3333333333333333 * Math.log($$5);
            double $$9 = $$2 * ($$6 - $$7 - $$8);
            $$9 = Math.max($$9, 0.0);
            return $$9 / 0.384 * $$1;
        }
    }

    static final class WindOffsetter {
        private final int originY;
        private final Vec3 windSpeed;

        WindOffsetter(int y, RandomSource random, FloatProvider $$2) {
            this.originY = y;
            float $$3 = $$2.sample(random);
            float $$4 = Mth.randomBetween(random, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3(Mth.cos($$4) * $$3, 0.0, Mth.sin($$4) * $$3);
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        static LargeDripstoneFeature.WindOffsetter noWind() {
            return new LargeDripstoneFeature.WindOffsetter();
        }

        BlockPos offset(BlockPos $$0) {
            if (this.windSpeed == null) {
                return $$0;
            } else {
                int $$1 = this.originY - $$0.getY();
                Vec3 $$2 = this.windSpeed.scale($$1);
                return $$0.offset(Mth.floor($$2.x), 0, Mth.floor($$2.z));
            }
        }
    }
}
