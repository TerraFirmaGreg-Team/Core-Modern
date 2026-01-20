package su.terrafirmagreg.core.mixins.client.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.dries007.tfc.client.ClimateRenderCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.utils.ClientClimateHelpers;

@Mixin(value = ClimateRenderCache.class, remap = false)
public class ClimateRenderCacheMixin {
    /**
     * Fix Clientside Climate current temperature display
     */
    @Redirect(method = "onClientTick", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"))
    private float tfg$getTemperatureForClimateDisplay(Level level, BlockPos pos) {
        return ClientClimateHelpers.getTemperatureForTooltip(level, pos);
    }
}
