package su.terrafirmagreg.core.common.data.capabilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.dries007.tfc.util.Support;
import net.dries007.tfc.util.Support.SupportRange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/**
 * Additional data that is attached to a chunk during world generation. Stores support information of each placed support within a chunk.
 * Due to TFG adding supports with very large ranges, TFC's standard implementation of {@link net.dries007.tfc.util.Support#isSupported(net.minecraft.world.level.BlockGetter, net.minecraft.core.BlockPos) Support.isSupported()} consumes enormous amounts of TPS.
 * This capability is meant to remedy that problem by querying each support block instead of iterating through a volume.
 */
public class Supports {

    public static final Supports EMPTY = new Supports.Immutable();

    public static boolean isSupported(LevelReader level, BlockPos pos) {
        for (SupportEntry entry : getNearbySupportList(level, pos)) {
            if (pos.distManhattan(entry.pos) > entry.maxRange()) continue;
            if (entry.boundingBox().contains(pos.getCenter())) return true;
        }
        return false;
    }

    public static List<SupportEntry> getNearbySupportList(LevelReader level, BlockPos pos) {
        SupportRange checkRange = Support.getSupportCheckRange();
        List<SupportEntry> supportEntries = new ArrayList<>();

        // 1. calculate chunks that could be in range

        Stream<ChunkAccess> chunksInRange = ChunkPos.rangeClosed(new ChunkPos(pos), (int) Math.ceil(checkRange.horizontal() / 16.0f))
                .map(chunkPos -> level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL));

        // 2. iterate through each chunk and pull supports data, if any

        chunksInRange.forEach(chunk -> {
            List<SupportEntry> listSupports = get((LevelChunk) chunk).getSupportList();
            supportEntries.addAll(listSupports);
        });

        supportEntries.sort(Comparator.comparingInt(s -> s.pos.distManhattan(pos)));
        return supportEntries;
    }

    public static Supports get(LevelChunk chunk) {
        return chunk.isEmpty() ? EMPTY : chunk.getCapability(SupportsCapability.CAPABILITY).map(SupportsCapability::getData).orElse(EMPTY);
    }

    private List<SupportEntry> supportEntries;

    public Supports() {}

    public List<SupportEntry> getSupportList() {
        return this.supportEntries;
    }


    public class SupportEntry {
        final public BlockPos pos;
        final private int rangeUp;
        final private int rangeDown;
        final private int rangeHorizontal;

        public SupportEntry(BlockPos pos, int rangeUp, int rangeDown, int rangeHorizontal) {
            this.pos = pos.immutable();
            this.rangeUp = rangeUp;
            this.rangeDown = rangeDown;
            this.rangeHorizontal = rangeHorizontal;
        }

        public AABB boundingBox() {
            return new AABB(pos.offset(-1 * this.rangeHorizontal, -1 * this.rangeDown, -1 * this.rangeHorizontal),
                    pos.offset(this.rangeHorizontal, this.rangeUp, this.rangeHorizontal));
        }

        public int maxRange() {
            return Math.max(Math.max(rangeUp, rangeDown), rangeHorizontal);
        }
    }

    private static class Immutable extends Supports {
    }
}
