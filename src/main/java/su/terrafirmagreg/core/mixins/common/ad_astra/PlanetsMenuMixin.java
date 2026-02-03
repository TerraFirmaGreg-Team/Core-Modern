package su.terrafirmagreg.core.mixins.common.ad_astra;

import org.spongepowered.asm.mixin.*;

import earth.terrarium.adastra.common.menus.PlanetsMenu;

@Mixin(value = PlanetsMenu.class, remap = false)
@Debug(export = true)
public abstract class PlanetsMenuMixin {

}
