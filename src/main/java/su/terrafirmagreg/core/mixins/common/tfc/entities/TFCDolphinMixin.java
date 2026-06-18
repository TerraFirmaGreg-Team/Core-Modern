package su.terrafirmagreg.core.mixins.common.tfc.entities;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.aquatic.AmphibiousAnimal;
import net.dries007.tfc.common.entities.aquatic.TFCDolphin;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

@Mixin(TFCDolphin.class)
public class TFCDolphinMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addAmphibiousTarget(CallbackInfo ci) {
        ((TFCDolphin) (Object) this).targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>((TFCDolphin) (Object) this, AmphibiousAnimal.class, 1000, true, false,
                        e -> Helpers.isEntity(e, TFCTags.Entities.HUNTED_BY_OCEAN_PREDATORS)));
    }
}
