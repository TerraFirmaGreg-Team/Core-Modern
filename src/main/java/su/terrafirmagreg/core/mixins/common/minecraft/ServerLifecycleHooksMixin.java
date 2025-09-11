package su.terrafirmagreg.core.mixins.common.minecraft;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mixin(ServerLifecycleHooks.class)
public abstract class ServerLifecycleHooksMixin {

    @Inject(method = "getServerConfigPath", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getServerConfigPath(MinecraftServer server, CallbackInfoReturnable<Path> cir) {
        var globalServerConfigsPath = FMLPaths.getOrCreateGameRelativePath(Paths.get("defaultconfigs"));
        cir.setReturnValue(globalServerConfigsPath);
    }
}
