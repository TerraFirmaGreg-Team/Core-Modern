package su.terrafirmagreg.core.mixins.common.tfc;

import earth.terrarium.adastra.api.systems.OxygenApi;
import net.dries007.tfc.common.blockentities.BlastFurnaceBlockEntity;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents the blast furnace from being lit in dimensions with no oxygen
 */

@Mixin(value = BlastFurnaceBlockEntity.class, remap = false)
public abstract class BlastFurnaceBlockEntityMixin {

	@Inject(method = "light", at = @At("HEAD"), remap = false, cancellable = true)
	public void tfg$light(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (!OxygenApi.API.hasOxygen(level, pos))
		{
			Helpers.playSound(level, pos, SoundEvents.FIRE_EXTINGUISH);
			cir.setReturnValue(false);
		}
	}
}
