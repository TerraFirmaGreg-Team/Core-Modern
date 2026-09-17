package su.terrafirmagreg.core.mixins.client.tmrv;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

import mezz.jei.common.Internal;

import su.terrafirmagreg.core.TFGCore;

/**
 * A mixin for the TooManyRecipeViewersMod class allowing for proper cleanup and
 * resource handling during player logout events.
 * Prevents crashing when logging out within the first few seconds of world load.
 */
@Mixin(targets = "dev.nolij.toomanyrecipeviewers.TooManyRecipeViewersMod", remap = false)
public class TooManyRecipeViewersModMixin {

    @Inject(method = "onLoggingOut", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$onLoggingOutSafe(ClientPlayerNetworkEvent.LoggingOut event, CallbackInfo ci) {
        ci.cancel();
        try {
            Class<?> tmrvClass = null;
            for (String className : new String[] {
                    "dev.nolij.toomanyrecipeviewers.TooManyRecipeViewers",
                    "dev.nolij.toomanyrecipeviewers.plugin.TooManyRecipeViewers"
            }) {
                try {
                    tmrvClass = Class.forName(className);
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }

            if (tmrvClass != null) {
                Field runtimeField = null;
                try {
                    runtimeField = tmrvClass.getDeclaredField("runtime");
                    runtimeField.setAccessible(true);
                } catch (NoSuchFieldException ignored) {
                }

                if (runtimeField != null) {
                    Object runtime = runtimeField.get(null);
                    if (runtime != null) {
                        runtimeField.set(null, null);
                        try {
                            Internal.setRuntime(null);
                            Internal.setServerConnection(null);
                        } catch (Throwable ignored) {
                        }

                        Object pluginManager = null;
                        for (String fieldName : new String[] { "jeiPluginManager", "jeiPlugins", "plugins" }) {
                            try {
                                Field f = runtime.getClass().getDeclaredField(fieldName);
                                f.setAccessible(true);
                                pluginManager = f.get(runtime);
                                if (pluginManager != null) {
                                    break;
                                }
                            } catch (NoSuchFieldException ignored) {
                            }
                        }

                        if (pluginManager != null) {
                            try {
                                Method method = pluginManager.getClass().getMethod("onRuntimeUnavailable");
                                method.setAccessible(true);
                                method.invoke(pluginManager);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            TFGCore.LOGGER.error("Error during TMRV logout cleanup", t);
        }
    }
}
