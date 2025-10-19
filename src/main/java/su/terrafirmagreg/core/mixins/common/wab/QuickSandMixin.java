package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.wanmine.wab.block.QuickSand;

/**
 * Stops quicksand blocks from trapping players and making them suffocate (despite being in a space suit)
 */

@Mixin(value = QuickSand.class, remap = false)
public class QuickSandMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), remap = true, cancellable = true)
    public void tfg$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
