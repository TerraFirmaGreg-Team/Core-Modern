package su.terrafirmagreg.core.mixins.client.ldlib;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;

/**
 * Disables LDLib's redundant EMI rendering calls in ModularUIGuiContainer.render().
 * EMI's own Forge event hooks already handle this for any AbstractContainerScreen.
 * The redundant calls cause double rendering and leave stale depth buffer values that occlude items in EMI tooltips.
 */
@Mixin(value = ModularUIGuiContainer.class)
public abstract class ModularUIGuiContainerMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/lowdragmc/lowdraglib/LDLib;isEmiLoaded()Z"))
    private boolean tfg$disableRedundantEmiRendering() {
        // For this method only, pretend EMI isn't loaded at all
        return false;
    }
}
