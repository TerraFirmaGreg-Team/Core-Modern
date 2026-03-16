package su.terrafirmagreg.core.mixins.client.patchouli;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;

import vazkii.patchouli.client.book.ClientBookRegistry;

@Mixin(value = ClientBookRegistry.class, remap = false)
public abstract class ClientBookRegistryMixin {

    @Unique
    private boolean tfg$hasReloaded = false;

    /**
     * Ensures Patchouli books are loaded on first book open.
     */
    @Inject(method = "displayBookGui", at = @At("HEAD"))
    private void tfg$ensureBooksLoaded(ResourceLocation bookStr, ResourceLocation entryId, int page, CallbackInfo ci) {
        if (!tfg$hasReloaded) {
            ((ClientBookRegistry) (Object) this).reload();
            tfg$hasReloaded = true;
        }
    }
}
