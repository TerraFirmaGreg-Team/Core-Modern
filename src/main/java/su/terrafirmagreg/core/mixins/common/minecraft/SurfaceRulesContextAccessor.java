package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextAccessor {

    @Accessor("context")
    WorldGenerationContext tfg$GetWorldCtx();

    @Accessor("chunk")
    ChunkAccess tfg$GetChunk();

    @Accessor("randomState")
    RandomState tfg$getRandomState();

    @Accessor("blockX")
    int tfg$getBlockX();

    @Accessor("blockY")
    int tfg$getBlockY();

    @Accessor("blockZ")
    int tfg$getBlockZ();

    @Accessor("lastUpdateXZ")
    long tfg$getLastUpdateXZ();

    @Accessor("lastUpdateY")
    long tfg$getLastUpdateY();
}
