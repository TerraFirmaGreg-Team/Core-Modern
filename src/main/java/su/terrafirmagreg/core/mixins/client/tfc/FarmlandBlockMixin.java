package su.terrafirmagreg.core.mixins.client.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import su.terrafirmagreg.core.utils.ClientClimateHelpers;

@Mixin(value = FarmlandBlock.class, remap = false)
public abstract class FarmlandBlockMixin {

    /**
     * This crop temp information is client-side only, which doesn't have access to the server-side information of
     * whether the block is oxygenated or not, so we can't have the tooltip depend on that. As a workaround, we just
     * tell players "hey this plant needs oxygen" and they should be able to figure out the rest
     */
    @ModifyVariable(method = "getTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;FZLjava/lang/String;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), argsOnly = true)
    private static float tfg$modifyTemperatureForFarmlandTooltip(float temperature, Level level, BlockPos pos, ClimateRange validRange) {
        return ClientClimateHelpers.getTemperatureForTooltip(level, pos);
    }
}
