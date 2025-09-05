package su.terrafirmagreg.core.mixins.common.ad_astra;

import earth.terrarium.adastra.common.entities.mob.MartianRaptor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Changes the martian raptor's sounds to skeleton sounds, to fit with its retexture

@Mixin(value = MartianRaptor.class, remap = true)
public class MartianRaptorMixin extends Mob {

	protected MartianRaptorMixin(EntityType<? extends Mob> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true, remap = true)
	private void tfg$getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> cir) {
		cir.setReturnValue(SoundEvents.SKELETON_HURT);
	}

	@Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true, remap = true)
	private void tfg$getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
		cir.setReturnValue(SoundEvents.SKELETON_DEATH);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SKELETON_AMBIENT;
	}
}
