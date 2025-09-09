package su.terrafirmagreg.core.world.feature;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.CoralMushroomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DeadCoralMushroomFeature extends CoralMushroomFeature {
    public DeadCoralMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    protected boolean placeCoralBlock(LevelAccessor level, RandomSource random, BlockPos pos, BlockState state) {
        return DeadCoralHelpers.placeCoralBlock(level, random, pos, state);

    }
}
