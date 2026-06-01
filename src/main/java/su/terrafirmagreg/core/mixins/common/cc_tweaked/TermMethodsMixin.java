package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dan200.computercraft.api.lua.Coerced;
import dan200.computercraft.core.apis.TermMethods;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = TermMethods.class, remap = false)
public class TermMethodsMixin {

    private static boolean tfg$loggedUtf8CompatState;

    @Inject(method = "write", at = @At("HEAD"), remap = false)
    private void tfg$debugUtf8Compat(Coerced<?> text, CallbackInfo ci) {
        if (tfg$loggedUtf8CompatState) {
            return;
        }

        tfg$loggedUtf8CompatState = true;
        TFGCore.LOGGER.info("CC:Tweaked UTF-8 compatibility patches are {}",
                TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get() ? "enabled" : "disabled");
    }
}
