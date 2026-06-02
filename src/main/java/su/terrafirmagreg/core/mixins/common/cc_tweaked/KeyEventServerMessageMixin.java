package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import java.nio.charset.StandardCharsets;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dan200.computercraft.shared.computer.menu.ComputerMenu;
import dan200.computercraft.shared.network.server.KeyEventServerMessage;
import dan200.computercraft.shared.network.server.ServerNetworkContext;

import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = KeyEventServerMessage.class, remap = false)
public class KeyEventServerMessageMixin {

    @Shadow
    @Final
    private KeyEventServerMessage.Action type;

    @Shadow
    @Final
    private int key;

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$handleUtf8Char(ServerNetworkContext context, ComputerMenu container, CallbackInfo ci) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (type != KeyEventServerMessage.Action.CHAR || key <= 255) {
            return;
        }

        if (key == 0 || key == '\r' || key == '\n') {
            ci.cancel();
            return;
        }

        var text = new String(Character.toChars(key));

        container.getComputer().queueEvent("char", new Object[] {
                text.getBytes(StandardCharsets.UTF_8)
        });

        ci.cancel();
    }
}
