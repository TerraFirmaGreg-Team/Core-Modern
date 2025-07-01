package su.terrafirmagreg.core.mixins.common.tfc;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FarmlandBlock.class, remap = false)
public abstract class FarmlandBlockMixin {

	@Inject(method = "getTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;FZLjava/lang/String;)Lnet/minecraft/network/chat/Component;",
		at = @At("HEAD"),
		remap = false,
		cancellable = true)
	private static void tfg$getTemperatureTooltip(Level level, BlockPos pos, ClimateRange validRange, float temperature, boolean allowWiggle, String translationKey, CallbackInfoReturnable<Component> cir)
	{
		Planet planet = PlanetApi.API.getPlanet(level);
		if (planet != null && !planet.oxygen())
		{
			cir.setReturnValue(Component.translatable("tfg.tooltip.extraterrestrial_farming"));
		}
	}
}
