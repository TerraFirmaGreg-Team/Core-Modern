package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.ForgeEventHandler;
import net.minecraftforge.event.level.BlockEvent;

@Mixin(value = ForgeEventHandler.class, remap = false)
public class ForgeEventHandlerMixin {

    // Forcibly disable nether portals because there's some funky mod conflict going on with
    // settings overwriting each other

    @Inject(method = "onCreateNetherPortal", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$onCreateNetherPortal(BlockEvent.PortalSpawnEvent event, CallbackInfo ci) {
        event.setCanceled(true);
        ci.cancel();
    }
}
