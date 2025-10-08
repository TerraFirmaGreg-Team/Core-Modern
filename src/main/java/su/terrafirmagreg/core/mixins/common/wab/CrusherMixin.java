package su.terrafirmagreg.core.mixins.common.wab;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ninni.species.server.entity.mob.update_1.Stackatick;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.wanmine.wab.entity.Crusher;

/**
 * Makes the crusher hostile to players (and stackaticks) but at a low priority
 */

@Mixin(value = Crusher.class)
public abstract class CrusherMixin extends Mob {

    protected CrusherMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    protected void tfg$registerGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Player.class, true, true));
        this.goalSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Stackatick.class, true, true));
    }
}
