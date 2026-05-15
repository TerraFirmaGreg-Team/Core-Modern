package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.HorizontalSupportBlock;
import net.dries007.tfc.common.blocks.wood.VerticalSupportBlock;
import net.dries007.tfc.util.Support;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.perf.SupportCache;

@Mixin(value = HorizontalSupportBlock.class, remap = true)
public abstract class HorizontalSupportBlockMixin extends VerticalSupportBlock {

    protected HorizontalSupportBlockMixin(ExtendedProperties properties) {
        super(properties);
    }

    /**
     * Adds supports to the cache.
     * Fires on both client and server for all players when a support block is placed.
     */
    @Override
    public void onBlockStateChange(LevelReader levelReader, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!(levelReader instanceof Level level))
            return;
        Support support = Support.get(newState);
        if (support != null) {
            SupportCache.forLevel(level).addSupport(pos.immutable(), support);
        }
    }

    /**
     * Unfortunately this is only called ServerSide, so the clientside cache may contain stale supports.
     * In practice this is no problem because we doublecheck all found supports and remove them if they're stale,
     * so clientside the cache is just a little lazier about removal.
     */
    @SuppressWarnings({ "NullableProblems", "deprecation" })
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            SupportCache.forLevel(level).removeSupport(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
