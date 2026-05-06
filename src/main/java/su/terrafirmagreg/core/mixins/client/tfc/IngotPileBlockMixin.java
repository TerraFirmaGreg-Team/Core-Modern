package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.blocks.devices.IngotPileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.block.IngotPedestalBlock.IngotPedestalBlockEntity;

@Mixin(value = IngotPileBlock.class, remap = false)
public abstract class IngotPileBlockMixin {

    /**
     * Fired when a pile block is removed.
     *
     * TFC's onRemove already checks `newState.getBlock() != this`
     * but we must fire even when transitioning between pile types (single to double),
     * so we check for non-suppression rather than the block type.
     */
    @Inject(method = "onRemove", at = @At("HEAD"), remap = false)
    private void tfg$onRemove(
            BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean isMoving,
            CallbackInfo ci) {

        if (IngotPedestalBlockEntity.SUPPRESSING)
            return;
        if (level.isClientSide())
            return;

        // check if the block got replaced rather than just updated
        if (newState.getBlock() == state.getBlock())
            return;

        IngotPedestalBlockEntity.notifyExternalColumnChange(level, pos);
    }

    // TODO: I am not quite sure this can ever happen.
    /** Fired when a pile block is newly placed. */
    @Inject(method = "onPlace", at = @At("HEAD"), remap = false)
    private void tfg$onPlace(
            BlockState state, Level level, BlockPos pos,
            BlockState oldState, boolean isMoving,
            CallbackInfo ci) {

        if (IngotPedestalBlockEntity.SUPPRESSING)
            return;
        if (level.isClientSide())
            return;

        // check if the block got replaced rather than just updated
        if (oldState.getBlock() == state.getBlock())
            return;

        IngotPedestalBlockEntity.notifyExternalColumnChange(level, pos);
    }
}
