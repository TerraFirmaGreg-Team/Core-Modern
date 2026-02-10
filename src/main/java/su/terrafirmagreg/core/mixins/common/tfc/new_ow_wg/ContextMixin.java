package su.terrafirmagreg.core.mixins.common.tfc.new_ow_wg;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.world.region.Region;
import net.dries007.tfc.world.region.RegionGenerator;

import su.terrafirmagreg.core.config.TFGConfig;
import su.terrafirmagreg.core.world.new_ow_wg.region.TFGRegionTask;

@Mixin(value = RegionGenerator.Context.class, remap = false)
public abstract class ContextMixin {

    @Shadow
    @Final
    public Region region;

    @Inject(method = "runTasks", at = @At("HEAD"), remap = false, cancellable = true)
    private void tfg$runTasks(CallbackInfoReturnable<RegionGenerator.Context> cir) {
        if (TFGConfig.SERVER.enableNewTFCWorldgen.get()) {
            for (TFGRegionTask task : TFGRegionTask.VALUES) {
                task.task.apply((RegionGenerator.Context) (Object) this);
            }
            cir.setReturnValue((RegionGenerator.Context) (Object) this);
        }
    }
}
