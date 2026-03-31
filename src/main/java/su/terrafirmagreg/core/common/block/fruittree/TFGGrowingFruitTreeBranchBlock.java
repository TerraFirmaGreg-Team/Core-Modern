package su.terrafirmagreg.core.common.block.fruittree;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeLeavesBlock;
import net.dries007.tfc.common.blocks.plant.fruit.GrowingFruitTreeBranchBlock;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.blockentity.TFGTickCounterBlockEntity;

/**
 * Custom growing fruit tree branch block.
 */
public class TFGGrowingFruitTreeBranchBlock extends GrowingFruitTreeBranchBlock {

    private final Supplier<ClimateRange> climateRange;

    /**
     * Constructor for {@link TFGGrowingFruitTreeBranchBlock}.
     *
     * @param properties Extended properties.
     * @param body Supplier for the trunk of the tree.
     * @param leaves Supplier for the leaves of the tree.
     * @param climateRange Supplier for the climate range.
     */
    public TFGGrowingFruitTreeBranchBlock(ExtendedProperties properties, Supplier<? extends Block> body, Supplier<? extends Block> leaves, Supplier<ClimateRange> climateRange) {
        super(properties, body, leaves, climateRange);
        this.climateRange = climateRange;
    }

    /**
     * Handles random ticking for the block.
     *
     * @param state The current block state.
     * @param level Level.
     * @param pos The position of the block.
     * @param rand Random source.
     */
    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource rand) {
        int hydration = FruitTreeLeavesBlock.getHydration(level, pos);
        float temp = Climate.getTemperature(level, pos);
        if (!this.climateRange.get().checkBoth(hydration, temp, false) && !state.getValue(NATURAL)) {
            TFGTickCounterBlockEntity.reset(level, pos);
        }
        super.randomTick(state, level, pos, rand);
    }
}
