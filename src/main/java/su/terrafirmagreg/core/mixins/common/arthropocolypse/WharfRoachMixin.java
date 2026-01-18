package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

@Pseudo
@Mixin(targets = "dev.dh.arthropocolypse.entity.Wharf_Roach")
public class WharfRoachMixin {

    // registerGoals
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "m_8099_", at = @At("TAIL"), remap = false)
    protected void tfg$registerGoals(CallbackInfo ci) {
        Animal mob = (Animal) (Object) this;

        mob.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
    }
}
