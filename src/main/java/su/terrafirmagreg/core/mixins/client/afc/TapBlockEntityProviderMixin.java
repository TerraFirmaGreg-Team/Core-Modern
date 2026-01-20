package su.terrafirmagreg.core.mixins.client.afc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.therighthon.afc.compat.jade.TapBlockEntityProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.utils.ClientClimateHelpers;

@Mixin(value = TapBlockEntityProvider.class, remap = false)
public abstract class TapBlockEntityProviderMixin {

    /**
     * Fix Clientside tree tap temperature tooltip
     */
    @Redirect(method = "appendTooltip", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"))
    private float tfg$getTemperatureForTreeTapTooltip(Level level, BlockPos pos) {
        return ClientClimateHelpers.getTemperatureForTooltip(level, pos);
    }
}
