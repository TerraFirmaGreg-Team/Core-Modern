package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.inventory.MenuType;

import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.registry.ModMenus;
import earth.terrarium.botarium.common.registry.RegistryHelpers;

import su.terrafirmagreg.core.common.data.screens.TFGPlanetsMenu;

@Mixin(value = ModMenus.class, remap = false)
public class ModMenusMixin {

    @Redirect(method = "lambda$static$14()Lnet/minecraft/world/inventory/MenuType;", at = @At(value = "INVOKE", target = "earth/terrarium/botarium/common/registry/RegistryHelpers.createMenuType (Learth/terrarium/botarium/common/registry/RegistryHelpers$MenuFactory;)Lnet/minecraft/world/inventory/MenuType;"))
    private static MenuType<PlanetsMenu> tfg$test(RegistryHelpers.MenuFactory<PlanetsMenu> factory) {
        return RegistryHelpers.createMenuType(TFGPlanetsMenu::new);
    }
}
