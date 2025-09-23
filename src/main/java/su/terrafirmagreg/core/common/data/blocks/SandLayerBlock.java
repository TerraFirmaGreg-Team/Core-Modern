package su.terrafirmagreg.core.common.data.blocks;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class SandLayerBlock extends AbstractLayerBlock{

    public SandLayerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public float getSpeedFactor() {
        return 1.0f;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader pLevel, BlockPos pPos) {
        BlockState blockstate = pLevel.getBlockState(pPos.below());
        if (blockstate.is(BlockTags.SNOW_LAYER_CANNOT_SURVIVE_ON)) {
            return false;
        } else if (blockstate.is(BlockTags.SNOW_LAYER_CAN_SURVIVE_ON) || Helpers.isBlock(blockstate, TFCTags.Blocks.SNOW_LAYER_SURVIVES_ON)) {
            return true;
            // allow tfc leaves to accumulate one layer of dust
        } else if (blockstate.getBlock() instanceof ILeavesBlock && state.getValue(LAYERS) == 1) {
            return true;
        } else {
            // only survive if on top of either full block or max size layer block
            return Block.isFaceFull(blockstate.getCollisionShape(pLevel, pPos.below()), Direction.UP) || blockstate.is(this) && blockstate.getValue(LAYERS) == 8;
        }
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        // if we can survive, return self
        if (pState.canSurvive(pLevel, pCurrentPos)) {
            return pState; // equivalent to SnowLayerBlock calling super.updateShape()
            // otherwise, check if it's possible to survive if we only had one layer
        } else if (pState.getValue(LAYERS) > 1) {
            final BlockState fallbackState = pState.setValue(LAYERS, 1);
            if (fallbackState.canSurvive(pLevel, pCurrentPos)) {
                return fallbackState;
            }
        }
        // if we can't survive and the fallback can't survive either, return air
        return Blocks.AIR.defaultBlockState();
    }

    // TODO: saltation visual effects during windstorm
    @Override
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {

    }

    // TBD
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
        // if targeted block is an instance of SandLayerBlock then add layer
        if (blockstate.getBlock() instanceof SandPileBlock)
        {
            // Similar to how snow layers modifies their placement state when targeting other snow layers, we do the same for snow piles
            return blockstate.setValue(LAYERS, Math.min(8, blockstate.getValue(LAYERS) + 1));
        } else if (blockstate.is(this)) {
            int i = blockstate.getValue(LAYERS);
            return blockstate.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
        } else {
            return super.getStateForPlacement(pContext);
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }


}
