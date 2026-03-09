package su.terrafirmagreg.core.common.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.dries007.tfc.util.Support;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Per-level cache of support block AABBs.
 * Replaces Support.isSupported()'s brute-force 14k block scan with a few AABB check.
 */
public class SupportCache {

    private static final Map<ResourceKey<Level>, SupportCache> CACHES = new HashMap<>();

    public static SupportCache forLevel(ServerLevel level) {
        return CACHES.computeIfAbsent(level.dimension(), k -> new SupportCache());
    }

    public static void clearLevel(ResourceKey<Level> dimension) {
        CACHES.remove(dimension);
    }

    // Maps chunk long pos -> list of (pos, aabb) entries for supports in that chunk
    private final Map<Long, List<SupportEntry>> chunkSupports = new HashMap<>();
    // Tracks which chunks have been scanned, even if they contained no supports
    private final Set<Long> scannedChunks = new HashSet<>();

    private SupportCache() {
    }

    /**
     * Check if a BlockPos is supported.
     */
    public boolean isSupported(ServerLevel level, BlockPos pos) {
        int horizontal = Support.getSupportCheckRange().horizontal();

        int minCX = (pos.getX() - horizontal) >> 4;
        int maxCX = (pos.getX() + horizontal) >> 4;
        int minCZ = (pos.getZ() - horizontal) >> 4;
        int maxCZ = (pos.getZ() + horizontal) >> 4;

        // Iterate over supports in all chunks that could affect this block
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                long key = ChunkPos.asLong(cx, cz);

                // Build cache if needed
                if (!scannedChunks.contains(key)) {
                    scanChunk(level, cx, cz);
                }

                List<SupportEntry> entries = chunkSupports.get(key);
                if (entries == null)
                    continue;

                var it = entries.iterator();
                while (it.hasNext()) {
                    SupportEntry entry = it.next();
                    if (entry.contains(pos)) {
                        // Sanity check
                        if (Support.get(level.getBlockState(entry.pos())) == null) {
                            it.remove();
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Find all support blocks in a chunk.
     * Does a very low-level scan of the raw PalettedContainer data to make things fast.
     */
    private void scanChunk(ServerLevel level, int cx, int cz) {
        long key = ChunkPos.asLong(cx, cz);
        scannedChunks.add(key);

        ChunkAccess chunk = level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, true);
        if (chunk == null)
            return;

        List<SupportEntry> entries = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();

        for (int sectionIdx = 0; sectionIdx < sections.length; sectionIdx++) {
            LevelChunkSection section = sections[sectionIdx];
            if (section.hasOnlyAir())
                continue;

            PalettedContainer<BlockState> states = section.getStates();

            // Find which palette indices correspond to support blocks
            IntSet supportIndices = new IntArraySet();
            var palette = states.data.palette();
            for (int i = 0; i < palette.getSize(); i++) {
                if (Support.get(palette.valueFor(i)) != null) {
                    supportIndices.add(i);
                }
            }
            if (supportIndices.isEmpty())
                continue;

            // Scan raw storage for positions with one of these indices
            int baseY = chunk.getSectionYFromSectionIndex(sectionIdx) << 4;
            int[] slot = { 0 };
            states.data.storage().getAll(paletteIdx -> {
                if (supportIndices.contains(paletteIdx)) {
                    int i = slot[0];
                    // Reconstruct BlockPos and Support
                    // PalettedContainer.Strategy.SECTION_STATES uses (y << 8 | z << 4 | x)
                    int x = i & 0xF;
                    int z = (i >> 4) & 0xF;
                    int y = (i >> 8) & 0xF;
                    BlockPos supportPos = new BlockPos((cx << 4) + x, baseY + y, (cz << 4) + z);
                    Support support = Support.get(palette.valueFor(paletteIdx));
                    assert support != null;
                    entries.add(SupportEntry.of(supportPos, support));
                }
                slot[0]++;
            });
        }

        if (!entries.isEmpty()) {
            chunkSupports.put(key, entries);
        }
    }

    /**
     * Build a set of BlockPos between the given positions, then remove all BlockPos that are supported from this set.
     * @return a Set<BlockPos> of unsupported blocks.
     */
    public Set<BlockPos> findUnsupportedPositions(ServerLevel level, BlockPos from, BlockPos to) {
        int minX = Math.min(from.getX(), to.getX());
        int maxX = Math.max(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int maxY = Math.max(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxZ = Math.max(from.getZ(), to.getZ());

        Set<BlockPos> unsupported = new HashSet<>();
        for (BlockPos p : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            unsupported.add(p.immutable());
        }

        Support.SupportRange range = Support.getSupportCheckRange();
        int minCX = (minX - range.horizontal()) >> 4;
        int maxCX = (maxX + range.horizontal()) >> 4;
        int minCZ = (minZ - range.horizontal()) >> 4;
        int maxCZ = (maxZ + range.horizontal()) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                long key = ChunkPos.asLong(cx, cz);
                if (!scannedChunks.contains(key)) {
                    scanChunk(level, cx, cz);
                }
                List<SupportEntry> entries = chunkSupports.get(key);
                if (entries == null)
                    continue;

                for (SupportEntry entry : entries) {
                    if (entry.maxX() < minX || entry.minX() > maxX)
                        continue;
                    if (entry.maxY() < minY || entry.minY() > maxY)
                        continue;
                    if (entry.maxZ() < minZ || entry.minZ() > maxZ)
                        continue;
                    int ex1 = Math.max(entry.minX(), minX);
                    int ex2 = Math.min(entry.maxX(), maxX);
                    int ey1 = Math.max(entry.minY(), minY);
                    int ey2 = Math.min(entry.maxY(), maxY);
                    int ez1 = Math.max(entry.minZ(), minZ);
                    int ez2 = Math.min(entry.maxZ(), maxZ);
                    for (BlockPos p : BlockPos.betweenClosed(ex1, ey1, ez1, ex2, ey2, ez2)) {
                        unsupported.remove(p);
                    }
                }
            }
        }

        return unsupported;
    }

    public void addSupport(BlockPos pos, Support support) {
        long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        List<SupportEntry> entries = chunkSupports.computeIfAbsent(key, k -> new ArrayList<>());
        for (SupportEntry e : entries) {
            if (e.pos().equals(pos))
                return;
        }
        entries.add(SupportEntry.of(pos, support));
    }

    public void removeSupport(BlockPos pos) {
        long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        List<SupportEntry> entries = chunkSupports.get(key);
        if (entries != null) {
            entries.removeIf(e -> e.pos().equals(pos));
            if (entries.isEmpty())
                chunkSupports.remove(key);
        }
    }

    public record SupportEntry(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        public static SupportEntry of(BlockPos pos, Support support) {
            return new SupportEntry(
                    pos,
                    pos.getX() - support.getSupportHorizontal(),
                    pos.getY() - support.getSupportDown(),
                    pos.getZ() - support.getSupportHorizontal(),
                    pos.getX() + support.getSupportHorizontal(),
                    pos.getY() + support.getSupportUp(),
                    pos.getZ() + support.getSupportHorizontal());
        }

        public boolean contains(BlockPos testPos) {
            return testPos.getX() >= minX && testPos.getX() <= maxX
                    && testPos.getY() >= minY && testPos.getY() <= maxY
                    && testPos.getZ() >= minZ && testPos.getZ() <= maxZ;
        }
    }
}
