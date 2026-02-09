package su.terrafirmagreg.core.mixins.client.carryon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.client.event.ScreenEvent;

import earth.terrarium.adastra.client.screens.PlanetsScreen;

@Pseudo
@Mixin(targets = "tschipp.carryon.events.ClientEvents", remap = false)
public class ClientEventsMixin {

    @Inject(method = "onGuiInit(Lnet/minecraftforge/client/event/ScreenEvent$Init$Pre;)V", at = @At(value = "INVOKE", target = "tschipp/carryon/common/carry/CarryOnDataManager.getCarryData (Lnet/minecraft/world/entity/player/Player;)Ltschipp/carryon/common/carry/CarryOnData;"), cancellable = true)
    private static void tfg$rocketScreenBypass(ScreenEvent.Init.Pre event, CallbackInfo ci) {
        if (event.getScreen() instanceof PlanetsScreen) {
            ci.cancel();
        }
    }
}
