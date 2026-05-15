package su.terrafirmagreg.core.mixins.common.firmaciv;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.alekiponi.firmaciv.events.BlockEventHandler;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;

/**
 * Replace setBlock calls with setFinalState in the dugout canoe carving process.
 * This gets properly synced to other clients and correctly prevents the event from propagating further.
 * Fixes bystander clients not seeing the canoe getting carved.
 */
@Mixin(value = BlockEventHandler.class, remap = false)
public class BlockEventHandlerMixin {

    @Redirect(method = "processCanoeComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", remap = true))
    private static boolean tfg$syncCanoeCarvedState(LevelAccessor instance, BlockPos pos, BlockState state, int flags, @Local(argsOnly = true) BlockEvent.BlockToolModificationEvent event) {
        event.setFinalState(state);
        return true;
    }

    @Redirect(method = "convertLogToCanoeComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", remap = true))
    private static boolean tfg$syncCanoeConversion(LevelAccessor instance, BlockPos pos, BlockState state, int flags, @Local(argsOnly = true) BlockEvent.BlockToolModificationEvent event) {
        event.setFinalState(state);
        return true;
    }
}
