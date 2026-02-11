package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;

@Mixin(value = GTRecipeWidget.class, remap = false)
public class GTRecipeWidgetMixin {

    @Unique
    private int tfg$dimensionOffset = 17;

    @ModifyArg(method = "setRecipeWidget", at = @At(value = "INVOKE", target = "com/gregtechceu/gtceu/common/recipe/condition/DimensionCondition.setupDimensionMarkers (II)Lcom/gregtechceu/gtceu/api/gui/widget/SlotWidget;"), index = 1)
    private int tfg$shift(int yOffset) {
        tfg$dimensionOffset -= 17;
        return yOffset + tfg$dimensionOffset;
    }
}
