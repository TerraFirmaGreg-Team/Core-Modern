package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stops bone meal from working on various mushroom blocks (includes modded ones too like ad astra's),
 * also makes mushroom blocks survive on more blocks
 */

@Mixin(value = MushroomBlock.class)
public class MushroomBlockMixin {

    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    public void tfg$isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    public void tfg$canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Block.isFaceFull(pState.getCollisionShape(pLevel, pPos.below()), Direction.UP));
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void tfg$randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom, CallbackInfo ci) {
        ci.cancel();
    }
}
