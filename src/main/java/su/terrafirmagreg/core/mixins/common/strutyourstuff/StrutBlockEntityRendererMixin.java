package su.terrafirmagreg.core.mixins.common.strutyourstuff;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.cake.struts.content.block.StrutBlockEntity;
import com.cake.struts.content.block.StrutBlockEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

@Mixin(value = StrutBlockEntityRenderer.class)
public class StrutBlockEntityRendererMixin {
    @ModifyVariable(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), name = "consumer")
    private VertexConsumer tfg$replaceConsumer(VertexConsumer original, StrutBlockEntity blockEntity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        return buffer.getBuffer(RenderType.cutout());
    }
}
