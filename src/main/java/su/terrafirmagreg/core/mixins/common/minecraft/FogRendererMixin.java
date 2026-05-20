package su.terrafirmagreg.core.mixins.common.minecraft;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.renderer.FogRenderer;

@Mixin(value = FogRenderer.class, remap = true)
public class FogRendererMixin {

    // Changes the distance of fog in dimensions where the dimension effects return true for "isFoggyAt", such
    // as the nether, so the end plane of the fog is much further away.
    // This keeps the fog feeling while still letting the player actually see more of the world

    @Redirect(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/FogRenderer$FogData;end:F", ordinal = 9, opcode = Opcodes.PUTFIELD), remap = true)
    private static void tfg$setupFog(FogRenderer.FogData fogData, float value, @Local(argsOnly = true, ordinal = 0) float farPlaneDistance) {
        fogData.end = farPlaneDistance;
    }
}
