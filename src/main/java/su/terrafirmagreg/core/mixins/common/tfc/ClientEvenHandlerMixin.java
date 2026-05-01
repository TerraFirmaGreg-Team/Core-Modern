package su.terrafirmagreg.core.mixins.common.tfc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import net.dries007.tfc.client.ClientEventHandler;
import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import su.terrafirmagreg.core.common.entity.animals.tfcwolf.TFCWolfRenderer;

@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEvenHandlerMixin {
    @ModifyArg(method = "registerEntityRenderers", remap = false, slice = @Slice(from = @At(value = "FIELD", target = "Lnet/dries007/tfc/common/entities/TFCEntities;WOLF:Lnet/minecraftforge/registries/RegistryObject;")), at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/EntityRenderersEvent$RegisterRenderers;registerEntityRenderer(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/client/renderer/entity/EntityRendererProvider;)V", ordinal = 0), index = 1)
    private static EntityRendererProvider<?> tfg$replaceWolfRenderer(EntityRendererProvider<?> original) {
        return (EntityRendererProvider<PackPredator>) TFCWolfRenderer::new;
    }
}
