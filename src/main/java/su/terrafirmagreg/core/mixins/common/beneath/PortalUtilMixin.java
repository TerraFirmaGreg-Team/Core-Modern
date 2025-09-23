package su.terrafirmagreg.core.mixins.common.beneath;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.eerussianguy.beneath.misc.PortalUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

@Mixin(value = PortalUtil.class, remap = false)
public abstract class PortalUtilMixin {

    @Inject(method = "onLivingDeath", at = @At("HEAD"), remap = false, cancellable = true)
    private static void onLivingDeath(LivingDeathEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "scanForOfferings", at = @At("HEAD"), remap = false, cancellable = true)
    private static void scanForOfferings(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isGoodOffering", at = @At("HEAD"), remap = false, cancellable = true)
    private static void isGoodOffering(LevelAccessor level, BlockPos pos, BlockState state,
            CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

}
