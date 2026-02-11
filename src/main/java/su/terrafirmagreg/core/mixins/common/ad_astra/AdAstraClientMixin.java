package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

import earth.terrarium.adastra.client.AdAstraClient;

@Mixin(AdAstraClient.class)
@Debug(export = true)
public class AdAstraClientMixin {

    /*@Redirect(method = "registerScreens", at = @At(value = "INVOKE", target = "net/minecraft/client/gui/screens/MenuScreens.register (Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/client/gui/screens/MenuScreens$ScreenConstructor;)V", ordinal = 14))
    private static void tfg$replaceScreen(MenuType<? extends PlanetsMenu> pType, MenuScreens.ScreenConstructor<PlanetsMenu, TFGPlanetsScreen> pFactory) {
        return TFGPlanetsScreen::new;
    }*/
}
