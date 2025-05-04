package su.terrafirmagreg.core.mixins.common.gtceu;

import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LargeBoilerMachine.class, remap = false)
public class LargeBoilerMachineMixin {

	@Redirect(method = "updateCurrentTemperature", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/common/machine/multiblock/steam/LargeBoilerMachine;doExplosion(F)V"), remap = false)
	private void tfg$updateCurrentTemperature$doExplosion(LargeBoilerMachine instance, float v)
	{
		return;
	}

	@Redirect(method = "updateCurrentTemperature", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/common/machine/multiblock/steam/LargeBoilerMachine;doExplosion(Lnet/minecraft/core/BlockPos;F)V"), remap = false)
	private void tfg2$updateCurrentTemperature$doExplosion(LargeBoilerMachine instance, BlockPos blockPos, float v)
	{
		return;
	}
}
