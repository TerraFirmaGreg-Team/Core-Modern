package su.terrafirmagreg.core.common.environment;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
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
 * {@link LevelChunk#getBlockState} reads from {@code PalettedContainer} which is
 * thread-safe (Mojang's light engine reads it off-thread).
 * {@code LevelChunk.sections} is final and sections are never nulled.
 * Stale reads are acceptable because block change events trigger re-validation
 * The main fragile bit is async block entity access, but we're only ever reading, and
 * we don't care much about stale reads and we don't iterate so there's no risk of CME.
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
     * Get the block state at the given position async.
     * Returns null if the chunk is not loaded.
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
     * Get the block entity at the given position async.
     * Returns null if the chunk is not loaded or there is no block entity.
     */
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        LevelChunk chunk = getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }

    /** Check if a chunk is loaded at the given position. */
    public boolean hasChunkAt(BlockPos pos) {
        return getChunk(pos.getX() >> 4, pos.getZ() >> 4) != null;
    }

    /** Delegates to the level for build height checks. */
    public boolean isOutsideBuildHeight(int y) {
        return level.isOutsideBuildHeight(y);
    }

    /**
     * Get a loaded LevelChunk without blocking, returns null if not loaded.
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
