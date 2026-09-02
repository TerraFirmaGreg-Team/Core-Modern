package su.terrafirmagreg.core.mixins.client.ihearttfc;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.alekiponi.ihearttfc.IngameOverlays;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.dries007.tfc.client.ClimateRenderCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.tfcambiental.capability.TemperatureCapability;

@Mixin(value = IngameOverlays.class, remap = false)
public class IngameOverlaysMixin {
    @Unique
    private static final ResourceLocation TEXTURE_TFG = TFGCore.id("textures/gui/icons/temperature_indicators.png");

    @ModifyExpressionValue(method = "renderTemperature", at = @At(value = "INVOKE", target = "Lnet/dries007/tfc/client/ClimateRenderCache;getTemperature()F", ordinal = 0))
    private static float tfg$replaceCurrentTempWithPlayerTemp(float currentTemp, @Local(name = "player") Player player) {
        return player.getCapability(TemperatureCapability.CAPABILITY).map(TemperatureCapability::getTemperature).orElseThrow();
    }

    @Inject(method = "renderTemperature", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private static void renderTemperature(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height, CallbackInfo ci) {
        float averageTemp = ClimateRenderCache.INSTANCE.getAverageTemperature();
        float currentTemp = ClimateRenderCache.INSTANCE.getTemperature();

        if (!Float.isNaN(averageTemp) && !Float.isNaN(currentTemp)) {
            float percentAverageTemp = (Mth.clamp(averageTemp, -45.0F, 45.0F) + 45.0F) / 90.0F;
            float percentCurrentTemp = (Mth.clamp(currentTemp, -45.0F, 45.0F) + 45.0F) / 90.0F;
            int averageTempPixels = (int) (percentAverageTemp * 178.0F);
            int currentTempPixels = (int) (percentCurrentTemp * 178.0F);
            graphics.blit(TEXTURE_TFG, -92 + averageTempPixels, -2, 0, 0, 7, 9, 14, 9);
            graphics.blit(TEXTURE_TFG, -92 + currentTempPixels, -2, 7, 0, 7, 9, 14, 9);
        }
    }
}
