package su.terrafirmagreg.core.mixins.client.alekiships;

import com.alekiponi.alekiships.client.render.texture.PaintedTexture;
import com.alekiponi.alekiships.client.resources.PaintedTextureGenerator;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import su.terrafirmagreg.core.TFGCore;
import su.terrafirmagreg.core.client.LazyPaintedTexture;

@Mixin(value = PaintedTextureGenerator.class, remap = true)
public class PaintedTextureGeneratorMixin {
    @Redirect(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;register(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/renderer/texture/AbstractTexture;)V", ordinal = 0))
    private void tfg$lazyRegisterBoatTexture(TextureManager instance, ResourceLocation path, AbstractTexture texture) {
        instance.register(path, texture instanceof PaintedTexture pt ? new LazyPaintedTexture(pt) : texture);
        TFGCore.LOGGER.debug("Lazy paint registered texture: {}", path);
    }
}
