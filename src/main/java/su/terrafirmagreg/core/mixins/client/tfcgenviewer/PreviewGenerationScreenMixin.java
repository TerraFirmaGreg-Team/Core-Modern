package su.terrafirmagreg.core.mixins.client.tfcgenviewer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.notenoughmail.tfcgenviewer.screen.PreviewGenerationScreen;

import su.terrafirmagreg.core.world.new_ow_wg.TfgClientPreviewState;

/**
 * Create-world preview does not use {@link com.notenoughmail.tfcgenviewer.network.packets.ViewerResponsePacket}; clear any
 * leftover viewer session from a previous in-world session so {@link TfgClientPreviewState#useTfgOverworldPipeline()}
 * follows config / unresolved-session inference instead of stale {@code active}.
 */
@Mixin(value = PreviewGenerationScreen.class, remap = false)
public class PreviewGenerationScreenMixin {

    @Inject(method = "<init>", at = @At(value = "RETURN"), remap = false)
    private void tfg$afterInit(CallbackInfo ci) {
        TfgClientPreviewState.leave();
    }
}
