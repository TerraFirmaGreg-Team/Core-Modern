package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import java.io.IOException;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import dan200.computercraft.shared.computer.core.ResourceMount;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.config.TFGConfig;

@Mixin(value = ResourceMount.class, remap = false)
public class ResourceMountMixin {

    @Shadow
    @Final
    private String namespace;

    @Shadow
    @Final
    private String subPath;

    @Shadow
    private ResourceManager manager;

    @Unique
    private static final String tfg$CC_NAMESPACE = "computercraft";

    @Unique
    private static final String tfg$CC_ROM_PATH = "lua/rom";

    @Inject(method = "getFileContents", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$getFileContents(String path, @Coerce Object file, CallbackInfoReturnable<byte[]> cir) throws IOException {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (!tfg$CC_NAMESPACE.equals(namespace) || !tfg$CC_ROM_PATH.equals(subPath)) {
            return;
        }

        var overrideLocation = ResourceLocation.fromNamespaceAndPath(
                TFGCore.MOD_ID,
                "computercraft/" + subPath + "/" + path);

        var resource = manager.getResource(overrideLocation).orElse(null);
        if (resource == null) {
            return;
        }

        try (var stream = resource.open()) {
            cir.setReturnValue(stream.readAllBytes());
        }
    }
}
