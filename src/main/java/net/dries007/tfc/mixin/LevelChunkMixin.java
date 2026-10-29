/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.world.ChunkGeneratorExtension;
import net.dries007.tfc.world.FluidPostProcess;

/**
 * This is necessary to capture the {@code ProtoChunk} when promoting proto chunks to level chunks after world generation. At this time,
 * we copy chunk data from the proto chunk (stored via weak hash map on the {@code ChunkDataProvider}) to a capability stored on the
 * {@code LevelChunk}. However, capability initialization does not have access, so we initialize the capability empty, and replace its
 * contents after the fact.
 * <p>
 * In 1.21, with NeoForge supporting both Proto and Level chunk attachments, this should not be necessary as we can represent both forms
 * of the chunk data with an attachment and NeoForge will handle copying.
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin
{
    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V", at = @At("RETURN"))
    private void copyProtoChunkDataToLevelChunk(ServerLevel level, ProtoChunk chunk, LevelChunk.PostLoadProcessor postLoad, CallbackInfo ci)
    {
        if (level.getChunkSource().getGenerator() instanceof ChunkGeneratorExtension ex)
        {
            ex.chunkDataProvider().promotePartial(chunk, (LevelChunk) (Object) this);
        }
    }

    /**
     * Flags when the fluid tick comes from the generation process, so that sluice checks can be skipped.
     * Also, for a block in the edge of a chunk, checks if its tick can be skipped entirely.
     * Interior blocks have already been checked on a worker thread.
     */
    @Redirect(method = "postProcessGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void tickPostProcessFluid(FluidState fluidState, Level level, BlockPos pos)
    {
        if (FluidPostProcess.isEdgeColumn(pos.getX() & 15, pos.getZ() & 15) && FluidPostProcess.isInteriorSource(level, pos)) {
            return;
        }
        FluidPostProcess.inPostProcess = true;
        try {
            fluidState.tick(level, pos);
        } finally {
            FluidPostProcess.inPostProcess = false;
        }
    }
}
