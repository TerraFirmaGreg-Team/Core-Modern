package su.terrafirmagreg.core.world.feature;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class RockPileFeature extends Feature<RockPileConfig> {
    public RockPileFeature(Codec<RockPileConfig> codec) {
        super(codec);
    }

    private List<BlockPos> getCorners(BlockPos origin, int newY, int radius) {
        BlockPos c1 = origin.offset(radius, 0, radius).atY(newY);
        BlockPos c2 = origin.offset(-radius, 0, -radius).atY(newY);

        return new ArrayList<>(List.of(c1, c2));
    }

    //Only places cobble
    @Override
    public boolean place(FeaturePlaceContext<RockPileConfig> context) {
        final RandomSource random = context.random();
        final BlockPos blockpos = context.origin();
        final WorldGenLevel level = context.level();
        final RockPileConfig config = context.config();

        if (blockpos.getY() < level.getMinBuildHeight() + 5) {
            return false;
        }

        Block localCobble = ChunkDataProvider.get(level).get(level, blockpos).getRockData().getRock(blockpos).cobble();

        int radius = config.radius();
        int height = config.height();
        final int size = config.size().sample(random);
        int placedAmount = 0;

        for (int yPos = blockpos.getY(); yPos <= blockpos.getY() + height - 1; yPos++) {
            List<BlockPos> corners = getCorners(blockpos, yPos, radius);
            System.out.println(yPos);
            System.out.println(corners);
            for (BlockPos placePos : BlockPos.betweenClosed(corners.get(0), corners.get(1))) {
                if (placedAmount < size) {
                    System.out.println("valid placement" + mayPlaceOn(level, placePos));
                    if (random.nextInt(1, 10) <= 8 && mayPlaceOn(level, placePos)) {
                        level.setBlock(placePos, localCobble.defaultBlockState(), 3);
                        placedAmount++;
                        System.out.println(placedAmount);
                    }
                }
            }

            if (random.nextInt(1, 10) <= 3 && radius > 1) {
                radius--;
            }
        }

        return false;
    }

    private boolean mayPlaceOn(LevelAccessor level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        return blockstate.isFaceSturdy(level, blockpos, Direction.UP);
    }
}
