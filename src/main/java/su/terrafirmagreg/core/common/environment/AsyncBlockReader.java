package su.terrafirmagreg.core.common.environment;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import lombok.Getter;

import su.terrafirmagreg.core.mixins.common.minecraft.AccessorChunkMap;
import su.terrafirmagreg.core.mixins.common.minecraft.AccessorServerChunkCache;

/**
 * Provides thread-safe read-only block state access for async flood fills.
 * <p>
 * Bypasses {@link ServerChunkCache#getChunk}'s main-thread bounce by reading
 * directly from {@link ChunkHolder} futures. Only reads already-loaded chunks;
 * returns null for unloaded chunks (caller should treat as escaped).
 * <p>
 * This is safe because:
 * <ul>
 *   <li>{@link LevelChunk#getBlockState} reads from {@code PalettedContainer} which is
 *       thread-safe (Mojang's light engine reads it off-thread)</li>
 *   <li>{@code LevelChunk.sections} is final and sections are never nulled</li>
 *   <li>Stale reads are acceptable — block change events trigger re-validation</li>
 * </ul>
 */
public class AsyncBlockReader {
    @Getter
    private final ServerLevel level;
    private final ChunkMap chunkMap;

    // Single-entry cache to avoid repeated map lookups for the same chunk
    private long cachedChunkKey = ChunkPos.INVALID_CHUNK_POS;
    @Nullable
    private LevelChunk cachedChunk;

    public AsyncBlockReader(ServerLevel level) {
        this.level = level;
        this.chunkMap = ((AccessorServerChunkCache) level.getChunkSource()).tfg$getChunkMap();
    }

    /**
     * Get the block state at the given position without bouncing to the main thread.
     * Returns null if the chunk is not loaded — caller must handle this (e.g. treat as unloaded escape).
     */
    @Nullable
    public BlockState getBlockState(BlockPos pos) {
        LevelChunk chunk = getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockState(pos);
    }

    /**
     * Check if a chunk is loaded at the given position.
     */
    public boolean hasChunkAt(BlockPos pos) {
        return getChunk(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    /**
     * Delegates to the level for build height checks (no world access needed).
     */
    public boolean isOutsideBuildHeight(int y) {
        return level.isOutsideBuildHeight(y);
    }

    /**
     * Gets a loaded LevelChunk without blocking. Returns null if not loaded.
     * Mirrors the logic of {@link ServerChunkCache#getChunkNow} minus the thread check.
     */
    @Nullable
    private LevelChunk getChunk(int chunkX, int chunkZ) {
        long key = ChunkPos.asLong(chunkX, chunkZ);
        if (key == cachedChunkKey) {
            return cachedChunk;
        }

        ChunkHolder holder = ((AccessorChunkMap) chunkMap).tfg$invokeGetVisibleChunkIfPresent(key);
        if (holder == null) {
            cachedChunkKey = key;
            cachedChunk = null;
            return null;
        }

        // Read the already-completed future without blocking.
        // Don't use holder.currentlyLoading — it's not volatile, unsafe to read off-thread.
        ChunkAccess access = holder.getFutureIfPresent(ChunkStatus.FULL)
                .getNow(ChunkHolder.UNLOADED_CHUNK)
                .left()
                .orElse(null);

        if (access instanceof LevelChunk lc) {
            cachedChunkKey = key;
            cachedChunk = lc;
            return lc;
        }

        cachedChunkKey = key;
        cachedChunk = null;
        return null;
    }
}
