package su.terrafirmagreg.core.mixins.common.arthropocolypse;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.dh.arthropocolypse.entity.Mealworm_Beetle")
public class MealwormBeetleMixin {

	// registerGoals
	@SuppressWarnings("DataFlowIssue")
	@Inject(method = "m_8099_", at = @At("TAIL"))
	protected void tfg$registerGoals(CallbackInfo ci) {
		Animal mob = (Animal) (Object) this;

		mob.goalSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, Player.class, true, true));
	}
}
