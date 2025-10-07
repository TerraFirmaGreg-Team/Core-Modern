package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.wanmine.wab.entity.Eater;
import net.wanmine.wab.entity.goals.eater.EaterAttackPlayerGoal;

/**
 * Makes the eater always hostile to players
 */

@Mixin(value = EaterAttackPlayerGoal.class)
public class EaterAttackPlayerGoalMixin extends NearestAttackableTargetGoal<Player> {

    @Shadow
    @Final
    protected Eater entity;

    public EaterAttackPlayerGoalMixin(Mob pMob, Class<Player> pTargetType, boolean pMustSee) {
        super(pMob, pTargetType, pMustSee);
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void tfg$canUse(CallbackInfoReturnable<Boolean> cir) {
        if (!this.entity.isBaby() && super.canUse()) {
            cir.setReturnValue(this.entity.getRoared());
        }
        cir.setReturnValue(false);
    }
}
