package su.terrafirmagreg.core.mixins.common.ad_astra;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.client.dimension.ModSkyRenderer;
import earth.terrarium.adastra.client.dimension.PlanetRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Completely disables sunrise colors in dims with ad astra's sky renderer where the sunrise color is set to 0.
 * (Without this, it leaves them at some dark green color.)
 * You may also need to set the sunrise angle to 90 for whatever reason, otherwise you still get a faint amount.
 */

@Mixin(value = ModSkyRenderer.class, remap = false)
public abstract class ModSkyRendererMixin {

	@Shadow @Final
	private PlanetRenderer renderer;

	@Inject(method = "renderSunrise", at = @At("HEAD"), cancellable = true, remap = false)
	public void tfg$renderSunrise(BufferBuilder bufferBuilder, ClientLevel level, float partialTick, PoseStack poseStack, float[] color, CallbackInfo ci)
	{
		if (renderer.sunriseColor() == 0)
		{
			ci.cancel();
		}
	}
}
