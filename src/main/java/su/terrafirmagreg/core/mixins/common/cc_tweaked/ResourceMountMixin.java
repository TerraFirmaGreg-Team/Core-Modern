package su.terrafirmagreg.core.mixins.common.cc_tweaked;

import java.io.IOException;
import java.io.InputStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;

import dan200.computercraft.impl.AbstractComputerCraftAPI;
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
    private static final String tfg$CC_LUA_PATH = "lua";

    @Inject(method = "getFileContents", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfg$getFileContents(String path, @Coerce Object file, CallbackInfoReturnable<byte[]> cir) throws IOException {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (!tfg$CC_NAMESPACE.equals(namespace)) {
            return;
        }

        if (!tfg$isLuaPath(subPath)) {
            return;
        }

        var overrideLocation = tfg$overrideLocation(subPath + "/" + path);
        var resource = manager.getResource(overrideLocation).orElse(null);

        if (resource == null) {
            return;
        }

        try (var stream = resource.open()) {
            cir.setReturnValue(stream.readAllBytes());
        }
    }

    @Unique
    private static boolean tfg$isLuaPath(String path) {
        return tfg$CC_LUA_PATH.equals(path) || path.startsWith(tfg$CC_LUA_PATH + "/");
    }

    @Unique
    private static ResourceLocation tfg$overrideLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                TFGCore.MOD_ID,
                "computercraft/" + path);
    }
}

@Mixin(value = AbstractComputerCraftAPI.class, remap = false)
class AbstractComputerCraftAPIMixin {

    @Unique
    private static final String tfg$CC_NAMESPACE = "computercraft";

    @Unique
    private static final String tfg$CC_LUA_PATH = "lua";

    @Inject(method = "getResourceFile", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tfg$getResourceFile(
            MinecraftServer server,
            String domain,
            String subPath,
            CallbackInfoReturnable<InputStream> cir) {
        if (!TFGConfig.COMMON.ENABLE_CC_UTF8_COMPAT.get()) {
            return;
        }

        if (!tfg$CC_NAMESPACE.equals(domain)) {
            return;
        }

        if (!tfg$isLuaPath(subPath)) {
            return;
        }

        var overrideLocation = tfg$overrideLocation(subPath);
        var resource = server.getResourceManager().getResource(overrideLocation).orElse(null);

        if (resource == null) {
            return;
        }

        try {
            cir.setReturnValue(resource.open());
        } catch (IOException ignored) {
        }
    }

    @Unique
    private static boolean tfg$isLuaPath(String path) {
        return tfg$CC_LUA_PATH.equals(path) || path.startsWith(tfg$CC_LUA_PATH + "/");
    }

    @Unique
    private static ResourceLocation tfg$overrideLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                TFGCore.MOD_ID,
                "computercraft/" + path);
    }
}
