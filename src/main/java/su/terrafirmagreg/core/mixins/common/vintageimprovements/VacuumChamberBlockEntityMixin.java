package su.terrafirmagreg.core.mixins.common.vintageimprovements;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.negodya1.vintageimprovements.content.kinetics.vacuum_chamber.VacuumChamberBlockEntity;

// NOTE: Delete once https://github.com/SSWTLZZ69/Create-Vintage-Improvements/pull/22 gets merged
@Mixin(value = VacuumChamberBlockEntity.class, remap = false)
public class VacuumChamberBlockEntityMixin {

    @ModifyVariable(method = "tick", at = @At("STORE"), name = "recipeSpeed", ordinal = 1)
    private float tfg$adjustRecipeSpeed(float recipeSpeed) {
        return recipeSpeed * 3000f / (recipeSpeed * 100f + 3000f);
    }
}
