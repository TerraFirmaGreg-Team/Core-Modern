package su.terrafirmagreg.core.world.new_ow_wg.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

/**
 * When generating chunks, all fluid blocks are queued for a postprocess tick on the main thread.
 * This is pretty slow and usually doesn't change anything, so here we do some cheaper pre-filtering.
 */
public final class FluidPostProcess {

    /** Marked in {@code LevelChunk#postProcessGeneration} */
    public static boolean inPostProcess = false;

    private FluidPostProcess() {
    }

    /** Removes postprocess marks for liquid sources whose tick cannot change anything. */
    public static void pruneInteriorSources(ChunkAccess chunk) {
        final ShortList[] lists = chunk.getPostProcessing();
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int index = 0; index < lists.length; index++) {
            final ShortList list = lists[index];
            if (list == null || list.isEmpty()) {
                continue;
            }
            final int sectionY = chunk.getSectionYFromSectionIndex(index);
            final ShortList kept = new ShortArrayList(list.size());
            for (int i = 0; i < list.size(); i++) {
                final short packed = list.getShort(i);
                final int x = packed & 15, z = (packed >>> 8) & 15;
                final int y = SectionPos.sectionToBlockCoord(sectionY, (packed >>> 4) & 15);
                // Edge columns can't be checked from inside the chunk's worker thread.
                // They get checked on the main server thread instead, see LevelChunkMixin.
                if (isEdgeColumn(x, z) || !isInteriorSource(chunk, cursor.set(minX + x, y, minZ + z))) {
                    kept.add(packed);
                }
            }
            if (kept.size() != list.size()) {
                list.clear();
                list.addAll(kept);
            }
        }
    }

    /** Positions relative to a fluid source that can cause change when the source ticks.*/
    private static final int[][] TICK_NEIGHBOURHOOD = {
            // Sides: the source may spread into them
            { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 },
            // Below: the source flows into it first, replacing it with whatever fluid the block below would become
            { 0, -1, 0 },
            // The sides of the block below, and the block under it, decide what that fluid is (and whether it is a source)
            { 1, -1, 0 }, { -1, -1, 0 }, { 0, -1, 1 }, { 0, -1, -1 }, { 0, -2, 0 }
    };

    /** Whether a block column lies on the border of its chunk, where the tick reads blocks in the neighbouring chunk. */
    public static boolean isEdgeColumn(int localX, int localZ) {
        return localX == 0 || localX == 15 || localZ == 0 || localZ == 15;
    }

    /**
     * The tick can't change anything if every block it reads is a source of the same fluid or a solid block.
     * BlockGetter is a chunk on the worldgen thread for non-edge columns, or the level on the server thread.
     */
    public static boolean isInteriorSource(BlockGetter level, BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        final FluidState fluid = state.getFluidState();
        if (!fluid.isSource() || !(state.getBlock() instanceof LiquidBlock)) {
            return false;
        }
        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int[] offset : TICK_NEIGHBOURHOOD) {
            if (!isSameSourceOrSealed(level.getBlockState(cursor.setWithOffset(pos, offset[0], offset[1], offset[2])), fluid.getType())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSameSourceOrSealed(BlockState neighbour, Fluid fluid) {
        final FluidState neighbourFluid = neighbour.getFluidState();
        if (neighbour.getBlock() instanceof LiquidBlock) {
            return neighbourFluid.isSource() && neighbourFluid.getType().isSame(fluid);
        }
        // Same as FlowingFluid#canHoldFluid, false for anything the fluid can interact with like waterloggable blocks.
        return neighbourFluid.isEmpty() && neighbour.blocksMotion() && !(neighbour.getBlock() instanceof LiquidBlockContainer);
    }
}
