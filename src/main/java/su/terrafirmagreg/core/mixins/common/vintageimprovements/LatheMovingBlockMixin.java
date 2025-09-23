package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.negodya1.vintageimprovements.content.kinetics.lathe.LatheMovingBlock;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = LatheMovingBlock.class, remap = false)
public class LatheMovingBlockMixin {

    // Stops the lathe from being wrenchable to rotate it, because that crashes

    @Inject(method = "onWrenched", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$onWrenched(BlockState state, UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        cir.setReturnValue(InteractionResult.FAIL);
    }
}
