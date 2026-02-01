package su.terrafirmagreg.core.common.atmosphere;

import javax.annotation.Nullable;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import su.terrafirmagreg.core.utils.atmosphere.FloodFillStatus;

/**
 * Tracks the state of an atmosphere room for a single machine.
 * Handles sealed/bubble/inactive modes, revalidation timing, and dirty state.
 */
//TODO: All of this
public class AtmosphereRoom {

    /**
     * Current operational mode of the room.
     */
    public enum Mode {
        /** Room is fully sealed and providing atmosphere. */
        SEALED,
        /** Room exceeds size limits, operating in bubble mode. */
        BUBBLE,
        /** Machine is inactive (no power, disabled, etc.) */
        INACTIVE,
        /** Room has incomplete data (unloaded chunks, pending fill, etc.) */
        INCOMPLETE
    }

    /** The provider (machine) for this room */
    @Getter
    private final IAtmosphereProvider provider;

    /** Current operational mode */
    @Getter
    private Mode mode = Mode.INCOMPLETE;

    /** The room scan data (blocks, status, bounds) */
    @Getter
    private RoomScan scan = RoomScan.empty();

    /** Current atmosphere level (0.0-1.0) */
    @Getter
    private float atmosphereLevel = 0.0f;

    /** Whether this room needs revalidation */
    @Getter
    private boolean isDirty = true;

    // Revalidation timing
    private long lastRevalidationTick = 0;
    private long earliestRevalidationTick = 0;
    private static final int MIN_REVALIDATION_DELAY = 1;
    private static final int BASE_REVALIDATION_DELAY = 10;

    // Escape point for vortex spawning (null if sealed)
    @Nullable
    private BlockPos escapePoint = null;
    private boolean wasSealed = false; // Track for breach detection

    public AtmosphereRoom(IAtmosphereProvider provider) {
        this.provider = provider;
    }

    /**
     * @return Escape point from the last flood fill (null if sealed)
     */
    @Nullable
    public BlockPos getEscapePoint() {
        return escapePoint;
    }

    /**
     * Marks this room as needing revalidation.
     */
    public void markDirty() {
        this.isDirty = true;
    }

    /**
     * Checks if this room is ready for revalidation.
     *
     * @param currentTick Current game tick
     * @return true if revalidation can proceed
     */
    public boolean canRevalidate(long currentTick) {
        return isDirty && currentTick >= earliestRevalidationTick;
    }

    /**
     * Gets the revalidation delay based on room size.
     * Larger rooms get longer delays to avoid excessive recalculation.
     *
     * @return Delay in ticks
     */
    public int getRevalidationDelay() {
        int size = scan.interiorSize();
        if (size < 1000) {
            return MIN_REVALIDATION_DELAY;
        } else if (size < 10000) {
            return BASE_REVALIDATION_DELAY;
        } else {
            return BASE_REVALIDATION_DELAY * 2; // 20 ticks for huge rooms
        }
    }

    /**
     * Called before starting a flood fill operation.
     * Sets up timing to prevent rapid revalidation.
     *
     * @param currentTick Current game tick
     */
    public void onRevalidationStarted(long currentTick) {
        this.lastRevalidationTick = currentTick;
        this.earliestRevalidationTick = currentTick + getRevalidationDelay();
    }

    /**
     * Updates the room state from a room scan.
     *
     * @param result The room scan result
     */
    public void updateFromResult(RoomScan result) {
        this.isDirty = false;

        // Track previous sealed state for breach detection
        boolean previouslySealed = this.wasSealed;

        // Update scan and escape point
        this.scan = result;
        this.escapePoint = result.escapePoint();

        // Determine new mode
        if (!provider.isActive()) {
            this.mode = Mode.INACTIVE;
            this.wasSealed = false;
        } else if (result.status() == FloodFillStatus.ESCAPED_UNLOADED) {
            this.mode = Mode.INCOMPLETE;
            this.wasSealed = false;
        } else if (!result.isComplete()) {
            // Block limit exceeded - fall back to bubble mode
            this.mode = Mode.BUBBLE;
            this.wasSealed = false;
        } else if (result.isSealed()) {
            this.mode = Mode.SEALED;
            this.wasSealed = true;
        } else {
            // Unsealed - check if this is a breach (was sealed, now isn't)
            this.mode = Mode.INCOMPLETE;
            this.wasSealed = false;

            // Notify provider of breach if applicable
            if (previouslySealed && escapePoint != null) {
                provider.onRoomBreached(this);
            }
        }

        // Update atmosphere level based on mode
        updateAtmosphereLevel();

        // Notify provider of state change
        provider.onRoomStateChanged(this);
    }

    /**
     * Updates the atmosphere level based on current mode.
     */
    private void updateAtmosphereLevel() {
        switch (mode) {
            case SEALED -> atmosphereLevel = 1.0f;
            case BUBBLE -> atmosphereLevel = 1.0f; // Bubble mode still provides full atmosphere
            case INACTIVE, INCOMPLETE -> atmosphereLevel = 0.0f;
        }
    }

    /**
     * Sets the room to inactive mode.
     */
    public void setInactive() {
        this.mode = Mode.INACTIVE;
        this.wasSealed = false;
        this.atmosphereLevel = 0.0f;
        this.isDirty = false;
        provider.onRoomStateChanged(this);
    }

    /**
     * Checks if a position has atmosphere from this room.
     *
     * @param pos Position to check
     * @return true if the position has atmosphere
     */
    public boolean hasAtmosphere(BlockPos pos) {
        if (!provider.isActive()) {
            return false;
        }

        return switch (mode) {
            case SEALED -> scan.containsEnvelope(pos);
            case BUBBLE -> {
                // In bubble mode, use distance-based check
                BlockPos center = provider.getPosition();
                double maxRadius = Math.cbrt(provider.getMaxRoomSize() / (4.0 / 3.0 * Math.PI));
                double distSq = pos.distSqr(center);
                yield distSq <= maxRadius * maxRadius;
            }
            case INACTIVE, INCOMPLETE -> false;
        };
    }

    /**
     * Checks if a chunk becoming loaded might affect this room.
     * If the room hit an unloaded chunk and this chunk is within bounds, revalidate.
     *
     * @param chunkPos Chunk that was loaded
     */
    public void onChunkLoaded(ChunkPos chunkPos) {
        if (mode != Mode.INCOMPLETE || scan.status() != FloodFillStatus.ESCAPED_UNLOADED) {
            return;
        }

        if (scan.touchedChunks().contains(chunkPos)) {
            markDirty();
        }
    }

    /**
     * Checks if a block position is within the area this room cares about.
     *
     * @param pos Position to check
     * @return true if a block change at this position should trigger revalidation
     */
    public boolean isPositionRelevant(BlockPos pos) {
        // Check if position is in envelope (block changes inside or on boundary matter)
        if (scan.containsEnvelope(pos)) {
            return true;
        }

        // Also check if position is adjacent to envelope (new blocks placed next to room)
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            if (scan.containsEnvelope(pos.relative(dir))) {
                return true;
            }
        }

        return false;
    }
}
