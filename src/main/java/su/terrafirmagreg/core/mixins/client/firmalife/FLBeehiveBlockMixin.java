package su.terrafirmagreg.core.mixins.client.firmalife;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.eerussianguy.firmalife.common.blocks.FLBeehiveBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.utils.ClientClimateHelpers;

@Mixin(value = FLBeehiveBlock.class, remap = false)
public abstract class FLBeehiveBlockMixin {

    /**
     * Fix Clientside beehive temperature tooltip
     */
    @Redirect(method = "lambda$addHoeOverlayInfo$6", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/util/climate/Climate;getTemperature(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)F"))
    private static float tfg$getTemperatureForBeehiveTooltip(Level level, BlockPos pos) {
        return ClientClimateHelpers.getTemperatureForTooltip(level, pos);
    }
}
