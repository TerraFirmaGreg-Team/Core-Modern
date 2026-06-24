package su.terrafirmagreg.core.mixins.common.species;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.ninni.species.server.entity.mob.update_3.Ghoul;

@Mixin(value = Ghoul.SearchGoal.class)
public class SearchGoalMixin {

    // Lowers the volume (and thus the attenuation distance) of the searching sound effect that ghouls do

    @ModifyConstant(method = "start", constant = @Constant(floatValue = 3f))
    private float tfg$modifySearchVolume(float constant) {
        return 1.7f;
    }
}
