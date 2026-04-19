package su.terrafirmagreg.core.mixins.common.tfc.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import su.terrafirmagreg.core.common.data.TFGTags;

@Mixin(value = EnvironmentHelpers.class, remap = false)
public class EnvironmentHelpersMixin {
    @Inject(method = "doIcicles", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;above()Lnet/minecraft/core/BlockPos;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)

    private static void noIciclesTag(Level level, BlockPos lcgPos, float temperature, CallbackInfo ci, RandomSource random, BlockPos iciclePos) {
        if (iciclePos != null) {
            BlockState stateAbove = level.getBlockState(iciclePos.above());

            if (stateAbove.is(TFGTags.Blocks.NoIcicles)) {
                ci.cancel();
            }
        }
    }
}
