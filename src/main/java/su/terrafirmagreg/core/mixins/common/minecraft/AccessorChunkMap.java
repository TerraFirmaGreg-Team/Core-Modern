package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

/**
 * @author Redeix, Mqrius
 * @reason exposes getChunks and getVisibleChunkIfPresent methods.
 */
@Mixin(ChunkMap.class)
public interface AccessorChunkMap {
    @Invoker("getChunks")
    Iterable<ChunkHolder> tfg$invokeGetChunks();

    @Invoker("getVisibleChunkIfPresent")
    ChunkHolder tfg$invokeGetVisibleChunkIfPresent(long pos);
}
