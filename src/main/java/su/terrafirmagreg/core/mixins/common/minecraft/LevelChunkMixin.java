package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;

import su.terrafirmagreg.core.world.new_ow_wg.chunk.FluidPostProcess;

/**
 * Flags when the fluid tick comes from the generation process, so that sluice checks can be skipped.
 * Also, for a block in the edge of a chunk, checks if its tick can be skipped entirely.
 * Interior blocks have already been checked on a worker thread.
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Redirect(method = "postProcessGeneration", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void tfg$flagPostProcessFluidTick(FluidState fluidState, Level level, BlockPos pos) {
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
