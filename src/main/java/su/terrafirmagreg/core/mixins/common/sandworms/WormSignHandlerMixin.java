package su.terrafirmagreg.core.mixins.common.sandworms;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.jelly.sandworm_mod.event.WormSignHandler")
public class WormSignHandlerMixin {
    @Redirect(method = "lambda$tickWS$2", at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(I)V"), require = 0)
    private static void tfg$tickWS(java.io.PrintStream stream, int value) {
    } // Effectively cancel the println
}
