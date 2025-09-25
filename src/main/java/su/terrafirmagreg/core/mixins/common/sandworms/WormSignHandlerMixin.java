package su.terrafirmagreg.core.mixins.common.sandworms;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.event.TickEvent;

@Pseudo
@Mixin(targets = "net.jelly.sandworm_mod.event.WormSignHandler")
public class WormSignHandlerMixin {
    @Redirect(method = "tickWS", at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(I)V"))
    private static void tfg$tickWS(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
    } // Effectively cancel the println
}
