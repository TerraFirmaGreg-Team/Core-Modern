package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.wanmine.wab.entity.Eater;
import net.wanmine.wab.entity.goals.eater.SleepingCheckGoal;

/**
 * Makes the eater always hostile to players
 */

@Mixin(value = Eater.class)
public abstract class EaterMixin extends Mob {

    protected EaterMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow(remap = false)
    public abstract int getNomCount();

    @Inject(method = "registerGoals", at = @At("TAIL"), remap = true)
    protected void tfg$registerGoals(CallbackInfo ci) {
        if (getNomCount() < 10) {
            this.goalSelector.addGoal(1, new SleepingCheckGoal((Eater) (Object) this, new NearestAttackableTargetGoal<>(this, Player.class, false, false)));
        }
    }
}
