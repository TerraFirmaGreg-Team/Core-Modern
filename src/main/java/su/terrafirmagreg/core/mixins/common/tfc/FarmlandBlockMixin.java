package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.api.planets.PlanetApi;

@Mixin(value = FarmlandBlock.class, remap = false)
public abstract class FarmlandBlockMixin {

    /**
     * This crop temp information is client-side only, which doesn't have access to the server-side information of
     * whether the block is oxygenated or not, so we can't have the tooltip depend on that. As a workaround, we just
     * tell players "hey this plant needs oxygen" and they should be able to figure out the rest
     */

    @Inject(method = "getTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;FZLjava/lang/String;)Lnet/minecraft/network/chat/Component;", at = @At("HEAD"), remap = false, cancellable = true)
    private static void tfg$getTemperatureTooltip(Level level, BlockPos pos, ClimateRange validRange, float temperature,
            boolean allowWiggle, String translationKey, CallbackInfoReturnable<Component> cir) {
        Planet planet = PlanetApi.API.getPlanet(level);
        if (planet != null && !planet.oxygen()) {
            cir.setReturnValue(Component.translatable("tfg.tooltip.extraterrestrial_farming"));
        }
    }
}
