package su.terrafirmagreg.core.mixins.common.aliencraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import en.predator97427fr.aliencraft.block.ThornFlowerBlock;

@Mixin(value = ThornFlowerBlock.class, remap = false)
public class ThornFlowerBlockMixin {

    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState blockstate, boolean clientSide, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
