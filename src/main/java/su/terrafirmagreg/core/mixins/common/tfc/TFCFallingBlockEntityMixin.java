package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.misc.TFCFallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ensure blocks tagged with TFCTags.Blocks.SUPPORTS_LANDSLIDE are not considered "fall-through".
 * This prevents Landslide logic from having a road fall right into another.
 */
@Mixin(TFCFallingBlockEntity.class)
public class TFCFallingBlockEntityMixin {
    // 5-arg method:
    @Inject(method = "canFallThrough(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onCanFallThroughFull(BlockGetter level, BlockPos pos, BlockState state, Direction fallingDirection, BlockState fallingState, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (state.is(TFCTags.Blocks.SUPPORTS_LANDSLIDE)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
        }
    }

    // 3-arg method:
    @Inject(method = "canFallThrough(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onCanFallThroughSimple(BlockGetter world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (state.is(TFCTags.Blocks.SUPPORTS_LANDSLIDE)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
        }
    }
}
