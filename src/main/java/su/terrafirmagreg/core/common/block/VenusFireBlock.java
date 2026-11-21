package su.terrafirmagreg.core.common.block;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.common.data.TFGTags;

/**
 * Fire started by Venus acid rain. Burns away the block it's on except if it's part of the tag #tfg:venus_fire_immune
 * Does not spread
 */
public class VenusFireBlock extends BaseFireBlock {

    // logs each block type the first time it burns to fill #tfg:venus_fire_immune
    private static final Set<Block> LOGGED_BURNS = new HashSet<>();

    public VenusFireBlock(Properties properties) {
        super(properties, 1.0F);
    }

    public static boolean canBurnAt(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir()
                && !(state.getBlock() instanceof BaseFireBlock)
                && state.getFluidState().isEmpty()
                && !state.is(TFGTags.Blocks.VenusFireImmune)
                && state.getDestroySpeed(level, pos) >= 0;
    }

    public static void burn(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (LOGGED_BURNS.add(block)) {
            TFGCore.LOGGER.info("Venus fire burned {}", BuiltInRegistries.BLOCK.getKey(block));
        }
        level.destroyBlock(pos, false);
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return !state.is(TFGTags.Blocks.VenusFireImmune);
    }

    // To change the duration before it burns with min/max in ticks
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        level.scheduleTick(pos, this, 50 + level.random.nextInt(100));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canBurnAt(level, pos.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        if (canBurnAt(level, below)) {
            burn(level, below);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
