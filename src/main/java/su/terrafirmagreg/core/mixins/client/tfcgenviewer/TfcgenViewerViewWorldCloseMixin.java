package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.notenoughmail.tfcgenviewer.screen.ViewWorldScreen;

import net.minecraft.client.gui.screens.Screen;

import su.terrafirmagreg.core.world.new_ow_wg.TfgClientPreviewState;

/**
 * Clears TFCGenViewer preview session when {@link ViewWorldScreen} is removed. Implemented via {@link Screen#removed()}
 * on the base class so the injection always resolves; mixing into mod {@code onClose} failed on some builds.
 */
@Mixin(Screen.class)
public class TfcgenViewerViewWorldCloseMixin {

    @Inject(method = "removed", at = @At("HEAD"))
    private void tfg$resetPreviewIfViewWorld(CallbackInfo ci) {
        if (((Object) this) instanceof ViewWorldScreen) {
            TfgClientPreviewState.leave();
        }
    }
}
