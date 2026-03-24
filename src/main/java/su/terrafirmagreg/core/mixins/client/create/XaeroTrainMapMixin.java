package su.terrafirmagreg.core.mixins.client.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.compat.trainmap.XaeroTrainMap;

/**
 * Respect interface scale on HiDPI displays
 * Backport of <a href="https://github.com/Creators-of-Create/Create/pull/9736">Create PR #9736</a>
 */
@Mixin(value = XaeroTrainMap.class, remap = false)
public abstract class XaeroTrainMapMixin {

    @ModifyVariable(method = "onRender", at = @At("STORE"), name = "scale")
    private static double tfg$respectInterfaceScale(double scale, @Local(name = "window") Window window) {
        double interfaceScale = (double) window.getWidth() / window.getScreenWidth();
        return scale / interfaceScale;
    }
}
