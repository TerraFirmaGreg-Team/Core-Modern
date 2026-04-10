package su.terrafirmagreg.core.mixins.common.gtceu.buds;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;

import su.terrafirmagreg.core.common.data.blocks.TFGBlocks_Buds;

@Mixin(value = GTMaterialBlocks.class, remap = false)
public abstract class MaterialBlocksMixin {

    @Inject(method = "generateOreIndicators", at = @At(value = "TAIL"))
    private static void tfg$generateOreIndicators(CallbackInfo ci) {
        TFGBlocks_Buds.generateBudIndicators();
    }

}
