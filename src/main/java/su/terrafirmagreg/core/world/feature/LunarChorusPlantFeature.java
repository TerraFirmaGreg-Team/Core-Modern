package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import su.terrafirmagreg.core.common.data.TFGBlocks;
import su.terrafirmagreg.core.common.data.blocks.LunarChorusFlowerBlock;

public class LunarChorusPlantFeature extends Feature<NoneFeatureConfiguration> {

    public LunarChorusPlantFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos blockPos = context.origin();
        RandomSource randomSource = context.random();

        if (level.isEmptyBlock(blockPos)
                && LunarChorusFlowerBlock.isGroundBlock(level.getBlockState(blockPos.below()))) {
            ((LunarChorusFlowerBlock) TFGBlocks.LUNAR_CHORUS_FLOWER.get()).generatePlant(level, blockPos, randomSource,
                    8);
            return true;
        } else {
            return false;
        }
    }
}
