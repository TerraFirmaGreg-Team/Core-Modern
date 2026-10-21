package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import dev.dh.arthropocolypse.entity.Stag_Beetle;

@Mixin(Stag_Beetle.class)
public class StagBeetleMixin {

    // registerGoals
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "registerGoals", at = @At("TAIL"))
    protected void tfg$registerGoals(CallbackInfo ci) {
        Animal mob = (Animal) (Object) this;

        mob.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
    }
}
