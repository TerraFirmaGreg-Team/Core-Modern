package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;

import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.phys.Vec3;

import earth.terrarium.adastra.common.tags.ModBlockTags;

/**
 * Mostly copied from here (which itself is mostly copied from vanilla), but with TFC rock layers added:
 * https://github.com/Apollounknowndev/lithostitched/blob/1.20.1/common/src/main/java/dev/worldgen/lithostitched/worldgen/feature/LargeDripstoneFeature.java
 */

public class SurfaceSpikeFeature extends Feature<SurfaceSpikeConfig> {

    public SurfaceSpikeFeature(Codec<SurfaceSpikeConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SurfaceSpikeConfig> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        SurfaceSpikeConfig config = context.config();
        RandomSource random = context.random();

        if (!EnvironmentHelpers.isWorldgenReplaceable(level, origin)) {
            return false;
        } else {
            final int height = (int) (config.columnRadius().sample(random) * config.heightScale().sample(random));

            int unclampedRadius = (int) (height * config.maxColumnRadiusToCaveHeightRatio());
            int maxRadius = Mth.clamp(unclampedRadius, config.columnRadius().getMinValue(), config.columnRadius().getMaxValue());

            int radius = Mth.randomBetweenInclusive(random, config.columnRadius().getMinValue(), maxRadius);

            SurfaceSpikeFeature.LargeDripstone floorDripstone = makeDripstone(
                    random, origin, radius, config.bluntness(), config.heightScale());

            SurfaceSpikeFeature.WindOffsetter windOffsetter;

            if (floorDripstone.isSuitableForWind(config)) {
                windOffsetter = new SurfaceSpikeFeature.WindOffsetter(origin.getY(), random, config.windSpeed());
            } else {
                windOffsetter = SurfaceSpikeFeature.WindOffsetter.noWind();
            }

            boolean okToPlace = floorDripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(level, windOffsetter);

            if (okToPlace) {
                floorDripstone.placeBlocks(level, random, windOffsetter, origin.getY() + height);
            }

            return true;
        }
    }

    private static SurfaceSpikeFeature.LargeDripstone makeDripstone(RandomSource random, BlockPos root, int radius, FloatProvider bluntness,
            FloatProvider scale) {
        return new SurfaceSpikeFeature.LargeDripstone(root, radius, bluntness.sample(random), scale.sample(random));
    }

    static final class LargeDripstone {
        private BlockPos root;
        private int radius;
        private final double bluntness;
        private final double scale;

        LargeDripstone(BlockPos root, int radius, double bluntness, double scale) {
            this.root = root;
            this.radius = radius;
            this.bluntness = bluntness;
            this.scale = scale;
        }

        private int getHeight() {
            return this.getHeightAtRadius(0.0F);
        }

        private boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel level, SurfaceSpikeFeature.WindOffsetter wind) {
            while (this.radius > 1) {
                BlockPos.MutableBlockPos mutablePos = this.root.mutable();
                int height = Math.min(10, this.getHeight());

                for (int y = 0; y < height; y++) {
                    if (isCircleMostlyEmbeddedInStone(level, wind.offset(mutablePos), this.radius)) {
                        this.root = mutablePos;
                        return true;
                    }

                    mutablePos.move(Direction.DOWN);
                }

                this.radius /= 2;
            }

            return false;
        }

        private int getHeightAtRadius(float distance) {
            return (int) getDripstoneHeight(distance, this.radius, this.scale, this.bluntness);
        }

        private void placeBlocks(WorldGenLevel level, RandomSource random, SurfaceSpikeFeature.WindOffsetter windOffsetter, int maxY) {
            for (int x = -this.radius; x <= this.radius; x++) {
                for (int z = -this.radius; z <= this.radius; z++) {

                    float rootDistance = Mth.sqrt((float) (x * x + z * z));

                    if (rootDistance <= (float) this.radius) {
                        int height = this.getHeightAtRadius(rootDistance);
                        if (height > 0) {
                            if (random.nextFloat() < 0.2) {
                                height = (int) (height * Mth.randomBetween(random, 0.8F, 1.0F));
                            }

                            BlockPos.MutableBlockPos pos = this.root.offset(x, 0, z).mutable();
                            boolean placedBlock = false;

                            for (int y = 0; y < height && pos.getY() < maxY; y++) {
                                BlockPos dripstonePos = windOffsetter.offset(pos);
                                if (EnvironmentHelpers.isWorldgenReplaceable(level, dripstonePos)) {
                                    level.setBlock(dripstonePos, AllPaletteStoneTypes.CRIMSITE.getBaseBlock().get().defaultBlockState(), Block.UPDATE_CLIENTS);
                                    placedBlock = true;
                                } else if (placedBlock && level.getBlockState(dripstonePos).is(ModBlockTags.VENUS_STONE_REPLACEABLES)) {
                                    break;
                                }

                                pos.move(Direction.UP);
                            }
                        }
                    }
                }
            }
        }

        private boolean isSuitableForWind(SurfaceSpikeConfig config) {
            return this.radius >= config.minRadiusForWind() && this.bluntness >= (double) config.minBluntnessForWind();
        }

        private static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel level, BlockPos pos, int radius) {
            if (EnvironmentHelpers.isWorldgenReplaceable(level, pos)) {
                return false;
            } else {
                float $$4 = 6.0F / (float) radius;

                for (float $$5 = 0.0F; $$5 < (float) (Math.PI * 2); $$5 += $$4) {
                    int $$6 = (int) (Mth.cos($$5) * (float) radius);
                    int $$7 = (int) (Mth.sin($$5) * (float) radius);
                    if (EnvironmentHelpers.isWorldgenReplaceable(level, pos.offset($$6, 0, $$7))) {
                        return false;
                    }
                }

                return true;
            }
        }

        private static double getDripstoneHeight(double distance, double radius, double scale, double bluntness) {
            if (distance < bluntness) {
                distance = bluntness;
            }

            final double $$4 = 0.384;
            double $$5 = distance / radius * $$4;
            double $$6 = 0.75 * Math.pow($$5, 1.3333333333333333);
            double $$7 = Math.pow($$5, 0.6666666666666666);
            double $$8 = 0.3333333333333333 * Math.log($$5);
            double $$9 = scale * ($$6 - $$7 - $$8);
            $$9 = Math.max($$9, 0.0);
            return $$9 / $$4 * radius;
        }
    }

    static final class WindOffsetter {
        private final int originY;
        private final Vec3 windSpeed;

        WindOffsetter(int y, RandomSource random, FloatProvider windSpeed) {
            this.originY = y;
            float wind = windSpeed.sample(random);
            float $$4 = Mth.randomBetween(random, 0.0F, (float) Math.PI);
            this.windSpeed = new Vec3(Mth.cos($$4) * wind, 0.0, Mth.sin($$4) * wind);
        }

        private WindOffsetter() {
            this.originY = 0;
            this.windSpeed = null;
        }

        static SurfaceSpikeFeature.WindOffsetter noWind() {
            return new SurfaceSpikeFeature.WindOffsetter();
        }

        BlockPos offset(BlockPos pos) {
            if (this.windSpeed == null) {
                return pos;
            } else {
                int offset = this.originY - pos.getY();
                Vec3 scaled = this.windSpeed.scale(offset);
                return pos.offset(Mth.floor(scaled.x), 0, Mth.floor(scaled.z));
            }
        }
    }
}
