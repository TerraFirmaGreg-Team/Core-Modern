package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.region.Region;

import su.terrafirmagreg.core.world.new_ow_wg.region.IRegionPoint;

@Mixin(value = Region.class, remap = false)
public abstract class RegionMixin {

    @Shadow
    public abstract int index(int gridX, int gridZ);

    @Inject(method = "atInit", at = @At(value = "RETURN"), remap = false, cancellable = true)
    private void tfg$atInit(int gridX, int gridZ, CallbackInfoReturnable<Region.Point> cir) {
        Region.Point point = cir.getReturnValue();
        IRegionPoint pt = (IRegionPoint) point;
        pt.tfg$init(gridX, gridZ, index(gridX, gridZ));
        cir.setReturnValue(point);
    }
}
