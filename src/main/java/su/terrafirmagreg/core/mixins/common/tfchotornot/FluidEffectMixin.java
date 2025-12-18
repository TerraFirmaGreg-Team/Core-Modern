package su.terrafirmagreg.core.mixins.common.tfchotornot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.utils.EntityDamageUtil;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import tfchotornot.EventHandler;

@Mixin(value = EventHandler.FluidEffect.class, remap = false)
public class FluidEffectMixin {

    // Deal cold damage when holding cold things instead of giving very short slowness and weakness debuffs

    @Inject(method = "lambda$static$1", at = @At(value = "HEAD", target = "Ltfchotornot/EventHandler$FluidEffect;COLD:Ltfchotornot/EventHandler$FluidEffect;"), remap = false, cancellable = true)
    private static void tfg$coldLambda(Player player, Level level, CallbackInfo ci) {
        EntityDamageUtil.applyFrostDamage(player, 1);
        ci.cancel();
    }
}
