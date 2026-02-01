package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.wanmine.wab.entity.Soarer;
import net.wanmine.wab.overlay.SoarerOverlay;

/**
 * This class contains code copied from Wan's Ancient Beasts, (c) WanMine, used with permission
 * --
 * Mixin to make the GUI for soarer flaps not collide with the default position for the minimap
 */

@Mixin(value = SoarerOverlay.class, remap = false)
public class SoarerOverlayMixin {

    @Shadow
    @Final
    private static ResourceLocation WING_FLAP;

    @Shadow
    @Final
    private static ResourceLocation WING_FLAP_USED;

    @Inject(method = "eventHandler", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$eventHandler(RenderGuiEvent.Pre event, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Entity e = player.getVehicle();
            if (e instanceof Soarer soarer) {
                for (int i = 0; i < 15; ++i) {
                    event.getGuiGraphics().blit(i < soarer.getFlaps() ? WING_FLAP : WING_FLAP_USED, 110 + i * 10, 5, 0.0F, 0.0F, 9, 9, 9, 9);
                }
            }
        }
        ci.cancel();
    }
}
