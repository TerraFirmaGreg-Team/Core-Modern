package su.terrafirmagreg.core.common.block;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.plant.PlantRegrowth;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CoarseDirtBlock extends ConnectedGrassBlock {
    public CoarseDirtBlock(Properties properties, Supplier<? extends Block> dirt, @Nullable Supplier<? extends Block> path, @Nullable Supplier<? extends Block> farmland) {
        super(properties, dirt, path, farmland);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Remove the super class's turning to grass
        if (!canBeGrass(state, level, pos)) {
            if (level.isAreaLoaded(pos, 3)) {
                // Turn to not-grass
                level.setBlockAndUpdate(pos, getDirt());
            }
        } else {
            PlantRegrowth.placeRisingRock(level, pos.above(), random);
        }
    }
}
