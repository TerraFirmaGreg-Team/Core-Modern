package su.terrafirmagreg.core.utils.atmosphere;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lombok.Getter;

/**
 * Efficient storage for room interior using section-based bitmaps.
 * Stores both interior (passable blocks) and envelope (interior + shell).
 * <p>
 * Envelope is used for hasOxygen checks because:
 * - Player standing on shell block should still have oxygen
 * - Single bitmap lookup instead of interior OR shell
 */
public class RoomInterior {

    /**
     * Section-based bitmap for interior blocks (passable air blocks).
     * Key: SectionPos packed as long
     * Value: BitSet with 4096 bits (16x16x16 section)
     */
    private final Map<Long, BitSet> interiorBitmaps = new HashMap<>();

    /**
     * Section-based bitmap for envelope blocks (interior + shell).
     * This is what's used for oxygen checks.
     */
    private final Map<Long, BitSet> envelopeBitmaps = new HashMap<>();

    /**
     * Bounds of the room.
     */
    @Getter
    private final AABB bounds;

    /**
     * Set of chunks this room touches (for listener registration).
     */
    @Getter
    private final Set<ChunkPos> touchedChunks;

    /**
     * Total number of interior blocks.
     */
    @Getter
    private final int interiorSize;

    /**
     * Total number of envelope blocks.
     */
    @Getter
    private final int envelopeSize;

    private RoomInterior(Map<Long, BitSet> interiorBitmaps, Map<Long, BitSet> envelopeBitmaps,
            AABB bounds, Set<ChunkPos> touchedChunks, int interiorSize, int envelopeSize) {
        this.interiorBitmaps.putAll(interiorBitmaps);
        this.envelopeBitmaps.putAll(envelopeBitmaps);
        this.bounds = bounds;
        this.touchedChunks = touchedChunks;
        this.interiorSize = interiorSize;
        this.envelopeSize = envelopeSize;
    }

    /**
     * Creates a RoomInterior from a FloodFillResult.
     *
     * @param result The flood fill result
     * @return RoomInterior with section-based bitmap storage
     */
    public static RoomInterior fromFloodFillResult(FloodFillResult result) {
        Map<Long, BitSet> interiorBitmaps = new HashMap<>();
        Map<Long, BitSet> envelopeBitmaps = new HashMap<>();
        Set<ChunkPos> touchedChunks = new HashSet<>();

        // Process interior blocks
        LongIterator interiorIter = result.interior().iterator();
        while (interiorIter.hasNext()) {
            long posLong = interiorIter.nextLong();
            addToBitmap(posLong, interiorBitmaps, touchedChunks);
        }

        // Process envelope (interior + shell) - this is what's used for oxygen checks
        LongIterator envelopeIter = result.envelope().iterator();
        while (envelopeIter.hasNext()) {
            long posLong = envelopeIter.nextLong();
            addToBitmap(posLong, envelopeBitmaps, touchedChunks);
        }

        return new RoomInterior(
                interiorBitmaps,
                envelopeBitmaps,
                result.bounds(),
                touchedChunks,
                result.interiorSize(),
                result.envelopeSize());
    }

    /**
     * Adds a position to a bitmap map.
     */
    private static void addToBitmap(long posLong, Map<Long, BitSet> bitmaps, Set<ChunkPos> touchedChunks) {
        BlockPos pos = BlockPos.of(posLong);
        long sectionKey = SectionPos.asLong(pos);
        int indexInSection = getIndexInSection(pos);

        bitmaps.computeIfAbsent(sectionKey, k -> new BitSet(4096)).set(indexInSection);
        touchedChunks.add(new ChunkPos(pos));
    }

    /**
     * Gets the index within a section (0-4095) for a block position.
     */
    private static int getIndexInSection(BlockPos pos) {
        int x = pos.getX() & 15;
        int y = pos.getY() & 15;
        int z = pos.getZ() & 15;
        return (y << 8) | (z << 4) | x;
    }

    /**
     * Checks if a position is in the interior (passable blocks only).
     *
     * @param pos Position to check
     * @return true if position is in the interior
     */
    public boolean containsInterior(BlockPos pos) {
        // Fast bounds check first
        if (!bounds.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
            return false;
        }

        long sectionKey = SectionPos.asLong(pos);
        BitSet bitmap = interiorBitmaps.get(sectionKey);
        if (bitmap == null) {
            return false;
        }

        return bitmap.get(getIndexInSection(pos));
    }

    /**
     * Checks if a position is in the envelope (interior + shell).
     * This is the primary method for oxygen checks.
     *
     * @param pos Position to check
     * @return true if position is in the envelope (has oxygen)
     */
    public boolean containsEnvelope(BlockPos pos) {
        // Fast bounds check first - use slightly expanded bounds for shell
        double margin = 1.0;
        if (pos.getX() < bounds.minX - margin || pos.getX() > bounds.maxX + margin ||
                pos.getY() < bounds.minY - margin || pos.getY() > bounds.maxY + margin ||
                pos.getZ() < bounds.minZ - margin || pos.getZ() > bounds.maxZ + margin) {
            return false;
        }

        long sectionKey = SectionPos.asLong(pos);
        BitSet bitmap = envelopeBitmaps.get(sectionKey);
        if (bitmap == null) {
            return false;
        }

        return bitmap.get(getIndexInSection(pos));
    }

    /**
     * Checks if a position is in the shell (envelope but not interior).
     *
     * @param pos Position to check
     * @return true if position is a shell block
     */
    public boolean containsShell(BlockPos pos) {
        return containsEnvelope(pos) && !containsInterior(pos);
    }

    /**
     * @return Number of shell blocks (envelope - interior)
     */
    public int getShellSize() {
        return envelopeSize - interiorSize;
    }

    /**
     * Checks if any of the given positions intersect with the envelope.
     * Useful for detecting block changes that affect the room.
     *
     * @param positions Set of positions to check (as packed longs)
     * @return true if any position intersects
     */
    public boolean containsAny(LongOpenHashSet positions) {
        LongIterator iter = positions.iterator();
        while (iter.hasNext()) {
            long posLong = iter.nextLong();
            if (containsEnvelope(BlockPos.of(posLong))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a specific chunk intersects with this room.
     *
     * @param chunkPos Chunk position to check
     * @return true if the chunk is touched by this room
     */
    public boolean touchesChunk(ChunkPos chunkPos) {
        return touchedChunks.contains(chunkPos);
    }

    /**
     * Creates an empty RoomInterior.
     *
     * @return Empty room with no blocks
     */
    public static RoomInterior empty() {
        return new RoomInterior(
                Map.of(),
                Map.of(),
                new AABB(0, 0, 0, 0, 0, 0),
                Set.of(),
                0,
                0);
    }
}
