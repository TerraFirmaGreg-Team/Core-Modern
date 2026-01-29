package su.terrafirmagreg.core.utils.atmosphere;

/**
 * Status of the flood fill operation.
 */
public enum FloodFillStatus {
    /** Fill completed, room fully enclosed */
    SEALED,
    /** Fill found escape via horizontal dimension limit */
    ESCAPED_DIMENSION,
    /** Fill found escape via world height limit */
    ESCAPED_BUILD_HEIGHT,
    /** Fill stopped at unloaded chunk (escape assumed) */
    ESCAPED_UNLOADED,
    /** Fill stopped at block limit (seal status unknown) */
    BLOCK_LIMIT;

    public boolean isSealed() {
        return this == SEALED;
    }

    public boolean isComplete() {
        return this != BLOCK_LIMIT;
    }

    public boolean hasEscape() {
        return this != SEALED && this != BLOCK_LIMIT;
    }
}
