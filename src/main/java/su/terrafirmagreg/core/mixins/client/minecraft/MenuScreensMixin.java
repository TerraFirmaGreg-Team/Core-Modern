package su.terrafirmagreg.core.mixins.client.minecraft;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.container.Container;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.world.inventory.MenuType;

import su.terrafirmagreg.core.client.screen.TFGNutritionScreen;

@Mixin(MenuScreens.class)
public abstract class MenuScreensMixin {

    @Shadow
    @Final
    private static Map<MenuType<?>, ScreenConstructor<?, ?>> SCREENS;

    /**
     * After TFC registers their NutritionScreen, replace it with TFGNutritionScreen.
     */
    @SuppressWarnings("rawtypes")
    @Inject(method = "register", at = @At("TAIL"))
    private static void tfg$replaceNutritionScreen(MenuType menuType, ScreenConstructor constructor, CallbackInfo ci) {
        if (TFCContainerTypes.NUTRITION.isPresent() && menuType == TFCContainerTypes.NUTRITION.get()) {
            ScreenConstructor<Container, TFGNutritionScreen> newConstructor = TFGNutritionScreen::new;
            SCREENS.put(menuType, newConstructor);
        }
    }
}
