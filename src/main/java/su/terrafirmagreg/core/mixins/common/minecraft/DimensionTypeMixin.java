package su.terrafirmagreg.core.mixins.common.minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

import su.terrafirmagreg.core.world.WorldgenData;

@Mixin(value = DimensionType.class, remap = true)
public class DimensionTypeMixin {

    @Shadow
    @Final
    private ResourceLocation effectsLocation;

    @Inject(method = "height", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$height(CallbackInfoReturnable<Integer> cir) {
        if (effectsLocation.getPath().equals("overworld")) {
            cir.setReturnValue(WorldgenData.MOUNTAIN_SCALING.height());
        }
    }

    @Inject(method = "logicalHeight", at = @At("HEAD"), remap = true, cancellable = true)
    private void tfg$logicalHeight(CallbackInfoReturnable<Integer> cir) {
        if (effectsLocation.getPath().equals("overworld")) {
            cir.setReturnValue(WorldgenData.MOUNTAIN_SCALING.logicalHeight());
        }
    }
}
