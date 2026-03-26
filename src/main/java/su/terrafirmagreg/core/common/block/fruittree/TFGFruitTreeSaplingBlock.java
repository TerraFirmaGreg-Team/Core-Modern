package su.terrafirmagreg.core.common.block.fruittree;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.FruitTreeSaplingBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.blockentity.TFGTickCounterBlockEntity;

/**
 * Custom fruit tree sapling block that uses TFG's own tick counter block entity.
 */
public class TFGFruitTreeSaplingBlock extends FruitTreeSaplingBlock {

    /**
     * Constructor for {@link TFGFruitTreeSaplingBlock}.
     *
     * @param properties Extended properties.
     * @param block Supplier for the branch that this sapling grows into.
     * @param treeGrowthDays Supplier for the number of days required for the tree to grow.
     * @param climateRange Supplier for the climate range suitable for this sapling.
     * @param stages Array of lifecycle stages for the sapling.
     */
    public TFGFruitTreeSaplingBlock(ExtendedProperties properties, Supplier<? extends Block> block, Supplier<Integer> treeGrowthDays, Supplier<ClimateRange> climateRange, Lifecycle[] stages) {
        super(properties, block, treeGrowthDays, climateRange, stages);
    }

    /**
     * Called when the block is placed by a player or entity.
     * Resets the tick counter for the block entity at the given position.
     *
     * @param level level.
     * @param pos The position of the block.
     * @param state The block state.
     * @param placer The entity that placed the block (nullable).
     * @param stack The item stack.
     */
    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        TFGTickCounterBlockEntity.reset(level, pos);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    /**
     * Creates a tree at the given position, updating the block state and resetting the tick counter.
     *
     * @param level Level.
     * @param pos The position of the sapling.
     * @param state The block state.
     * @param random Random source.
     */
    @Override
    public void createTree(Level level, BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        boolean onBranch = Helpers.isBlock(level.getBlockState(pos.below()), TFCTags.Blocks.FRUIT_TREE_BRANCH);
        int internalSapling = onBranch ? 3 : state.getValue(TFCBlockStateProperties.SAPLINGS);
        if (internalSapling == 1 && random.nextBoolean()) {
            internalSapling++;
        }
        level.setBlock(pos,
                this.block.get().defaultBlockState()
                        .setValue(PipeBlock.DOWN, true)
                        .setValue(TFCBlockStateProperties.SAPLINGS, internalSapling)
                        .setValue(TFCBlockStateProperties.STAGE_3, onBranch ? 1 : 0),
                3);
        TFGTickCounterBlockEntity.reset(level, pos);
    }
}
