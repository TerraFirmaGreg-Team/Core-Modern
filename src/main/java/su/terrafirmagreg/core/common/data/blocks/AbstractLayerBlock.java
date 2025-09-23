package su.terrafirmagreg.core.common.data.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;

import javax.annotation.Nullable;

/**
 * Base layer block implementation that bundles genericized TFC {@link net.dries007.tfc.mixin.SnowLayerBlockMixin} methods
 */
public abstract class AbstractLayerBlock extends SnowLayerBlock {

    public AbstractLayerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return switch (pType) {
            case LAND -> pState.getValue(LAYERS) < HEIGHT_IMPASSABLE;
            case WATER, AIR -> false;
        };
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        final BlockState blockState = pLevel.getBlockState(pPos.below());

        return Block.isFaceFull(blockState.getCollisionShape(pLevel, pPos.below()), Direction.UP) || blockState.is(this) && blockState.getValue(LAYERS) == 8;
    }

    @Override
    public abstract void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom);

    /**
     * Add behavior to layer blocks - when they are destroyed, they should only destroy one layer.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid)
    {
        playerWillDestroy(level, pos, state, player);
        final int prevLayers = state.getValue(LAYERS);
        if (prevLayers > 1 && !player.isCreative())
        {
            return level.setBlock(pos, state.setValue(LAYERS, prevLayers - 1), level.isClientSide ? 11 : 3);
        }
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockState = pContext.getLevel().getBlockState(pContext.getClickedPos());
        if (blockState.is(this)) {
            int i = blockState.getValue(LAYERS);
            return blockState.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
        } else {
            return this.defaultBlockState();
        }
    }

    @Override
    public abstract float getSpeedFactor();



}
