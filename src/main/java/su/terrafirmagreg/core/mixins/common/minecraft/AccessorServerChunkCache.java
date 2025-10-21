package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;

/**
 * @author Redeix
 * @reason exposes chunkMap field.
 */
@Mixin(ServerChunkCache.class)
public interface AccessorServerChunkCache {
    @Accessor("chunkMap")
    ChunkMap tfg$getChunkMap();
}
