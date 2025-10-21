package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

/**
 * @author Redeix
 * @reason exposes getChunks method.
 */
@Mixin(ChunkMap.class)
public interface AccessorChunkMap {
    @Invoker("getChunks")
    Iterable<ChunkHolder> tfg$invokeGetChunks();
}
