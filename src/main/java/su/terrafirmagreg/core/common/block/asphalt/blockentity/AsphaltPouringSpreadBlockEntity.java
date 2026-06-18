package su.terrafirmagreg.core.common.block.asphalt.blockentity;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.therighthon.rnr.common.RNRTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadHelper;
import su.terrafirmagreg.core.common.block.asphalt.AsphaltRoadPouringBlock;
import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Asphalt;

public class AsphaltPouringSpreadBlockEntity extends BlockEntity {

    private long[] spreadPlan = new long[0];
    private int spreadIndex = 0;

    public AsphaltPouringSpreadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * @return true when this pour tile is finished spreading / settling and ready to merge onto the base below.
     */
    public boolean spreadStep(ServerLevel level, BlockPos pourPos) {
        if (spreadPlan.length == 0) {
            spreadPlan = buildSpreadPlan(level, pourPos);
            spreadIndex = 0;
            setChanged();
        }

        int processed = 0;
        while (spreadIndex < spreadPlan.length && processed < AsphaltRoadHelper.POURING_SPREAD_BATCH_PER_TICK) {
            BlockPos baseNeighbor = BlockPos.of(spreadPlan[spreadIndex++]);
            BlockState baseState = level.getBlockState(baseNeighbor);
            if (baseState.is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
                level.setBlock(baseNeighbor, TFGBlocks_Asphalt.ASPHALT_ROAD_HOT.getDefaultState(), Block.UPDATE_ALL);
                level.updateNeighborsAt(baseNeighbor, TFGBlocks_Asphalt.ASPHALT_ROAD_HOT.get());
            }
            processed++;
        }

        if (spreadIndex >= spreadPlan.length) {
            spreadPlan = new long[0];
            spreadIndex = 0;
            setChanged();
            return true;
        }
        return false;
    }

    public int currentVisualLevel() {
        if (spreadPlan.length == 0) {
            return AsphaltRoadPouringBlock.MAX_VISUAL_LEVEL;
        }
        double remainingRatio = (double) (spreadPlan.length - spreadIndex) / (double) spreadPlan.length;
        int level = (int) Math.ceil(remainingRatio * AsphaltRoadPouringBlock.MAX_VISUAL_LEVEL);
        return Math.max(0, Math.min(AsphaltRoadPouringBlock.MAX_VISUAL_LEVEL, level));
    }

    private static long[] buildSpreadPlan(ServerLevel level, BlockPos pourSourcePos) {
        BlockPos sourceBase = pourSourcePos.below();
        if (!level.getBlockState(sourceBase).is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
            return new long[0];
        }

        record Path(BlockPos pos, int cost) {
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final Map<BlockPos, Integer> visited = new HashMap<>();
        final Queue<Path> queue = new ArrayDeque<>();

        visited.put(sourceBase, 0);
        queue.add(new Path(sourceBase, 0));

        while (!queue.isEmpty() && visited.size() < AsphaltRoadHelper.POURING_SPREAD_MAX_BLOCKS) {
            final Path current = queue.remove();

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                cursor.setWithOffset(current.pos, direction);
                if (visited.containsKey(cursor) || !level.getBlockState(cursor).is(RNRTags.Blocks.CONCRETE_SPREADABLE)) {
                    continue;
                }

                final BlockPos next = cursor.immutable();
                visited.put(next, current.cost + 1);
                queue.add(new Path(next, current.cost + 1));

                if (visited.size() >= AsphaltRoadHelper.POURING_SPREAD_MAX_BLOCKS) {
                    break;
                }
            }
        }

        return visited.entrySet()
                .stream()
                .sorted(Map.Entry.<BlockPos, Integer>comparingByValue()
                        .thenComparing(entry -> entry.getKey().distSqr(sourceBase)))
                .mapToLong(entry -> entry.getKey().asLong())
                .toArray();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLongArray("spreadPlan", spreadPlan);
        tag.putInt("spreadIndex", spreadIndex);
    }

    @Override
    public void load(CompoundTag tag) {
        spreadPlan = tag.getLongArray("spreadPlan");
        spreadIndex = tag.getInt("spreadIndex");
        super.load(tag);
    }
}
