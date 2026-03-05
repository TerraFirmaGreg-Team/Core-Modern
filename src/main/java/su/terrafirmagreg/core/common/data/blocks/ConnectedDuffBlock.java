package su.terrafirmagreg.core.common.data.blocks;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.blocks.plant.PlantRegrowth;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ConnectedDuffBlock extends ConnectedGrassBlock {
    public ConnectedDuffBlock(Properties properties, Supplier<? extends Block> dirt, @Nullable Supplier<? extends Block> path, @Nullable Supplier<? extends Block> farmland) {
        super(properties, dirt, path, farmland);
    }

    ConnectedDuffBlock(Properties properties, SoilBlockType dirtType, RegistrySoilVariant variant) {
        this(properties, variant.getBlock(dirtType), variant.getBlock(SoilBlockType.GRASS_PATH), variant.getBlock(SoilBlockType.FARMLAND));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
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
