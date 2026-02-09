package su.terrafirmagreg.core.mixins.common.gtceu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;

@Mixin(value = DimensionCondition.class, remap = false)
public class DimensionConditionMixin {

    @ModifyVariable(method = "setupDimensionMarkers", at = @At(value = "HEAD"), ordinal = 0)
    private int tfg$shiftGlobe(int value) {
        return (int) (value * 1.2286f);
    }
}
