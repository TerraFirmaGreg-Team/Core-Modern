package su.terrafirmagreg.core.mixins.common.minecraft;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;
import net.wanmine.wab.entity.Surfer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Mob.class)
public class MobMixin {

	@Inject(method = "checkSpawnObstruction", at = @At("HEAD"), cancellable = true)
	public void tfg$checkSpawnObstruction(LevelReader pLevel, CallbackInfoReturnable<Boolean> cir) {
		if ((Mob) (Object) this instanceof Surfer)
		{
			cir.setReturnValue(pLevel.isUnobstructed((Mob) (Object) this));
		}
	}
}
