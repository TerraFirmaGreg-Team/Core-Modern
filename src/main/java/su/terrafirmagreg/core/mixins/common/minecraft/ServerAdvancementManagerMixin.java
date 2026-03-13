package su.terrafirmagreg.core.mixins.common.minecraft;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(ServerAdvancementManager.class)
public abstract class ServerAdvancementManagerMixin {

    /**
     * Removes all advancements from loading, skipping JSON parsing entirely.
     * Make sure to call ClientBookRegistry.INSTANCE.reload() manually,
     * otherwise Patchouli's ClientAdvancements::onClientPacket gate prevents initial loading.
     * @see su.terrafirmagreg.core.client.ForgeClientEventListener#onClientLogin
     */
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"), cancellable = true)
    private void tfg$removeAdvancements(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler, CallbackInfo ci) {
        ci.cancel();
    }
}
