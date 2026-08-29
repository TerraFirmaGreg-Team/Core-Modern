package su.terrafirmagreg.core.mixins.client.minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.pipeline.RenderTarget;

// Distant Horizons doesn't like when enableStencil() is called after it has done its own setup.
// This patches the configured buffers before they are ever created, so that stencil support is
// there when DH caches everything. enableStencil calls shouldn't be needed anywhere else after this.
// This should fix our own nutrient graph, and also all TACZ scoped weapons.
@Mixin(RenderTarget.class)
public abstract class RenderTargetStencilMixin {

    @Shadow
    private boolean stencilEnabled;

    @Inject(method = "createBuffers", at = @At("HEAD"))
    private void tfg$forceStencil(int width, int height, boolean clearError, CallbackInfo ci) {
        this.stencilEnabled = true;
    }
}
