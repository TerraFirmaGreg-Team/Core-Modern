/*
 * This file includes code from TerraFirmaCraft (https://github.com/TerraFirmaCraft/TerraFirmaCraft)
 * Copyright (c) 2020 alcatrazEscapee
 * Licensed under the EUPLv1.2 License
 */
package su.terrafirmagreg.core.mixins.client.tfc.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.client.screen.NutritionScreen;
import net.dries007.tfc.compat.jei.JEIIntegration;
import net.dries007.tfc.compat.jei.TFCInventoryGuiHandler;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;

import su.terrafirmagreg.core.client.screen.TFGNutritionScreen;

@Mixin(value = JEIIntegration.class, remap = false)
public abstract class JEIIntegrationMixin {

    /**
     * Replaces TFC's NutritionScreen with TFGNutritionScreen in JEI/EMI GUI handler registration.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Redirect(method = "registerGuiHandlers", at = @At(value = "INVOKE", target = "Lmezz/jei/api/registration/IGuiHandlerRegistration;addGuiContainerHandler(Ljava/lang/Class;Lmezz/jei/api/gui/handlers/IGuiContainerHandler;)V"), remap = false)
    private void tfg$replaceNutritionScreenHandler(IGuiHandlerRegistration registry, Class<?> screenClass, IGuiContainerHandler<?> handler) {
        if (screenClass == NutritionScreen.class) {
            registry.addGuiContainerHandler(TFGNutritionScreen.class, new TFCInventoryGuiHandler<>());
        } else {
            registry.addGuiContainerHandler((Class) screenClass, (IGuiContainerHandler) handler);
        }
    }
}
