package su.terrafirmagreg.core.mixins.common.wab;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.wanmine.wab.entity.*;
import net.wanmine.wab.entity.goals.eater.*;
import net.wanmine.wab.init.data.WabTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Eater.class, remap = false)
public abstract class EaterMixin extends Animal {

	@Shadow
    public abstract int getNomCount();

	protected EaterMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "registerGoals", at = @At("HEAD"), cancellable = true)
	private void tfg$registerGoals(CallbackInfo ci)
	{
		var thisAsEater = (Eater) ((Object) this);

		super.registerGoals();
		if (this.getNomCount() < 10) {
			this.goalSelector.addGoal(0, new SleepingCheckGoal(thisAsEater, new FloatGoal(this)));
			this.goalSelector.addGoal(4, new SleepingCheckGoal(thisAsEater, new WaterAvoidingRandomStrollGoal(this, 0.4)));
			this.goalSelector.addGoal(6, new SleepingCheckGoal(thisAsEater, new LookAtPlayerGoal(this, Player.class, 6.0F)));
			this.goalSelector.addGoal(7, new SleepingCheckGoal(thisAsEater, new RandomLookAroundGoal(this)));
			this.goalSelector.addGoal(2, new SleepingCheckGoal(thisAsEater, new EaterBreedGoal(this, (double)0.5F)));
			this.goalSelector.addGoal(3, new SleepingCheckGoal(thisAsEater, new TemptGoal(this, (double)0.5F, Ingredient.of(WabTags.Items.EATER_FOOD), false)));
			this.goalSelector.addGoal(5, new SleepingCheckGoal(thisAsEater, new RoarFirstGoal(thisAsEater, Player.class, true)));
			this.goalSelector.addGoal(2, new SleepingCheckGoal(thisAsEater, new EaterAttackPlayerGoal(thisAsEater, Player.class, true)));
			this.goalSelector.addGoal(2, new SleepingCheckGoal(thisAsEater, new HurtByTargetGoal(this, new Class[0])));
			this.goalSelector.addGoal(2, new SleepingCheckGoal(thisAsEater, new MoveTowardsTargetGoal(this, (double)0.1F, 32.0F)));
			this.goalSelector.addGoal(1, new SleepingCheckGoal(thisAsEater, new EaterMeleeAttackGoal(thisAsEater, (double)1.0F, false)));
			this.goalSelector.addGoal(5, new SleepingCheckGoal(thisAsEater, new ResetUniversalAngerTargetGoal<>(thisAsEater, false)));
			this.goalSelector.addGoal(5, new EaterSleepingGoal(thisAsEater));
		}

		ci.cancel();
	}
}
